package com.enigmaticdevs.wallhaven.domain.repository

import com.enigmaticdevs.wallhaven.data.remote.WallhavenAPI
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject


@Inject
class WallpaperRepository(
    private val wallhavenAPI: WallhavenAPI
) {
}