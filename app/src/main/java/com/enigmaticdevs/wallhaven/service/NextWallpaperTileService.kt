package com.enigmaticdevs.wallhaven.service

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.enigmaticdevs.wallhaven.work.AutoWallpaperWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class NextWallpaperTileService : TileService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var observeJob: Job? = null

    companion object {
        private const val UNIQUE_WORK_NAME = "auto_wallpaper_now"
    }

    override fun onStartListening() {
        super.onStartListening()
        observeWorkState()
    }

    override fun onClick() {
        super.onClick()
        triggerNextWallpaper()
    }

    private fun triggerNextWallpaper() {
        // Only require basic network (ignore user's "only on Wi-Fi / charging" periodic constraints)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<AutoWallpaperWorker>()
            .setConstraints(constraints)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            ExistingWorkPolicy.KEEP, // Ignore accidental double-taps while already changing
            request
        )

        updateTile(isRunning = true)
    }

    private fun observeWorkState() {
        observeJob?.cancel()
        observeJob = serviceScope.launch {
            WorkManager.getInstance(applicationContext)
                .getWorkInfosForUniqueWorkFlow(UNIQUE_WORK_NAME)
                .collect { infos ->
                    val isRunning = infos.any {
                        it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED
                    }
                    updateTile(isRunning = isRunning)
                }
        }
    }

    private fun updateTile(isRunning: Boolean) {
        val tile = qsTile ?: return
        tile.state = if (isRunning) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = if (isRunning) "Changing..." else "Next Wallpaper"
        // Optional on Android 10+ (API 29+):
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            tile.subtitle = if (isRunning) "Walldo" else "Tap to skip"
        }
        tile.updateTile()
    }

    override fun onStopListening() {
        observeJob?.cancel()
        super.onStopListening()
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}