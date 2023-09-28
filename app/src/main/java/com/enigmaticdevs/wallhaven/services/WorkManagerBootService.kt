package com.enigmaticdevs.wallhaven.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.enigmaticdevs.wallhaven.data.model.Params
import java.util.concurrent.TimeUnit

class WorkManagerBootService : BroadcastReceiver() {
    private lateinit var preference: SharedPreferences
    private var params: Params = Params("100", "111", "", "")
    private val AUTO_WALLPAPER_WORK_ID = "AUTO_WALLPAPER"
    override fun onReceive(context: Context, intent: Intent?) {
        preference = PreferenceManager.getDefaultSharedPreferences(context)
        val autoWallpaper = preference.getBoolean("auto_wallpaper", false)
        if (autoWallpaper) {
            val wifiMust = preference.getBoolean("only_on_wifi", false)
            val chargingMust = preference.getBoolean("only_on_charging", false)
            val idleMust = preference.getBoolean("only_when_idle", false)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(if (wifiMust) NetworkType.UNMETERED else NetworkType.NOT_REQUIRED)
                .setRequiresCharging(chargingMust)
                .setRequiresDeviceIdle(idleMust)
            val interval = preference.getString("auto_wall_interval", "1440")?.toLong() ?: 1440
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
            WorkManager.getInstance(context).cancelUniqueWork(AUTO_WALLPAPER_WORK_ID)
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                AUTO_WALLPAPER_WORK_ID,
                ExistingPeriodicWorkPolicy.UPDATE,  //Existing Periodic Work policy
                workRequest //work request
            )
        }
    }
}