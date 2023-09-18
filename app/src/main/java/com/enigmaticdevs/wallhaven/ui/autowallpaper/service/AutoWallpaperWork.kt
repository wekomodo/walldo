package com.enigmaticdevs.wallhaven.ui.autowallpaper.service


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
import com.enigmaticdevs.wallhaven.data.download.DownloadServiceRepository
import com.enigmaticdevs.wallhaven.data.model.Params
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
    private val downloadServiceRepository: DownloadServiceRepository
) :
    CoroutineWorker(context, workerParams) {
    private var params: Params = Params("110", "111", "", "")
    private lateinit var preferences: SharedPreferences
    private lateinit var source: String
    private lateinit var notificationManager: NotificationManagerCompat
    private var purity: String = "100"
    private var category: String = "111"
    private var ratio: String = ""
    private var resolution: String = ""
    private var topRange: String = ""
    private lateinit var context : Context
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
        source = preferences.getString("wallpaper_source", "random").toString()
        purity = inputData.getString("purity").toString()
        resolution = inputData.getString("resolution").toString()
        ratio = inputData.getString("ratio").toString()
        category = inputData.getString("category").toString()
        params = Params(purity, category, ratio, resolution)
        source = preferences.getString("wallpaper_source", "random").toString()
        when (source) {
            "random" -> {
                loadSourcePhoto("random", 1, "", params)
            }

            "toplist" -> {
                topRange = preferences.getString("topRange", "1y").toString()
                loadSourcePhoto("toplist", (0..20).random(), topRange, params)
            }

            "favorite" -> {
                //  loadFavoritePhoto()
            }
        }
        return Result.success()
    }

    private fun loadSourcePhoto(sort: String, page: Int, topRange: String, params: Params) {
        // GET PHOTO HERE
        CoroutineScope(IO).launch {
            try {
                val wallpaperList = repository.getSearchWallpapers("", sort, topRange, params, page)
                if (wallpaperList!=null) {
                    val size = wallpaperList.data.size - 1
                    val data = wallpaperList.data[(0..size).random()]
                    val url = data.path
                    val cropRect = getCropHintRect(
                        min(screenWidth, screenHeight).toDouble(),
                        max(screenWidth, screenHeight).toDouble(),
                        data.dimension_x.toDouble(),
                        data.dimension_y.toDouble())
                    downloadServiceRepository.downloadFile(url)?.byteStream().use {
                        WallpaperManager.getInstance(context).setStream(it,cropRect,true)
                    }
                }
            }
            catch (e : Exception){
                Log.d("Worker",e.message.toString())
            }

        }

    }

    /*private fun setWallpaper(photo: ImageView) {
        val screen = preferences.getString("auto_wall_screen", "both")
        CoroutineScope(IO).launch {
            val wallpaperMgr = WallpaperManager.getInstance(applicationContext)
            val myBitmap: Bitmap = (photo.drawable as BitmapDrawable).bitmap
            try {
                when (screen) {
                    "home" -> {
                        Log.d("WallpaperSet ", "HomeScreen")
                        wallpaperMgr.setBitmap(myBitmap, null, false, WallpaperManager.FLAG_SYSTEM)
                    }

                    "lock" -> {
                        Log.d("WallpaperSet ", "Lockscreen")
                        wallpaperMgr.setBitmap(myBitmap, null, false, WallpaperManager.FLAG_LOCK)
                    }

                    "both" -> {
                        wallpaperMgr.setBitmap(myBitmap)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
*/
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
