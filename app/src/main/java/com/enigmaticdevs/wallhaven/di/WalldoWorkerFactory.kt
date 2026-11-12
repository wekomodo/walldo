package com.enigmaticdevs.wallhaven.di

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionSyncer
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionsDatabase
import com.enigmaticdevs.wallhaven.data.repository.AutoWallpaperRepository
import com.enigmaticdevs.wallhaven.data.repository.WallpaperRepository
import com.enigmaticdevs.wallhaven.work.AutoWallpaperWorker
import com.enigmaticdevs.wallhaven.work.CollectionSyncWorker
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlin.jvm.java

@ContributesBinding(AppScope::class)
@Inject
class WalldoWorkerFactory(
    private val syncer: CollectionSyncer,
    private val db: CollectionsDatabase,
    private val wallpaperRepository: WallpaperRepository,
    private val autoWallpaperRepository: AutoWallpaperRepository
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context, workerClassName: String, workerParameters: WorkerParameters
    ): ListenableWorker? = when (workerClassName) {
        CollectionSyncWorker::class.java.name -> CollectionSyncWorker(
            appContext,
            workerParameters,
            syncer
        )

        AutoWallpaperWorker::class.java.name -> AutoWallpaperWorker(
            appContext,
            workerParameters,
            db,
            wallpaperRepository,
            autoWallpaperRepository
        )

        else -> null
    }
}