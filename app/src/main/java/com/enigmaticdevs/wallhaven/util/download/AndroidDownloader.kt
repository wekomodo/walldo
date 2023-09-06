package com.enigmaticdevs.wallhaven.util.download

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import androidx.collection.LongSparseArray
import androidx.core.net.toUri
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.io.File

@SuppressLint("UnspecifiedRegisterReceiverFlag")
class AndroidDownloader(
    private val context: Context,
    private val fileName: String
) : Downloader {
    val SUBDIRECTORY = "Walldo"
    private val downloadManager = context.getSystemService(DownloadManager::class.java)

    init {
        val downloadStatusReceiver = DownloadCompletedReceiver()
        val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        context.registerReceiver(downloadStatusReceiver, intentFilter)
    }

    override fun downloadFile(url: String): Long {
        val request = DownloadManager.Request(url.toUri())
            .setMimeType("image/jpeg")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle(fileName)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_PICTURES,
                "$SUBDIRECTORY${File.separator}$fileName"
            )
        return downloadManager.enqueue(request)
    }

}