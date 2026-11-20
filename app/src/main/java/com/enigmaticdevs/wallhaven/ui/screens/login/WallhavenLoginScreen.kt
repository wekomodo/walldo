package com.enigmaticdevs.wallhaven.ui.screens.login

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import com.enigmaticdevs.wallhaven.BuildConfig
import com.enigmaticdevs.wallhaven.LocalToastTrigger
import com.enigmaticdevs.wallhaven.ui.presentation.PrefTopAppBar
import com.enigmaticdevs.wallhaven.ui.util.WALLHAVEN_UA
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.json.JSONTokener
import java.io.ByteArrayInputStream
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "WallhavenLogin"
private const val HOST = "wallhaven.cc"
private const val LOGIN_URL = "https://$HOST/login"

/* -------------------------------------------------------------------------- */
/*  Route                                                                     */
/* -------------------------------------------------------------------------- */

@Composable
fun WallhavenLoginRoute(
    onBack: () -> Unit,
    viewmodel: WallhavenLoginViewModel = metroViewModel()
) {
    val showToast = LocalToastTrigger.current
    LaunchedEffect(Unit) {
        viewmodel.events.collect { event ->
            when (event) {
                is LoginEvent.Success -> {
                    showToast("Logged in & API Key saved!")
                    onBack()
                }
                is LoginEvent.Logout -> Unit
            }
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { PrefTopAppBar("Wallhaven login", onBack = onBack) },
    ) { paddingValues ->
        WallhavenLoginScreen(
            paddingValues = paddingValues,
            onLoginSuccess = viewmodel::onLoginCredentialsReceived,
            onBack = onBack
        )
    }
}

/* -------------------------------------------------------------------------- */
/*  Navigation / detection rules                                              */
/* -------------------------------------------------------------------------- */

/** Account creation happens on the website, never inside the app. */
private val SIGNUP_PATHS = listOf("/join", "/register", "/signup")

/**
 * Path segments of pages where a session cookie does NOT mean "logged in".
 * Laravel issues a session cookie to anonymous visitors, so the cookie alone is meaningless.
 */
private val AUTH_SEGMENT_PREFIXES =
    listOf("login", "join", "register", "signup", "forgot", "password", "reset")

private fun isSignupPath(path: String): Boolean =
    SIGNUP_PATHS.any { path == it || path.startsWith("$it/") }

private fun isAuthPage(path: String): Boolean =
    path.split('/')
        .filter { it.isNotEmpty() }
        .any { seg -> AUTH_SEGMENT_PREFIXES.any { seg.startsWith(it, ignoreCase = true) } }

private fun isAllowedUrl(uri: Uri): Boolean {
    val host = uri.host ?: return false
    return uri.scheme == "https" && (host == HOST || host.endsWith(".$HOST"))
}

/** Hides signup links so users never hit a dead end. Safe to run repeatedly. */
private const val HIDE_SIGNUP_JS = """
(function(){
  if (document.getElementById('__hide_signup')) return;
  var s = document.createElement('style');
  s.id = '__hide_signup';
  s.textContent = 'a[href*="/join"],a[href*="/register"],a[href*="/signup"]{display:none !important;}';
  (document.head || document.documentElement).appendChild(s);
})();
"""

/**
 * Returns a JSON string: {"csrf": "...", "loggedIn": true|false}
 * (log in with Chrome, DevTools, find something that only exists when signed in).
 */
private const val PROBE_JS = """
(function(){
  var csrf = document.querySelector('meta[name="csrf-token"]');
  var loggedIn = !!document.querySelector(
    'a[href$="/logout"], form[action$="/logout"], a[href*="/settings"]'
  );
  return JSON.stringify({csrf: csrf ? csrf.content : null, loggedIn: loggedIn});
})();
"""

/** Decodes the value evaluateJavascript hands back (a JSON-encoded string containing JSON). */
private fun parseProbe(raw: String?): JSONObject? {
    if (raw.isNullOrBlank() || raw == "null") return null
    return runCatching {
        val inner = JSONTokener(raw).nextValue() as? String ?: return null
        JSONObject(inner)
    }.getOrNull()
}

/* -------------------------------------------------------------------------- */
/*  Screen                                                                    */
/* -------------------------------------------------------------------------- */

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WallhavenLoginScreen(
    onLoginSuccess: (cookies: String, csrfToken: String?) -> Unit,
    onBack: () -> Unit,
    paddingValues: PaddingValues
) {
    var webView by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    // Not Compose state: it's read from WebView callbacks and shouldn't trigger recomposition.
    val handled = remember { AtomicBoolean(false) }
    val currentOnLoginSuccess by rememberUpdatedState(onLoginSuccess)

    BackHandler {
        val wv = webView
        if (wv?.canGoBack() == true) wv.goBack() else onBack()
    }

    DisposableEffect(Unit) {
        onDispose {
            webView?.apply {
                stopLoading()
                webViewClient = WebViewClient()
                destroy()
            }
            webView = null
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    val cookieManager = CookieManager.getInstance()
                    cookieManager.setAcceptCookie(true)
                    cookieManager.setAcceptThirdPartyCookies(this, false)

                    settings.apply {
                        userAgentString = WALLHAVEN_UA
                        javaScriptEnabled = true // needed for the login probe
                        domStorageEnabled = true
                        allowFileAccess = false
                        allowContentAccess = false
                        setSupportZoom(true)
                        builtInZoomControls = true
                        displayZoomControls = false
                        mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                        setGeolocationEnabled(false)
                    }

                    webViewClient = object : WebViewClient() {

                        private fun showBlockedToast() {
                            Toast.makeText(
                                context,
                                "Account creation isn't available in this app.",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        // Block navigation away from wallhaven and into signup pages.
                        override fun shouldOverrideUrlLoading(
                            view: WebView,
                            request: WebResourceRequest
                        ): Boolean {
                            val url = request.url
                            if (!isAllowedUrl(url)) return true
                            if (isSignupPath(url.path.orEmpty())) {
                                showBlockedToast()
                                return true
                            }
                            return false
                        }

                        // Backstop for what shouldOverrideUrlLoading can miss (form POSTs, redirects).
                        override fun shouldInterceptRequest(
                            view: WebView,
                            request: WebResourceRequest
                        ): WebResourceResponse? {
                            if (request.isForMainFrame && isSignupPath(request.url.path.orEmpty())) {
                                return WebResourceResponse(
                                    "text/plain", "utf-8", 403, "Blocked",
                                    emptyMap(), ByteArrayInputStream(ByteArray(0))
                                )
                            }
                            return null
                        }

                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            isLoading = true
                        }

                        override fun onPageCommitVisible(view: WebView, url: String?) {
                            view.evaluateJavascript(HIDE_SIGNUP_JS, null)
                        }

                        override fun onPageFinished(view: WebView, url: String?) {
                            isLoading = false
                            view.evaluateJavascript(HIDE_SIGNUP_JS, null)

                            if (handled.get() || url == null) return
                            // Register / forgot-password / login pages never count as "logged in".
                            if (isAuthPage(url.toUri().path.orEmpty())) return

                            view.evaluateJavascript(PROBE_JS) { raw ->
                                if (handled.get()) return@evaluateJavascript
                                val json = parseProbe(raw) ?: return@evaluateJavascript
                                if (!json.optBoolean("loggedIn")) return@evaluateJavascript

                                cookieManager.flush()
                                val cookies = cookieManager.getCookie(url).orEmpty()
                                if ("wallhaven_session" !in cookies) return@evaluateJavascript

                                // compareAndSet guards against onPageFinished firing twice quickly.
                                if (!handled.compareAndSet(false, true)) return@evaluateJavascript

                                val csrf = json.optString("csrf")
                                    .takeIf { it.isNotBlank() && it != "null" }
                                currentOnLoginSuccess(cookies, csrf)
                            }
                        }
                    }

                    loadUrl(LOGIN_URL)
                    webView = this
                }
            }
        )

        if (isLoading) {
            LinearProgressIndicator(
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            )
        }
    }
}

/* -------------------------------------------------------------------------- */
/*  Debug-only helpers (move to a debug source set when convenient)           */
/* -------------------------------------------------------------------------- */

fun simulateExpiredSession() {
    if (!BuildConfig.DEBUG) return
    val m = CookieManager.getInstance()
    listOf("wallhaven_session", "XSRF-TOKEN").forEach {
        m.setCookie("https://$HOST", "$it=; Max-Age=0; Path=/")
    }
    m.flush()
    val names = m.getCookie("https://$HOST")?.split(";")?.map { it.substringBefore("=").trim() }
    Log.d(TAG, "After simulate: $names")
}

@OptIn(DelicateCoroutinesApi::class)
fun runSpike(label: String) {
    if (!BuildConfig.DEBUG) return
    GlobalScope.launch(Dispatchers.IO) {
        try {
            simulateExpiredSession()
            val api = WallhavenAccountApi()
            val wallpaperId = "d8vvlo"
            val collectionId = "2298578"
            // Log.d(TAG, "create -> ${api.createCollection("api-test", isPublic = false)}")
            // Log.d(TAG, "add -> ${api.addToCollection(wallpaperId, collectionId)}")
            // Log.d(TAG, "remove -> ${api.removeWallpaper(wallpaperId)}")
            // Log.d(TAG, "delete -> ${api.deleteCollection(collectionId)}")
        } catch (e: Throwable) {
            Log.e(TAG, "Spike failed ($label)", e)
        }
    }
}
