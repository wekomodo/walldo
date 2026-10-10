package com.enigmaticdevs.wallhaven.domain.viewmodels

import android.util.LruCache
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enigmaticdevs.wallhaven.data.model.Wallpaper
import com.enigmaticdevs.wallhaven.data.model.WallpaperDetail
import com.enigmaticdevs.wallhaven.di.AppGraph
import com.enigmaticdevs.wallhaven.domain.repository.WallpaperRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey(WallpaperDetailViewModel::class)
class WallpaperDetailViewModel (
    private val repository: WallpaperRepository
) : ViewModel() {
    private val _wallpaper = MutableStateFlow<WallpaperDetail?>(null)
    val wallpaper = _wallpaper.asStateFlow()

    // 1. Create a cache that holds a maximum of 30 wallpapers in memory
    private val wallpaperCache = LruCache<String, WallpaperDetail>(30)


    fun getWallpaperDetails(id : String) {

        wallpaperCache.get(id)?.let { cachedWallpaper ->
            _wallpaper.value = cachedWallpaper
            return
        }
        viewModelScope.launch {
            _wallpaper.value = null
            val networkRequest = repository.getWallpaperDetail(id)
            networkRequest?.let {
                // caching the new request
                wallpaperCache.put(id, it)
                _wallpaper.value = it
            }
        }
    }



}