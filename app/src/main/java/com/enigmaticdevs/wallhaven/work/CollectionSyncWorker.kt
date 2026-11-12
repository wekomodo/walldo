package com.enigmaticdevs.wallhaven.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionSyncer
import com.enigmaticdevs.wallhaven.data.local.collections.SyncResult

class CollectionSyncWorker(
    appContext: Context,
    params: WorkerParameters,
    private val syncer: CollectionSyncer
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        // 1. Push pending ops (creates, deletes, adds, removes)
        when (syncer.sync()) {
            is SyncResult.Failed, SyncResult.SessionExpired -> return Result.retry()
            SyncResult.NeedsLogin, SyncResult.NeedsCloudflare -> return Result.failure()
            SyncResult.Skipped, is SyncResult.Done -> Unit
        }

        // 2. Pull any remaining pages for incomplete collections
        val allPagesPulled = syncer.pullRemainingPages()
        return if (allPagesPulled) Result.success() else Result.retry()
    }
}

fun enqueueCollectionSync(context: Context) {
    val request = OneTimeWorkRequestBuilder<CollectionSyncWorker>()
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        )
        .build()

    WorkManager.getInstance(context).enqueueUniqueWork(
        "collection_sync",
        ExistingWorkPolicy.APPEND_OR_REPLACE,
        request
    )
}