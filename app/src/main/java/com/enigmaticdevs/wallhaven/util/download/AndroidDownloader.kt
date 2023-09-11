package com.enigmaticdevs.wallhaven.util.download

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.core.net.toUri
import java.io.File


@SuppressLint("UnspecifiedRegisterReceiverFlag")
class AndroidDownloader(
    private val context: Context
) : Downloader {

    private val downloadManager by lazy {
        context.getSystemService(DownloadManager::class.java)
    }

    override fun downloadFile(url: String, fileName: String): Long {
        val request = createRequest(url, fileName, true)
        return downloadManager.enqueue(request)
    }

    override fun downloadWallpaper(url: String, fileName: String): Long {
        val request = createRequest(url, fileName, false)
        return downloadManager.enqueue(request)
    }

    private fun createRequest(
        url: String,
        fileName: String,
        showCompletedNotification: Boolean
    ): DownloadManager.Request {
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
            .setNotificationVisibility(
                if (showCompletedNotification)
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                else
                    DownloadManager.Request.VISIBILITY_VISIBLE
            )
            .setTitle(fileName)
            .setDestinationInExternalPublicDir(
                destination,
                subPath
            )
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            request.setVisibleInDownloadsUi(true)
            request.allowScanningByMediaScanner()
        }
        return request
    }
}