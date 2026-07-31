package com.enigmaticdevs.wallhaven.domain.repository

import com.enigmaticdevs.wallhaven.data.model.Wallpapers
import com.enigmaticdevs.wallhaven.data.model.local.WallhavenAPIparams
import com.enigmaticdevs.wallhaven.data.remote.WallhavenAPI
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject


@Inject
class WallpaperRepository(
    private val wallhavenAPI: WallhavenAPI
) {

    suspend fun getWallpapersBySort(params : WallhavenAPIparams
    ) : Result<Wallpapers> = runCatching{
         wallhavenAPI.getWallpapersBySort(
            params.sorting,
            params.purity,
            params.category,
            params.topRange,
            params.ratio,
            params.resolution,
            params.page
        )

    }
}