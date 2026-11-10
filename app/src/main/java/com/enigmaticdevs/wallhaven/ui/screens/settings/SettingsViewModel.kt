package com.enigmaticdevs.wallhaven.ui.screens.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import com.enigmaticdevs.wallhaven.data.local.SecureStorage
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionSyncManager
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionsDatabase
import com.enigmaticdevs.wallhaven.data.repository.GlobalFlagsRepository
import com.enigmaticdevs.wallhaven.data.repository.SettingsRepository
import com.enigmaticdevs.wallhaven.ui.screens.features.login.LoginEvent
import com.enigmaticdevs.wallhaven.ui.theme.ThemeMode
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.log


data class SettingsUiState(
    val dynamicColors : Boolean = false,
    val theme: ThemeMode = ThemeMode.System,
    val apiKey: String = "",
    val wallpaperSetter: String = "native",
    val isLoaded: Boolean = false,
    val cacheSizeBytes: Long = 0L,
    val loginStatus : Boolean = false,
    val username : String = ""
)

enum class KeyCheck { Idle, Checking, Invalid, NetworkError, Saved }

@Inject
@SingleIn(AppScope::class)
class ImageCacheManager(private val imageLoader: ImageLoader) {
    suspend fun sizeBytes(): Long =
        withContext(Dispatchers.IO) { imageLoader.diskCache?.size ?: 0L }

    suspend fun clear() = withContext(Dispatchers.IO) {
        imageLoader.diskCache?.clear()
        imageLoader.memoryCache?.clear()
    }
}

// Simple private data class to hold the intermediate state
private data class RepoState(
    val colors: Boolean= false,
    val theme: String,
    val setter: String,
    val key: String
)


@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey
class SettingsViewModel(
    private val secureStorage: SecureStorage,
    private val imageCacheManager: ImageCacheManager,
    private val repository: SettingsRepository,
    private val globalFlagsRepository: GlobalFlagsRepository,
    private val collectionSyncManager: CollectionSyncManager
) : ViewModel() {

    private val TAG = "SettingsViewModel"
    private val cacheSize = MutableStateFlow(0L)
    private val _keyCheck = MutableStateFlow(KeyCheck.Idle)
    val keyCheck: StateFlow<KeyCheck> = _keyCheck.asStateFlow()
    val uiState: StateFlow<SettingsUiState> = combine(
        //group 1
        combine(
            repository.dynamicColorsFlow,
            repository.themeFlow,
            repository.wallpaperSetterFlow,
            repository.apiKeyFlow,
        ) { colors, theme, setter, key ->
            RepoState(colors, theme, setter, key)
        },
        cacheSize,
        globalFlagsRepository.loginStatus,
        repository.usernameFlow
    ) { repoState, size, loginStatus,username ->
        SettingsUiState(
            dynamicColors = repoState.colors,
            theme = ThemeMode.from(repoState.theme),
            apiKey = repoState.key,
            wallpaperSetter = repoState.setter,
            cacheSizeBytes = size,
            loginStatus = loginStatus,
            username = username,
            isLoaded = true,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )
    private val _events = Channel<String>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        refreshCacheSize()
    }


    fun updateLoginStatus(value : Boolean) = globalFlagsRepository.updateLoginStatus(value)

    fun updateTheme(mode: ThemeMode) {
        viewModelScope.launch { repository.updateTheme(mode.name) }
    }

    fun updateDynamicColors(value: Boolean){
        viewModelScope.launch { repository.updateDynamicColors(value) }
    }
    fun authenticateAPIkey(apiKey: String) {
        val key = apiKey.trim()
        if (_keyCheck.value == KeyCheck.Checking) return
        viewModelScope.launch {

            // Blank means "remove my key", so there's nothing to validate
            if (key.isEmpty()) {
                repository.updateApiKey("")
                _keyCheck.value = KeyCheck.Saved
                return@launch
            }
            _keyCheck.value = KeyCheck.Checking
            val result = runCatching { repository.authenticateAPIkey(key) }
            result.onFailure {
                Log.e(TAG, result.exceptionOrNull()?.message.toString())
            }
            _keyCheck.value = when {
                result.isFailure -> {
                    KeyCheck.NetworkError
                }

                result.getOrNull() == null -> KeyCheck.Invalid
                else -> {
                    repository.updateApiKey(key)
                    _events.send("API key saved")
                    KeyCheck.Saved
                }
            }
        }
    }


    fun logout() {
        viewModelScope.launch {
            // Clear encrypted session tokens
            secureStorage.clearSession()
            // Clear the API key from DataStore
            repository.updateApiKey("")
            collectionSyncManager.onLoggedOut()
            // Wipe Android's WebKit CookieManager so WebView doesn't auto-login next time
            android.webkit.CookieManager.getInstance().removeAllCookies(null)
            android.webkit.CookieManager.getInstance().flush()
            globalFlagsRepository.updateLoginStatus(false)
            _events.send("Logged out!")
        }
    }


    fun resetKeyCheck() {
        _keyCheck.value = KeyCheck.Idle
    }

    fun clearImageCache() {
        viewModelScope.launch {
            imageCacheManager.clear()
            refreshCacheSize()
            _events.send("Cache cleared")
        }
    }

    private fun refreshCacheSize() {
        viewModelScope.launch { cacheSize.value = imageCacheManager.sizeBytes() }
    }
}