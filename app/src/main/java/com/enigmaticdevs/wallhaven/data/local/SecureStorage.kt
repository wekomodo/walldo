package com.enigmaticdevs.wallhaven.data.local

import android.content.Context
import androidx.core.content.edit
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Inject
@SingleIn(AppScope::class)
class SecureStorage(context: Context) {

    private val prefs = context.getSharedPreferences("walldo_auth_session", Context.MODE_PRIVATE)
    private val crypto = CryptoManager()

    private val _csrfTokenFlow = MutableStateFlow(getCsrfToken())
    val csrfTokenFlow = _csrfTokenFlow.asStateFlow()

    fun saveCsrfToken(token: String) {
        val encrypted = crypto.encrypt(token)
        prefs.edit { putString(KEY_CSRF, encrypted) }
        _csrfTokenFlow.value = token
    }

    fun getCsrfToken(): String {
        val encrypted = prefs.getString(KEY_CSRF, "") ?: ""
        return crypto.decrypt(encrypted)
    }

    fun saveCookies(cookies: String) {
        val encrypted = crypto.encrypt(cookies)
        prefs.edit { putString(KEY_COOKIES, encrypted) }
    }

    fun getCookies(): String {
        val encrypted = prefs.getString(KEY_COOKIES, "") ?: ""
        return crypto.decrypt(encrypted)
    }

    fun clearSession() {
        prefs.edit { clear() }
        _csrfTokenFlow.value = ""
    }

    companion object {
        private const val KEY_CSRF = "enc_csrf_token"
        private const val KEY_COOKIES = "enc_cookies"
    }
}

@Inject
@SingleIn(AppScope::class)
class SessionTokenHolder {
    // In-memory only: resets on process death
    var csrfToken: String? = null
        private set

    fun updateToken(token: String?) {
        csrfToken = token
    }

    fun clear() {
        csrfToken = null
    }
}