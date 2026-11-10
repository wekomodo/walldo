package com.enigmaticdevs.wallhaven.ui.screens.features.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enigmaticdevs.wallhaven.di.AppGraph
import com.enigmaticdevs.wallhaven.data.local.SecureStorage
import com.enigmaticdevs.wallhaven.data.remote.WallhavenWebClient
import com.enigmaticdevs.wallhaven.data.repository.GlobalFlagsRepository
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
@ViewModelKey
class WallhavenLoginViewModel(
    private val secureStorage : SecureStorage,
    private val settingsRepository: SettingsRepository,
    private val globalFlagsRepository: GlobalFlagsRepository,
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
            val data = wallhavenWebClient.fetchCredentials(cookies)
            if (!data?.apiKey.isNullOrBlank()){
                globalFlagsRepository.updateLoginStatus(true)
                settingsRepository.updateApiKey(data.apiKey)
                settingsRepository.updateUsername(data.username)
            }
            else
                Log.e(TAG,"API Key fetch failed")

            
            // 3. Emit success event to trigger navigation
            _events.send(LoginEvent.Success)
        }
    }

}


sealed interface LoginEvent {
    data object Success : LoginEvent
    data object Logout : LoginEvent
}