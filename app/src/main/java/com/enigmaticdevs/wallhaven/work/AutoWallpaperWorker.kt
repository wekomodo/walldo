package com.enigmaticdevs.wallhaven.work

import android.app.WallpaperManager
import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkManager.Companion.getInstance
import androidx.work.WorkerParameters
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionsDatabase
import com.enigmaticdevs.wallhaven.data.repository.AutoWallpaperRepository
import com.enigmaticdevs.wallhaven.data.repository.WallpaperRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.concurrent.TimeUnit

class AutoWallpaperWorker(
    appContext: Context,
    params: WorkerParameters,
    private val db: CollectionsDatabase,
    private val wallpaperRepository: WallpaperRepository,
    private val autoWallpaperRepository: AutoWallpaperRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            // 1. Pick the next wallpaper URL from your DB collection or API
            val imageUrl: String = ""

            // 2. Stream directly into WallpaperManager
            val wallpaperManager = WallpaperManager.getInstance(applicationContext)
            withContext(Dispatchers.IO) {
                URL(imageUrl).openStream()
            }.use { inputStream ->
                wallpaperManager.setStream(inputStream)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }


}


fun scheduleAutoWallpaper(
    context: Context,
    enabled: Boolean,
    intervalMinutes: Long, // Minimum allowed by Android is 15 minutes
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
        repeatInterval = maxOf(intervalMinutes, 15L),
        repeatIntervalTimeUnit = TimeUnit.MINUTES
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