package com.enigmaticdevs.wallhaven.domain.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingSourceFactory
import com.enigmaticdevs.wallhaven.data.model.Wallpaper
import com.enigmaticdevs.wallhaven.data.model.WallpaperDetail
import com.enigmaticdevs.wallhaven.data.model.Wallpapers
import com.enigmaticdevs.wallhaven.data.model.local.WallhavenAPIparams
import com.enigmaticdevs.wallhaven.data.remote.WallhavenAPI
import com.enigmaticdevs.wallhaven.domain.WallpaperPagingSource
import com.enigmaticdevs.wallhaven.ui.screens.homescreen.TAG
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow


@Inject
class WallpaperRepository(
    private val wallhavenAPI: WallhavenAPI
) {

    fun getWallpapersBySort(
        params: WallhavenAPIparams
    ): Flow<PagingData<Wallpaper>> {

        return Pager(
            config = PagingConfig(
                pageSize = 24, enablePlaceholders = false
            ), pagingSourceFactory = {
                WallpaperPagingSource(
                    api = wallhavenAPI, apiParams = params
                )
            }).flow

    }

    suspend fun getWallpaperDetail(id: String): WallpaperDetail? {

        runCatching {
            wallhavenAPI.getWallpaper(id)
        }.onSuccess {
            return it
        }.onFailure { exception ->
            exception.message?.let { Log.e("$TAG Error", it) }
            return null
        }
        return null
    }
}