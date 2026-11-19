package com.enigmaticdevs.wallhaven.work

import android.app.WallpaperManager
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionsDatabase
import com.enigmaticdevs.wallhaven.data.remote.model.local.WallhavenAPIparams
import com.enigmaticdevs.wallhaven.data.remote.model.objects.Sorting
import com.enigmaticdevs.wallhaven.data.remote.model.objects.TopRange
import com.enigmaticdevs.wallhaven.data.repository.AutoWallpaperRepository
import com.enigmaticdevs.wallhaven.data.repository.CollectionsRepository
import com.enigmaticdevs.wallhaven.data.repository.GlobalFilterRepository
import com.enigmaticdevs.wallhaven.data.repository.WallpaperRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.net.URL

class AutoWallpaperWorker(
    appContext: Context,
    params: WorkerParameters,
    private val db: CollectionsDatabase,
    private val wallpaperRepository: WallpaperRepository,
    private val globalFilterRepository: GlobalFilterRepository,
    private val collectionsRepository: CollectionsRepository,
    private val autoWallpaperRepository: AutoWallpaperRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {

            // 1. Pick the next wallpaper URL from your DB collection or API
            val filters= autoWallpaperRepository.state.first()
            val filterState = globalFilterRepository.filters.first()
            val autoWallpaperState = autoWallpaperRepository.state.first()
            val targetScreen = autoWallpaperState.screenSelect
            val lastWallpaperId = autoWallpaperState.lastWallpaperId
            val imageUrl: String = ""

            when(autoWallpaperState.source){
                "popular" -> {
                    //if the user selected popular
                    val imageData = wallpaperRepository.getAutoWallpaperPopular(
                        params = WallhavenAPIparams(
                            purity = filterState.purity,
                            category = filterState.category,
                            ratio = filterState.ratio,
                            resolution = filterState.resolution,
                            topRange = TopRange.oneMonth,
                            sorting = Sorting.topList
                        ),
                        page = 1
                    )

                }
                "collections" -> {
                    val imageUrl = collectionsRepository.getNextOrderedWallpaper("111","")
                }
                else -> {}
            }

            setAutoWallpaper(applicationContext,imageUrl)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }


}

suspend fun setAutoWallpaper(
    context : Context,
    imageUrl : String,
){
    // 2. Stream directly into WallpaperManager
    val wallpaperManager = WallpaperManager.getInstance(context)
    withContext(Dispatchers.IO) {
        URL(imageUrl).openStream()
    }.use { inputStream ->
        wallpaperManager.setStream(inputStream)
    }
}
