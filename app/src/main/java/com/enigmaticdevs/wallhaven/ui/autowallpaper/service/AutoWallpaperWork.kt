package com.enigmaticdevs.wallhaven.ui.autowallpaper.service


import android.app.WallpaperManager
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.widget.ImageView
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.preference.PreferenceManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.enigmaticdevs.wallhaven.data.model.Params
import com.enigmaticdevs.wallhaven.domain.repository.MainRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.io.IOException


@HiltWorker
class AutoWallpaperWork
@AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: MainRepository
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
    override suspend fun doWork(): Result {
        Log.d("workerWallpaper", "Chaleya")
        preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val orientationFix = preferences.getBoolean("device_orientation_fix", false)
        notificationManager = NotificationManagerCompat.from(
            applicationContext
        )
        if (orientationFix) {
            if (applicationContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
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
            val wallpaperList = repository.getSearchWallpapers("", sort, topRange, params, page)
            if (wallpaperList != null) {
                val size = wallpaperList.data.size - 1
                val data = wallpaperList.data[(0..size).random()]
                val photo = ImageView(applicationContext)
            /*    Picasso.get().load(data.path)
                    .resize(
                        Resources.getSystem().displayMetrics.widthPixels,
                        Resources.getSystem().displayMetrics.heightPixels
                    )
                    .centerCrop()
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(photo, object : com.squareup.picasso.Callback {
                        override fun onSuccess() {
                            if (photo.drawable != null) {
                                // notificationId is a unique int for each notification that you must define
                                notificationManager.cancel(456)
                                setWallpaper(photo)
                                Log.i("Wallpaper", "Set")
                            }
                        }

                        override fun onError(e: Exception?) {
                            Log.d("Error", "failed to make drawable")
                        }
                    })*/
            }

        }
    }

    private fun setWallpaper(photo: ImageView) {
        val screen = preferences.getString("auto_wall_screen", "both").toString()
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
}
