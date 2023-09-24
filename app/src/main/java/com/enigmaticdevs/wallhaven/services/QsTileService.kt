package com.enigmaticdevs.wallhaven.services

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.enigmaticdevs.wallhaven.R
import com.enigmaticdevs.wallhaven.data.model.Params
import com.enigmaticdevs.wallhaven.ui.autowallpaper.AutoWallpaperSettings
import java.util.concurrent.TimeUnit

class QsTileService : TileService() {

    private  lateinit var preference: SharedPreferences
    private var params: Params = Params("100", "111", "", "")
    private val AUTO_WALLPAPER_WORK_ID = "AUTO_WALLPAPER"

        override fun onStartListening() {
            super.onStartListening()
            qsTile?.apply {
                state = Tile.STATE_ACTIVE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    subtitle = "next in line"
                }
                updateTile()
            }
        }


        override fun onClick() {
        super.onClick()
        preference = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val autoWallpaper = preference.getBoolean("auto_wallpaper",false)
        if(autoWallpaper){
            val builder = NotificationCompat.Builder(
                applicationContext, "channelId"
            )
                .setContentTitle("Walldo")
                .setContentText("Changing Wallpaper")
                .setSmallIcon(R.drawable.ic_app_icon)
                .setPriority(NotificationCompat.PRIORITY_LOW)

            val notificationManager = NotificationManagerCompat.from(
                applicationContext
            )
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(456, builder.build())
            }
            val wifiMust = preference.getBoolean("only_on_wifi", false)
            val chargingMust = preference.getBoolean("only_on_charging", false)
            val idleMust = preference.getBoolean("only_when_idle", false)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(if (wifiMust) NetworkType.UNMETERED else NetworkType.NOT_REQUIRED)
                .setRequiresCharging(chargingMust)
                .setRequiresDeviceIdle(idleMust)
            val interval = preference.getString("auto_wall_interval", "1440")!!.toLong()
            val data: Data.Builder = Data.Builder()
            data.putString("source", preference.getString("wallpaper_source", "random"))
            data.putString("purity", params.purity)
            data.putString("category", params.category)
            data.putString("ratio", params.ratio)
            data.putString("resolution", params.resolution)
            data.putString("screen", preference.getString("auto_wall_screen", "both"))
            val workRequest =
                PeriodicWorkRequest.Builder(
                    AutoWallpaperWork::class.java,
                    interval * 60 * 1000,
                    TimeUnit.MILLISECONDS
                ).setInputData(data.build()).setConstraints(constraints.build()).build()
            WorkManager.getInstance(this).cancelUniqueWork(AUTO_WALLPAPER_WORK_ID)
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                AUTO_WALLPAPER_WORK_ID,
                ExistingPeriodicWorkPolicy.UPDATE,  //Existing Periodic Work policy
                workRequest //work request
            )
        }
        else {
            val intent = Intent(this, AutoWallpaperSettings::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}