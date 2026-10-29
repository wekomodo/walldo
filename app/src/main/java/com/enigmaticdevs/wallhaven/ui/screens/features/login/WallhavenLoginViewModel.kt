package com.enigmaticdevs.wallhaven.ui.screens.features.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enigmaticdevs.wallhaven.di.AppGraph
import com.enigmaticdevs.wallhaven.data.local.SecureStorage
import com.enigmaticdevs.wallhaven.data.remote.WallhavenWebClient
import com.enigmaticdevs.wallhaven.data.repository.SettingsRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import io.ktor.client.HttpClient
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

private val TAG = "WallhavenLoginViewModel"
@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey(WallhavenLoginViewModel::class)
class WallhavenLoginViewModel(
    private val secureStorage : SecureStorage,
    private val settingsRepository: SettingsRepository,
    private val wallhavenWebClient: WallhavenWebClient
) : ViewModel() {

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onLoginCredentialsReceived(
        cookies: String,
        csrfToken: String?
    ) {
        viewModelScope.launch {
            // 1. Save cookies & CSRF into hardware-encrypted storage
            secureStorage.saveCookies(cookies)
            csrfToken?.let { token ->
                secureStorage.saveCsrfToken(token)
            }

            // 2. Background attempt to grab & save the read-only API key
            val apiKey = wallhavenWebClient.fetchApiKey(cookies)
            if (!apiKey.isNullOrBlank()) {
                settingsRepository.updateApiKey(apiKey)
            }
            else
                Log.e(TAG,"API Key fetch failed")

            // 3. Emit success event to trigger navigation
            _events.send(LoginEvent.Success)
        }
    }

    fun logout() {
        viewModelScope.launch {
            // Clear encrypted session tokens
            secureStorage.clearSession()
            // Clear the API key from DataStore
            settingsRepository.updateApiKey("")
            // Wipe Android's WebKit CookieManager so WebView doesn't auto-login next time
            android.webkit.CookieManager.getInstance().removeAllCookies(null)
            android.webkit.CookieManager.getInstance().flush()

            _events.send(LoginEvent.Logout)
        }
    }

}


sealed interface LoginEvent {
    data object Success : LoginEvent
    data object Logout : LoginEvent
}