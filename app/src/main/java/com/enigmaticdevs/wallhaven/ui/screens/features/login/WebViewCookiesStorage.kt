package com.enigmaticdevs.wallhaven.ui.screens.features.login

import android.util.Log
import android.webkit.CookieManager
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.CookieEncoding
import io.ktor.http.Url
import java.security.MessageDigest

class WebViewCookiesStorage : CookiesStorage {

    private fun h(s: String) =
        MessageDigest.getInstance("SHA-256").digest(s.toByteArray()).take(4)
            .joinToString("") { b -> "%02x".format(b) }

    override suspend fun get(requestUrl: Url): List<Cookie> {
        val raw = CookieManager.getInstance().getCookie(requestUrl.toString())
            ?: return emptyList()

        val all = raw.split(";")
            .map { it.trim() }
            .filter { it.contains("=") }
            .map {
                Cookie(
                    name = it.substringBefore("="),
                    value = it.substringAfter("="),
                    encoding = CookieEncoding.RAW
                )
            }

        // Duplicate names showed up on POSTs during the spike. Keep this log
        // so a future 419 can be checked against the copy order.
        all.groupBy { it.name }
            .filter { it.value.size > 1 }
            .forEach { (name, copies) ->
                Log.d("WallhavenCookies", "DUP $name url=$requestUrl copies=${copies.map { h(it.value) }}")
            }

        // One cookie per name; the last copy wins (it was the live one in every spike run).
        return all.associateBy { it.name }.values.toList()
    }

    // Cookies are written by WallhavenAccountApi.saveRawCookies(), not here.
    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {}

    override fun close() {}
}