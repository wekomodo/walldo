package com.enigmaticdevs.wallhaven.ui.screens.features.login

import android.util.Log
import android.webkit.CookieManager
import com.enigmaticdevs.wallhaven.ui.screens.features.login.AccountOutcome
import com.enigmaticdevs.wallhaven.ui.util.WALLHAVEN_UA
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.HttpHeaders
import io.ktor.http.parameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.IOException
import kotlin.coroutines.resume

sealed interface AccountOutcome<out T> {
    data class Success<T>(val value: T) : AccountOutcome<T>

    /** Redirected to /login: show the login screen. */
    data object NeedsLogin : AccountOutcome<Nothing>

    /** cf-mitigated: challenge, or 403/503: open the Cloudflare solver WebView. */
    data object NeedsCloudflare : AccountOutcome<Nothing>

    /** Still 419 after the retry. */
    data object SessionExpired : AccountOutcome<Nothing>

    /** Anything else. code = -1 means a network error before any response. */
    data class Failed(val code: Int, val message: String? = null) : AccountOutcome<Nothing>
}

/**
 * The four wallhaven.cc writes that the official API doesn't offer.
 * Reads (collections, wallpapers) stay on the official API.
 *
 * Every function returns an AccountOutcome. Success means the server accepted the
 * request; it does NOT prove the change happened, so verify with an API pull.
 *
 * Call from an app-level scope or WorkManager, never a screen/ViewModel scope.
 */
class WallhavenAccountApi(
    private val http: HttpClient = createClient()
) {

    companion object {
        private const val TAG = "WallhavenAccountApi"
        private const val BASE = "https://wallhaven.cc"
        private const val TOKEN_PAGE = "$BASE/favorites"
        private const val MAX_ATTEMPTS = 2

        fun createClient() = HttpClient(OkHttp) {
            followRedirects = false
            install(UserAgent) { agent = WALLHAVEN_UA }
            install(HttpCookies) { storage = WebViewCookiesStorage() }
        }
    }

    // Writes share one cookie jar, so run them one at a time.
    private val mutex = Mutex()

    // ---------------------------------------------------------------- public API

    /** Creates a collection. The response has no ID; find it by label after an API pull. */
    suspend fun createCollection(label: String, isPublic: Boolean): AccountOutcome<Unit> =
        execute { token ->
            http.submitForm(
                url = "$BASE/collection/new",
                formParameters = parameters {
                    append("_token", token)
                    append("label", label)
                    if (isPublic) append("public", "on")
                }
            )
        }

    /** Deletes a collection (not the default one). */
    suspend fun deleteCollection(collectionId: String): AccountOutcome<Unit> =
        execute { token ->
            http.get("$BASE/collection/remove/$collectionId") {
                parameter("_token", token)
            }
        }

    /**
     * Adds a wallpaper to a collection. The server silently ignores this if the wallpaper
     * is already in another collection, so to move one: removeWallpaper, then add.
     */
    suspend fun addToCollection(wallpaperId: String, collectionId: String): AccountOutcome<Unit> =
        execute { token ->
            http.post("$BASE/favorites/add") {
                parameter("wallHashid", wallpaperId)
                parameter("collectionId", collectionId)
                parameter("_token", token)
            }
        }

    /** Removes a wallpaper from whichever collection holds it. No collection ID needed. */
    suspend fun removeWallpaper(wallpaperId: String): AccountOutcome<Unit> =
        execute { token ->
            http.post("$BASE/favorites/delete/$wallpaperId") {
                parameter("_token", token)
            }
        }

    // ------------------------------------------------------------ shared request path

    /**
     * GET a page, scrape the CSRF token, run the write, and map the result.
     * On a 419 it repeats once with a fresh GET and token (Laravel rejects a 419
     * before running the action, so a retry can't double-apply a write).
     */
    private suspend fun execute(
        request: suspend (token: String) -> HttpResponse
    ): AccountOutcome<Unit> = mutex.withLock {
        try {
            for (attempt in 0 until MAX_ATTEMPTS) {
                // 1. Fresh GET for cookies and token
                val page = http.get(TOKEN_PAGE)
                saveRawCookies(page)

                classifyFailure(page)?.let { return@withLock it }
                if (page.status.value != 200) {
                    return@withLock AccountOutcome.Failed(page.status.value, "token page")
                }

                val token = Jsoup.parse(page.bodyAsText())
                    .selectFirst("meta[name=csrf-token]")?.attr("content")
                    ?: return@withLock AccountOutcome.Failed(200, "no csrf token on page")

                // 2. The write
                val resp = request(token)
                saveRawCookies(resp)
                Log.d(TAG, "attempt=$attempt ${resp.request.url.encodedPath} -> ${resp.status.value}")

                // 3. Map the result
                if (resp.status.value == 419) {
                    if (attempt == MAX_ATTEMPTS - 1) return@withLock AccountOutcome.SessionExpired
                    continue
                }
                classifyFailure(resp)?.let { return@withLock it }

                return@withLock if (resp.status.value in 200..399) {
                    AccountOutcome.Success(Unit)
                } else {
                    AccountOutcome.Failed(resp.status.value)
                }
            }
            AccountOutcome.SessionExpired
        } catch (e: IOException) {
            Log.w(TAG, "network error", e)
            AccountOutcome.Failed(-1, e.message)
        }
    }

    /** Cloudflare block or login redirect, or null if the response is neither. */
    private fun classifyFailure(resp: HttpResponse): AccountOutcome<Nothing>? {
        val code = resp.status.value
        if (resp.headers["cf-mitigated"] == "challenge" || code == 403 || code == 503) {
            return AccountOutcome.NeedsCloudflare
        }
        if (code in 300..399 && resp.headers[HttpHeaders.Location].orEmpty().contains("/login")) {
            return AccountOutcome.NeedsLogin
        }
        return null
    }

    /** Writes each raw Set-Cookie header into the WebView CookieManager, then flushes. */
    private suspend fun saveRawCookies(response: HttpResponse) {
        val headers = response.headers.getAll(HttpHeaders.SetCookie) ?: return
        val url = response.request.url.toString()

        // The callback version of setCookie needs a thread with a Looper, so use Main.
        withContext(Dispatchers.Main) {
            val manager = CookieManager.getInstance()
            headers.forEach { header ->
                suspendCancellableCoroutine { cont ->
                    manager.setCookie(url, header) { cont.resume(Unit) }
                }
            }
            manager.flush()
        }
    }
}