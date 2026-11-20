package com.enigmaticdevs.wallhaven.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.enigmaticdevs.wallhaven.MainActivity
import com.enigmaticdevs.wallhaven.MyApp
import com.enigmaticdevs.wallhaven.work.AutoWallpaperWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class NextWallpaperTileService : TileService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var observeJob: Job? = null

    private val globalFlagsRepository by lazy {
        (application as MyApp).appGraph.globalFlagsRepository
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "auto_wallpaper_now"
    }

    override fun onStartListening() {
        super.onStartListening()
        observeTileState()
    }

    @SuppressLint("StartActivityAndCollapseDeprecated")
    override fun onClick() {
        super.onClick()
        if (!globalFlagsRepository.proStatus.value) {
            // Optional: open your app directly to the Pro paywall if tapped
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("open_pro_paywall", true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                startActivityAndCollapse(pendingIntent)
            } else {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(intent)
            }
            return
        }
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

        updateTile(isPro = true, isRunning = true)
    }

    private fun observeTileState() {
        observeJob?.cancel()
        observeJob = serviceScope.launch {
            combine(
                globalFlagsRepository.proStatus,
                WorkManager.getInstance(applicationContext)
                    .getWorkInfosForUniqueWorkFlow(UNIQUE_WORK_NAME)
            ) { isPro, infos ->
                val isRunning = infos.any {
                    it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED
                }
                isPro to isRunning
            }.collect { (isPro, isRunning) ->
                updateTile(isPro = isPro, isRunning = isRunning)
            }
        }
    }

    private fun updateTile(isPro: Boolean, isRunning: Boolean) {
        val tile = qsTile ?: return
        when {
            !isPro -> {
                tile.state = Tile.STATE_UNAVAILABLE
                tile.label = "Next Wallpaper"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = "Pro"
                }
            }

            isRunning -> {
                tile.state = Tile.STATE_ACTIVE
                tile.label = "Changing..."
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = "Walldo"
                }
            }

            else -> {
                tile.state = Tile.STATE_INACTIVE
                tile.label = "Next Wallpaper"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = "Tap to skip"
                }
            }
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

