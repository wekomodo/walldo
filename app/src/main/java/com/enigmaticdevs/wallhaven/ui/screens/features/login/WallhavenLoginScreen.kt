package com.enigmaticdevs.wallhaven.ui.screens.features.login

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.enigmaticdevs.wallhaven.LocalToastTrigger
import com.enigmaticdevs.wallhaven.data.local.SecureStorage
import dev.zacsweers.metrox.viewmodel.metroViewModel

private const val TAG = "WallhavenLogin"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WallhavenLoginRoute(
    onBack: () -> Unit,
    viewmodel: WallhavenLoginViewModel = metroViewModel()
){
    val showToast = LocalToastTrigger.current
    WallhavenLoginScreen(
        onLoginSuccess = { cookies, csrfToken ->
            viewmodel.onLoginCredentialsReceived(cookies,csrfToken)
            showToast("Logged in & API Key saved!")
            onBack()
        },
        onBack = onBack
    )
}
@Composable
fun WallhavenLoginScreen(
    onLoginSuccess: (cookies: String, csrfToken: String?) -> Unit,
    onBack: () -> Unit
) {
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Allow back navigation inside the WebView history before closing the screen
    BackHandler {
        if (webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        } else {
            onBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    // 1. Enable Cookies & DOM Storage
                    val cookieManager = CookieManager.getInstance()
                    cookieManager.setAcceptCookie(true)
                    cookieManager.setAcceptThirdPartyCookies(this, true)

                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.userAgentString = settings.userAgentString // Standard Android User-Agent

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isLoading = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                            cookieManager.flush() // Force cookies from RAM to disk SQLite

                            url?.let { currentUrl ->
                                val cookies = cookieManager.getCookie(currentUrl) ?: ""
                                Log.d(TAG, "Current URL: $currentUrl")
                                Log.d(TAG, "Cookies: $cookies")

                                // Check if user completed login (redirected away from /login, or session cookie exists)
                                val hasSession = cookies.contains("wallhaven_session")
                                val isNotLoginPage = !currentUrl.contains("/login")

                                if (hasSession && isNotLoginPage) {
                                    // 2. Extract CSRF Token via JavaScript evaluation
                                    view?.evaluateJavascript(
                                        "document.querySelector('meta[name=\"csrf-token\"]')?.getAttribute('content')"
                                    ) { csrfResult ->
                                        // Result comes wrapped in quotes e.g. "\"abcd123\""
                                        val cleanCsrf = csrfResult
                                            ?.replace("\"", "")
                                            ?.takeIf { it != "null" && it.isNotBlank() }

                                        Log.d(TAG, "LOGIN SUCCESS!")
                                        Log.d(TAG, "Extracted CSRF: $cleanCsrf")

                                        onLoginSuccess(cookies, cleanCsrf)
                                    }
                                }
                            }
                        }
                    }

                    loadUrl("https://wallhaven.cc/login")
                    webViewInstance = this
                }
            },
            update = {
                webViewInstance = it
            }
        )

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            )
        }
    }
}