package com.enigmaticdevs.wallhaven.ui.screens.features.login

import android.webkit.CookieManager
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.CookieEncoding
import io.ktor.http.Url
import io.ktor.http.renderCookieHeader


class WebViewCookiesStorage : CookiesStorage {

    private val manager = CookieManager.getInstance()
    override suspend fun get(requestUrl: Url): List<Cookie> =
        manager.getCookie(requestUrl.toString())
            ?.split(";")
            ?.mapNotNull { pair ->
                val name = pair.substringBefore("=").trim()
                val value = pair.substringAfter("=","").trim()
                if (name.isEmpty()) null else Cookie(name, value, encoding = CookieEncoding.RAW)
            }
            .orEmpty()


    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
       manager.setCookie(requestUrl.toString(), renderCookieHeader(cookie))
       manager.flush()
    }

    override fun close() {
    }

}