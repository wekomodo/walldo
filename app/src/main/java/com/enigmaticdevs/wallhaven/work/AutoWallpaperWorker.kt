package com.enigmaticdevs.wallhaven.work

import android.app.WallpaperManager
import android.content.Context
import android.util.Log
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
                    val imageUrl = imageData?.first?.path
                    setAutoWallpaper(
                        screen = targetScreen,
                        context = applicationContext,
                        imageUrl = imageUrl
                    )
                }
                "collections" -> {
                    Log.d("autowallpaperworker", "lastid : $lastWallpaperId")
                    val imageData =
                        collectionsRepository.getNextOrderedWallpaper("111", lastWallpaperId)
                    autoWallpaperRepository.updateLastWallpaperId(imageData?.wallpaperId)
                    setAutoWallpaper(
                        screen = targetScreen,
                        context = applicationContext,
                        imageUrl = imageData?.fullUrl
                    )
                }
            }
            Log.d("autowallpaperworker", "RAN")
            Result.success()
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.retry()
        }
    }


}

suspend fun setAutoWallpaper(
    screen: String,
    context : Context,
    imageUrl: String? = "",
){
    // 2. Stream directly into WallpaperManager

    val wallpaperManager = WallpaperManager.getInstance(context)
    withContext(Dispatchers.IO) {
        URL(imageUrl).openStream()
    }.use { inputStream ->
        when (screen) {
            "home" -> {
                wallpaperManager.setStream(inputStream, null, true, WallpaperManager.FLAG_SYSTEM)

            }

            "lock" -> {
                wallpaperManager.setStream(inputStream, null, true, WallpaperManager.FLAG_LOCK)

            }

            "both" -> {
                wallpaperManager.setStream(
                    inputStream,
                    null,
                    true,
                    WallpaperManager.FLAG_LOCK or WallpaperManager.FLAG_SYSTEM
                )

            }
        }
    }
}
