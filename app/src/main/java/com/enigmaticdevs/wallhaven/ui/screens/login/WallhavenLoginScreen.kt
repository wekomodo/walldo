package com.enigmaticdevs.wallhaven.ui.screens.login

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.enigmaticdevs.wallhaven.LocalToastTrigger
import com.enigmaticdevs.wallhaven.ui.presentation.PrefTopAppBar
import com.enigmaticdevs.wallhaven.ui.util.WALLHAVEN_UA
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private const val TAG = "WallhavenLogin"


@Composable
fun WallhavenLoginRoute(
    onBack: () -> Unit,
    viewmodel: WallhavenLoginViewModel = metroViewModel()
) {
    val showToast = LocalToastTrigger.current
    
    // Listen for events from the ViewModel
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewmodel.events.collect { event ->
            when (event) {
                is LoginEvent.Success -> {
                    showToast("Logged in & API Key saved!")
                    onBack()
                }
                is LoginEvent.Logout -> {
                    // Handle logout if needed
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { PrefTopAppBar("Wallhaven login form", onBack = onBack) },
    ) { paddingValues ->

        WallhavenLoginScreen(
            paddingValues = paddingValues,
            onLoginSuccess = { cookies, csrfToken ->
                viewmodel.onLoginCredentialsReceived(cookies, csrfToken)
                // We no longer call onBack() or showToast() here! 
                // We wait for the ViewModel to finish and emit LoginEvent.Success
            },
            onBack = onBack
        )
    }

}

fun simulateExpiredSession() {
    val m = CookieManager.getInstance()
    listOf("wallhaven_session", "XSRF-TOKEN").forEach {
        m.setCookie("https://wallhaven.cc", "$it=; Max-Age=0; Path=/")
    }
    m.flush()
    val names = m.getCookie("https://wallhaven.cc")?.split(";")?.map { it.substringBefore("=").trim() }
    Log.d("Spike", "After simulate: $names")   // wallhaven_session should be gone
}

private const val HOST = "wallhaven.cc"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WallhavenLoginScreen(
    onLoginSuccess: (cookies: String, csrfToken: String?) -> Unit,
    onBack: () -> Unit,
    paddingValues: PaddingValues
) {
    var webView by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var handled by remember { mutableStateOf(false) }

    BackHandler {
        if (webView?.canGoBack() == true) webView?.goBack() else onBack()
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

    Box(Modifier
        .fillMaxSize()
        .padding(paddingValues)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    val cookieManager = CookieManager.getInstance()
                    cookieManager.setAcceptCookie(true)
                    cookieManager.setAcceptThirdPartyCookies(this, false)

                    settings.apply {
                        userAgentString = WALLHAVEN_UA
                        javaScriptEnabled = true          // set false if login works without it
                        domStorageEnabled = true
                        allowFileAccess = false
                        setSupportZoom(true)
                        builtInZoomControls = true
                        allowContentAccess = false
                        mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                        //safeBrowsingEnabled = true
                        setGeolocationEnabled(false)
                    }

                    webViewClient = object : WebViewClient() {
                        // Block top-level navigation away from wallhaven
                        override fun shouldOverrideUrlLoading(
                            view: WebView, request: WebResourceRequest
                        ): Boolean {
                            val host = request.url.host ?: return true
                            
                            // Block account creation within the app to bypass Play Store's account deletion requirement.
                            // If the user tries to register, redirect them to the system browser instead.
                            val path = request.url.path ?: ""
                            if (path.contains("/join") || path.contains("/register")) {
                                val intent = Intent(Intent.ACTION_VIEW, request.url)
                                view.context.startActivity(intent)
                                return true
                            }

                            val allowed = request.url.scheme == "https" &&
                                    (host == HOST || host.endsWith(".$HOST"))
                            return !allowed // true = block
                        }

                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            isLoading = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                            if (handled || url == null) return

                            cookieManager.flush()
                            val cookies = cookieManager.getCookie(url).orEmpty()
                            val loggedIn = "wallhaven_session" in cookies &&
                                    !url.contains("/login")
                            if (!loggedIn) return

                            handled = true
                            view?.evaluateJavascript(
                                "document.querySelector('meta[name=\"csrf-token\"]')?.content"
                            ) { raw ->
                                val csrf = raw?.trim('"')
                                    ?.takeIf { it.isNotBlank() && it != "null" }
                                onLoginSuccess(cookies, csrf)
                                // Credentials are stored now; we use webview to make further calls
                                /*cookieManager.removeAllCookies(null)
                                cookieManager.flush()*/
                            }
                        }
                    }

                    loadUrl("https://$HOST/login")
                    webView = this
                }
            }
        )

        if (isLoading) {
            LinearProgressIndicator(Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter))
        }
    }
}


@OptIn(DelicateCoroutinesApi::class)
fun runSpike(label : String) {
    GlobalScope.launch(Dispatchers.IO) {

        /* val names = CookieManager.getInstance().getCookie("https://wallhaven.cc")
             ?.split(";")?.map { it.substringBefore("=").trim() }
         Log.d("Spike", "Before POST: $names")*/
        try {
            simulateExpiredSession()
            val api = WallhavenAccountApi()
            val wallpaperId = "d8vvlo"
            val collectionId = "2298578"
            //   Log.d("Test", "create -> ${api.createCollection("api-test", isPublic = false)}")
// find its ID from the official API, then:
            // Log.d("Test", "add -> ${api.addToCollection(wallpaperId, collectionId)}")
            // Log.d("Test", "remove -> ${api.removeWallpaper(wallpaperId)}")
            //   Log.d("Test", "delete -> ${api.deleteCollection(collectionId)}")
            //  WallhavenSpike().probeDupOrigin()
            // WallhavenSpike().runWithRetry("spike-test-last")
        } catch (e: Throwable) {
            Log.e("Spike", "Spike failed", e)
        }
    }
}

