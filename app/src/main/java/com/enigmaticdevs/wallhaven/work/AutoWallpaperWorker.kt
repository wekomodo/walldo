package com.enigmaticdevs.wallhaven.work

import android.app.WallpaperManager
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Build
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
import java.io.ByteArrayInputStream
import java.net.URL
import kotlin.math.roundToInt

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
            val filterState = globalFilterRepository.filters.first { it.isLoaded }
            val autoWallpaperState = autoWallpaperRepository.state.first { it.isLoaded }
            val targetScreen = autoWallpaperState.screenSelect
            val lastWallpaperId = autoWallpaperState.lastWallpaperId

            when (autoWallpaperState.source) {
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
                        ), page = 1
                    )
                    val imageUrl = imageData?.first?.path
                    setAutoWallpaper(
                        screen = targetScreen, context = applicationContext, imageUrl = imageUrl
                    )
                }

                "collections" -> {
                    Log.d("autowallpaperworker", "lastid : $lastWallpaperId")
                    val imageData =
                        collectionsRepository.getNextOrderedWallpaper("111", lastWallpaperId)
                            ?: collectionsRepository.getNextOrderedWallpaper("111", null)
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Log.w("AutoWallpaperWorker", "Worker stopped by OS. Reason code: $stopReason")
            }
            throw e // Always rethrow CancellationException in coroutines!
        }
    }


}

suspend fun setAutoWallpaper(
    screen: String,
    context: Context,
    imageUrl: String? = "",
) {
    // 2. Stream directly into WallpaperManager
    val wallpaperManager = WallpaperManager.getInstance(context)
    withContext(Dispatchers.IO) {
        // 1. Download network stream bytes into memory once
        val url = URL(imageUrl)
        val connection = url.openConnection() as java.net.HttpURLConnection
        connection.connectTimeout = 15000
        connection.readTimeout = 30000
        val imageBytes = connection.inputStream.use { it.readBytes() }

        // 2. Get the crop Rect using the separate function
        val cropRect = calculateCenterCropRect(context, imageBytes)

        // 3. Determine the correct system wallpaper flags
        val flag = when (screen) {
            "home" -> WallpaperManager.FLAG_SYSTEM
            "lock" -> WallpaperManager.FLAG_LOCK
            "both" -> WallpaperManager.FLAG_LOCK or WallpaperManager.FLAG_SYSTEM
            else -> WallpaperManager.FLAG_SYSTEM
        }

        // 4. Direct application using the exact crop rect
        ByteArrayInputStream(imageBytes).use { freshInputStream ->
            wallpaperManager.setStream(
                freshInputStream, cropRect, true, flag
            )
        }
    }
}


/**
 * Calculates a center-cropped Rect matching the device's screen aspect ratio.
 */
private fun calculateCenterCropRect(context: Context, imageBytes: ByteArray): Rect? {
    // Get screen dimensions for target aspect ratio
    val displayMetrics = context.resources.displayMetrics
    val targetWidth = displayMetrics.widthPixels
    val targetHeight = displayMetrics.heightPixels

    // Read only image dimensions from the byte array (no heavy memory decoding)
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)
    val sourceWidth = options.outWidth
    val sourceHeight = options.outHeight

    if (sourceWidth <= 0 || sourceHeight <= 0 || targetWidth <= 0 || targetHeight <= 0) {
        return null
    }

    // Calculate crop math
    val sourceRatio = sourceWidth.toFloat() / sourceHeight
    val targetRatio = targetWidth.toFloat() / targetHeight

    val cropWidth: Int
    val cropHeight: Int

    if (sourceRatio > targetRatio) {
        cropHeight = sourceHeight
        cropWidth = (sourceHeight * targetRatio).roundToInt()
    } else {
        cropWidth = sourceWidth
        cropHeight = (sourceWidth / targetRatio).roundToInt()
    }

    val left = (sourceWidth - cropWidth) / 2
    val top = (sourceHeight - cropHeight) / 2

    val rect = Rect(left, top, left + cropWidth, top + cropHeight)
    if (rect.isEmpty) {
        return null
    }
    return rect
}