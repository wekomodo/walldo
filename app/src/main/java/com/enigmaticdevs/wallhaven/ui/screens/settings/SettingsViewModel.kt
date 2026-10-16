package com.enigmaticdevs.wallhaven.ui.screens.settings

import android.content.Context
import android.util.Log
import androidx.activity.contextaware.ContextAware
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import com.enigmaticdevs.wallhaven.domain.repository.SettingsRepository
import com.enigmaticdevs.wallhaven.domain.repository.WallpaperRepository
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


data class SettingsUiState(
    val theme: ThemeMode = ThemeMode.System,
    val apiKey: String = "",
    val wallpaperSetter: String = "native",
    val isLoaded: Boolean = false,
    val cacheSizeBytes: Long = 0L
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


@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey(SettingsViewModel::class)
class SettingsViewModel(
    private val imageCacheManager: ImageCacheManager,
    private val repository: SettingsRepository,
) : ViewModel() {

    private val TAG = "SettingsViewModel"
    private val cacheSize = MutableStateFlow(0L)
    private val _keyCheck = MutableStateFlow(KeyCheck.Idle)
    val keyCheck: StateFlow<KeyCheck> = _keyCheck.asStateFlow()
    val uiState: StateFlow<SettingsUiState> = combine(
        repository.themeFlow,
        repository.wallpaperSetterFlow,
        repository.apiKeyFlow,
        cacheSize
    ) { theme, wallpaperSetter, key, size ->
        SettingsUiState(
            theme = ThemeMode.from(theme),
            apiKey = key,
            wallpaperSetter = wallpaperSetter,
            cacheSizeBytes = size,
            isLoaded = true
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

    fun updateTheme(mode: ThemeMode) {
        viewModelScope.launch { repository.updateTheme(mode.name) }
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

    fun resetKeyCheck() {
        _keyCheck.value = KeyCheck.Idle
    }

    fun updateApiKey(newKey: String) {
        viewModelScope.launch { repository.updateApiKey(newKey.trim()) }
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