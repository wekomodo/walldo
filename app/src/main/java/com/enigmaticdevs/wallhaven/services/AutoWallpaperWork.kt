package com.enigmaticdevs.wallhaven.services


import android.app.WallpaperManager
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Rect
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.preference.PreferenceManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.enigmaticdevs.wallhaven.autoWallpaperdb.AutoWallpaperRepository
import com.enigmaticdevs.wallhaven.data.autowallpaper.models.AutoWallpaper
import com.enigmaticdevs.wallhaven.data.model.Params
import com.enigmaticdevs.wallhaven.data.model.Wallpaper
import com.enigmaticdevs.wallhaven.domain.favorite.FavoriteRepository
import com.enigmaticdevs.wallhaven.domain.repository.DownloadServiceRepository
import com.enigmaticdevs.wallhaven.domain.repository.MainRepository
import com.enigmaticdevs.wallhaven.util.screenHeight
import com.enigmaticdevs.wallhaven.util.screenWidth
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min


@HiltWorker
class AutoWallpaperWork
@AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: MainRepository,
    private val downloadServiceRepository: DownloadServiceRepository,
    private val autoWallpaperRepository: AutoWallpaperRepository,
    private val favoriteRepository: FavoriteRepository
) :
    CoroutineWorker(context, workerParams) {
    private var params: Params = Params("110", "111", "", "")
    private lateinit var preferences: SharedPreferences
    private var source: String = "random"
    private lateinit var notificationManager: NotificationManagerCompat
    private var purity: String = "100"
    private var category: String = "111"
    private var ratio: String = ""
    private var resolution: String = ""
    private var topRange: String = ""
    private lateinit var context : Context
    private var screen : String = "both"
    override suspend fun doWork(): Result {
        Log.d("workerWallpaper", "Chaleya")
        context = applicationContext
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val orientationFix = preferences.getBoolean("device_orientation_fix", false)
        notificationManager = NotificationManagerCompat.from(
            context
        )
        if (orientationFix) {
            if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                return Result.success()
        }
        source = inputData.getString("source").toString()
        purity = inputData.getString("purity").toString()
        resolution = inputData.getString("resolution").toString()
        ratio = inputData.getString("ratio").toString()
        category = inputData.getString("category").toString()
        screen = inputData.getString("screen").toString()
        params = Params(purity, category, ratio, resolution)
        Log.d("worker",source)
        when (source) {
            "random" -> {
                loadSourcePhoto("random", 1, "", params)
            }

            "popular" -> {
                topRange = preferences.getString("topRange", "1y").toString()
                loadSourcePhoto("toplist", (0..20).random(), topRange, params)
            }

            "favorite" -> {
                  loadFavoritePhoto()
            }
        }
        return Result.success()
    }

    private fun loadSourcePhoto(sort: String, page: Int, topRange: String, params: Params) {
        // GET PHOTO HERE
        CoroutineScope(IO).launch {
            try {
                val wallpaperList = repository.getSearchWallpapers("", sort, topRange, params, page)
                if (wallpaperList != null) {
                    val size = wallpaperList.data.size - 1
                    val data = wallpaperList.data[(0..size).random()]
                    val url = data.path
                    insertData(data)
                    val cropRect = getCropHintRect(
                        min(screenWidth, screenHeight).toDouble(),
                        max(screenWidth, screenHeight).toDouble(),
                        data.dimension_x.toDouble(),
                        data.dimension_y.toDouble()
                    )
                    downloadAndSetWallpaper(url,cropRect)

                }
            }
            catch (e : Exception){
                Log.d("Worker",e.message.toString())
            }

        }

    }

    private fun downloadAndSetWallpaper(url: String, cropRect: Rect?) {
        CoroutineScope(IO).launch {
            downloadServiceRepository.downloadFile(url)?.byteStream().use {
                it?.let{
                    when (screen) {
                        "home" -> WallpaperManager.getInstance(context)
                            .setStream(it, cropRect, true, WallpaperManager.FLAG_SYSTEM)

                        "lock" -> WallpaperManager.getInstance(context)
                            .setStream(it, cropRect, true, WallpaperManager.FLAG_LOCK)

                        "both" -> WallpaperManager.getInstance(context)
                            .setStream(it, cropRect, true)
                        else -> {}
                }
                }
            }
        }
    }

    private fun loadFavoritePhoto() {
        CoroutineScope(IO).launch {
            Log.d("databaseRead", "Chaleya")
           val list = favoriteRepository.getOnlyUrls()
            if(list.size>0)
            {

                val size: Int = list.size - 1
                val item = list[(0..size).random()]
                val cropRect = getCropHintRect(
                    min(screenWidth, screenHeight).toDouble(),
                    max(screenWidth, screenHeight).toDouble(),
                    item.dimension_x.toDouble(),
                    item.dimension_y.toDouble()
                )
                item.imageUrl?.let {
                    downloadAndSetWallpaper(it, cropRect)
                }
            }
        }
    }
    private  fun insertData(data: Wallpaper) {
        if(preferences.getBoolean("auto_wallpaper_history_switch",true))
            CoroutineScope(IO).launch {
                val autoWallImage = AutoWallpaper(0, data.id, data.thumbs.original, data.path, data.purity,data.dimension_x,data.dimension_y)
                autoWallpaperRepository.addImage(autoWallImage)
            }
    }
    private fun getCropHintRect(
        screenWidth: Double,
        screenHeight: Double,
        photoWidth: Double,
        photoHeight: Double
    ): Rect? {
        if (screenWidth > 0 && screenHeight > 0 && photoWidth > 0 && photoHeight > 0) {
            val screenAspectRatio = screenWidth / screenHeight
            val photoAspectRatio = photoWidth / photoHeight
            val resizeFactor = if (screenAspectRatio >= photoAspectRatio) {
                photoWidth / screenWidth
            } else {
                photoHeight / screenHeight
            }
            val newWidth = screenWidth * resizeFactor
            val newHeight = screenHeight * resizeFactor
            val newLeft = (photoWidth - newWidth) / 2
            val newTop = (photoHeight - newHeight) / 2
            val newRight = newWidth + newLeft

            val rect = Rect(newLeft.toInt(), newTop.toInt(), newRight.toInt(), (newHeight + newTop).toInt())
            return if (rect.isValid()) rect else null
        }
        return null
    }

    private fun Rect.isValid(): Boolean {
        return right >= 0 && left in 0..right && bottom >= 0 && top in 0..bottom
    }

}
