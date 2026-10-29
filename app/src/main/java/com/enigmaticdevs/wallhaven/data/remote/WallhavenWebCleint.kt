package com.enigmaticdevs.wallhaven.data.remote

import com.enigmaticdevs.wallhaven.di.flag.SkipSavedApiKey
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Named
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

@Inject
class WallhavenWebClient(private val client: HttpClient) {

    /** Returns the account's API key, or null if the cookie isn't a logged-in session. */
    suspend fun fetchApiKey(cookie: String): String? {
        val response = client.get("https://wallhaven.cc/settings/account") {
            attributes.put(SkipSavedApiKey,true)
            header(HttpHeaders.Cookie, cookie)
        }
        // Logged-out visitors get redirected to the login page
        if (!response.status.isSuccess() || "login" in response.request.url.encodedPath) return null

        val document = Jsoup.parse(response.bodyAsText())

        return (document.selectFirst("input[type=text][readonly]")
            ?: document.selectFirst("h3:contains(API Key) + p + p input"))
            ?.attr("value")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    }
}

// fetch fresh CSRF token on app launch
suspend fun fetchFreshCsrfToken(client: HttpClient): String? = withContext(Dispatchers.IO) {
    runCatching {
        // Ktor automatically attaches your persistent cookies via WebViewCookiesStorage
        val response = client.get("https://wallhaven.cc/settings/account")
        val html = response.bodyAsText()

        // 1. Primary: Extract from <meta name="csrf-token" content="...">
        val doc = Jsoup.parse(html)
        val metaToken = doc.selectFirst("meta[name=csrf-token]")?.attr("content")?.trim()

        if (!metaToken.isNullOrEmpty()) {
            return@withContext metaToken
        }

        // 2. Fallback: Extract from the hidden logout form field <input type="hidden" name="_token" value="...">
        doc.selectFirst("form[action*='logout'] input[name=_token]")
            ?.attr("value")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    }.getOrNull()
}