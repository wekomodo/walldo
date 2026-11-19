package com.enigmaticdevs.wallhaven.work

import android.app.WallpaperManager
import android.content.Context
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkManager.Companion.getInstance
import androidx.work.WorkerParameters
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionsDatabase
import com.enigmaticdevs.wallhaven.data.repository.AutoWallpaperInterval
import com.enigmaticdevs.wallhaven.data.repository.AutoWallpaperRepository
import com.enigmaticdevs.wallhaven.data.repository.CollectionsRepository
import com.enigmaticdevs.wallhaven.data.repository.WallpaperRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.concurrent.TimeUnit

class AutoWallpaperWorker(
    appContext: Context,
    params: WorkerParameters,
    private val db: CollectionsDatabase,
    private val wallpaperRepository: WallpaperRepository,
    private val collectionsRepository: CollectionsRepository,
    private val autoWallpaperRepository: AutoWallpaperRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            // 1. Pick the next wallpaper URL from your DB collection or API
            val filters= autoWallpaperRepository.state.first()
            val autoWallpaperState = autoWallpaperRepository.state.first()
            val targetScreen = autoWallpaperState.screenSelect
            val lastWallpaperId = autoWallpaperState.lastWallpaperId
            val imageUrl: String = ""

            when(autoWallpaperState.source){
                "popular" -> {
                    //if the user selected popular

                }
                "collections" -> {
                    val imageUrl = collectionsRepository.getNextOrderedWallpaper("111","")
                }
                else -> {}
            }



            scheduleAutoWallpaper(
                context = applicationContext,
                enabled = autoWallpaperState.switch,
                interval = AutoWallpaperInterval.fromName(autoWallpaperState.interval) ,
                onlyOnWifi = autoWallpaperState.onWifi,
                onlyWhenCharging = autoWallpaperState.onCharging,
                onlyWhenIdle = autoWallpaperState.onIdle,
                batteryNotLow = autoWallpaperState.pauseOnLowBattery,
                storageNotLow = autoWallpaperState.ifLowOnStorage
            )





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


fun scheduleAutoWallpaper(
    context: Context,
    enabled: Boolean,
    interval: AutoWallpaperInterval, // Minimum allowed by Android is 15 minutes
    onlyOnWifi: Boolean,
    onlyWhenCharging: Boolean,
    onlyWhenIdle: Boolean,
    batteryNotLow: Boolean,
    storageNotLow: Boolean
) {
    val workManager = getInstance(context)

    if (!enabled) {
        workManager.cancelUniqueWork("auto_wallpaper")
        return
    }

    val constraints = Constraints.Builder()
        .setRequiredNetworkType(
            if (onlyOnWifi) NetworkType.UNMETERED else NetworkType.CONNECTED
        )
        .setRequiresCharging(onlyWhenCharging)
        .setRequiresDeviceIdle(onlyWhenIdle)
        .setRequiresBatteryNotLow(batteryNotLow)
        .setRequiresStorageNotLow(storageNotLow)
        .build()

    val request = PeriodicWorkRequestBuilder<AutoWallpaperWorker>(
        repeatInterval = maxOf(interval.value, 15L),
        repeatIntervalTimeUnit = interval.timeUnit
    )
        .setConstraints(constraints)
        .build()

    // UPDATE applies the new interval/constraints immediately without resetting the timer unnecessarily
    workManager.enqueueUniquePeriodicWork(
        "auto_wallpaper",
        ExistingPeriodicWorkPolicy.UPDATE,
        request
    )
}