package com.enigmaticdevs.wallhaven.domain.viewmodels

import androidx.lifecycle.ViewModel
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


@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey(WallpaperDetailViewModel::class)
class WallpaperDetailViewModel (
    private val repository: WallpaperRepository
) : ViewModel() {
    private val _wallpaper = MutableStateFlow<WallpaperDetail?>(null)
    val wallpaper = _wallpaper.asStateFlow()


    suspend fun getWallpaperDetails(id : String) {

        val networkRequest = repository.getWallpaperDetail(id)
        networkRequest?.let {
            _wallpaper.value = it
        }

    }



}