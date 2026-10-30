package com.enigmaticdevs.wallhaven.ui.screens.features.login

import android.annotation.SuppressLint
import android.graphics.Bitmap
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
import androidx.compose.foundation.layout.statusBarsPadding
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
import dev.zacsweers.metrox.viewmodel.metroViewModel

private const val TAG = "WallhavenLogin"


@Composable
fun WallhavenLoginRoute(
    onBack: () -> Unit,
    viewmodel: WallhavenLoginViewModel = metroViewModel()
){
    val showToast = LocalToastTrigger.current
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { PrefTopAppBar("Wallhaven login form",onBack = onBack) },
    ) { paddingValues ->

        WallhavenLoginScreen(
            paddingValues = paddingValues,
            onLoginSuccess = { cookies, csrfToken ->
                viewmodel.onLoginCredentialsReceived(cookies,csrfToken)
                showToast("Logged in & API Key saved!")
                onBack()
            },
            onBack = onBack
        )
    }

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

    Box(Modifier.fillMaxSize().padding(paddingValues)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    val cookieManager = CookieManager.getInstance()
                    cookieManager.setAcceptCookie(true)
                    cookieManager.setAcceptThirdPartyCookies(this, false)

                    settings.apply {
                        javaScriptEnabled = true          // set false if login works without it
                        domStorageEnabled = true
                        allowFileAccess = false
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
                                // Credentials are stored now; wipe the WebView's copy
                                cookieManager.removeAllCookies(null)
                                cookieManager.flush()
                            }
                        }
                    }

                    loadUrl("https://$HOST/login")
                    webView = this
                }
            }
        )

        if (isLoading) {
            LinearProgressIndicator(Modifier.fillMaxWidth().align(Alignment.TopCenter))
        }
    }
}