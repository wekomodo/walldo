package com.enigmaticdevs.wallhaven.util.download

import android.app.DownloadManager
import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.core.net.toUri
import java.io.File


class AndroidDownloader(
    private val context: Context
) : Downloader {

    private val downloadManager by lazy {
        context.getSystemService(DownloadManager::class.java)
    }

    override fun downloadFile(url: String, fileName: String): Long {

        val destination = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Environment.DIRECTORY_PICTURES
        } else {
            Environment.DIRECTORY_DOWNLOADS
        }
        val subPath = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            "$WALLDO_DIRECTORY${File.separator}$fileName"
        } else {
            fileName
        }
        val request = DownloadManager.Request(url.toUri())
            .setMimeType("image/jpeg")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle(fileName)
            .setDestinationInExternalPublicDir(
                destination,
                subPath
            )
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            request.setVisibleInDownloadsUi(true)
            request.allowScanningByMediaScanner()
        }
        return downloadManager.enqueue(request)
    }


}