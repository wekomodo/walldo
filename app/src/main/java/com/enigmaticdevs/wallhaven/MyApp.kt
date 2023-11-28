package com.enigmaticdevs.wallhaven

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject


@HiltAndroidApp
class MyApp : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    override fun onCreate() {
        super.onCreate()
       // DynamicColors.applyToActivitiesIfAvailable(this)
    }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder().setMinimumLoggingLevel(Log.DEBUG)
            .setWorkerFactory(workerFactory)
            .build()
}

    /*class AutoWallpaperWorkerFactory @Inject constructor(private val repository: MainRepository) : WorkerFactory() {
        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters
        ): ListenableWorker = AutoWallpaperWork(appContext, workerParameters, repository)

    }*/