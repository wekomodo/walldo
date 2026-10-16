package com.enigmaticdevs.wallhaven.domain.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.enigmaticdevs.wallhaven.data.model.AuthenticateAPIkey
import com.enigmaticdevs.wallhaven.data.model.Wallpaper
import com.enigmaticdevs.wallhaven.data.model.WallpaperDetail
import com.enigmaticdevs.wallhaven.data.model.local.WallhavenAPIparams
import com.enigmaticdevs.wallhaven.data.remote.WallhavenAPI
import com.enigmaticdevs.wallhaven.domain.pagingsource.WallpaperPagingSource
import com.enigmaticdevs.wallhaven.ui.screens.homescreen.components.TAG
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow


@Inject
class WallpaperRepository(
    private val wallhavenAPI: WallhavenAPI
) {

    fun getWallpapers(
        params: WallhavenAPIparams,
        query : String
    ): Flow<PagingData<Wallpaper>> {

        return Pager(
            config = PagingConfig(
                pageSize = 24, enablePlaceholders = false
            ), pagingSourceFactory = {
                WallpaperPagingSource(
                    api = wallhavenAPI, apiParams = params, query = query
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