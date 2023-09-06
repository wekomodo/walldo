package com.enigmaticdevs.wallhaven.util.download

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import androidx.collection.LongSparseArray
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class DownloadCompletedReceiver : BroadcastReceiver() {
    private val downloadActionMap = LongSparseArray<DownloadAction>()
    private lateinit var downloadManager: DownloadManager
    override fun onReceive(context: Context?, intent: Intent?) {
        val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L) ?: 0L
        val downloadAction = downloadActionMap.get(id) ?: return
        val query = DownloadManager.Query().apply { setFilterById(id) }
        downloadManager = context?.getSystemService(DownloadManager::class.java)!!

        val cursor = downloadManager.query(query)

        if (!cursor.moveToFirst()) {
            onError(cursor, id, downloadAction, "Cursor empty, this shouldn't happened",context)
            return
        }

        val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
        if (cursor.getInt(statusIndex) != DownloadManager.STATUS_SUCCESSFUL) {
            onError(cursor, id, downloadAction, "Download Failed", context)
        } else {
            onSuccess(
                cursor,
                id,
                downloadAction,
                downloadManager.getUriForDownloadedFile(id),
                context
            )
        }
    }

    private fun onSuccess(
        cursor: Cursor,
        id: Long,
        downloadAction: DownloadAction,
        uri: Uri,
        context: Context
    ) {
        cursor.close()
        downloadActionMap.remove(id)

        val localIntent = Intent(ACTION_DOWNLOAD_COMPLETE).apply {
            putExtra(DOWNLOAD_STATUS, STATUS_SUCCESSFUL)
            putExtra(DATA_ACTION, downloadAction)
            putExtra(DATA_URI, uri)
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent)
    }

    private fun onError(
        cursor: Cursor,
        id: Long,
        downloadAction: DownloadAction,
        errorMessage: String,
        context: Context
    ) {
        cursor.close()
        downloadManager.remove(id)
        downloadActionMap.remove(id)

        val localIntent = Intent(ACTION_DOWNLOAD_COMPLETE).apply {
            putExtra(DOWNLOAD_STATUS, STATUS_CANCELLED)
            putExtra(DATA_ACTION, downloadAction)
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent)
        error("onError: $errorMessage")
    }

}