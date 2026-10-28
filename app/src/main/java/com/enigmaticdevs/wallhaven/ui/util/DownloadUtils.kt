package com.enigmaticdevs.wallhaven.ui.util

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.telephony.mbms.DownloadRequest
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.enigmaticdevs.wallhaven.navigation.Screen
import com.enigmaticdevs.wallhaven.navigation.Screen.WallpaperDetail
import java.io.File
import java.util.jar.Manifest


sealed interface DownloadUtils {
    data class Started(val id : Long) : DownloadUtils
    data class AlreadyDownloaded(val file : File) : DownloadUtils
}
fun downloadWallpaper(context: Context, url: String, fileName: String) : DownloadUtils {

    val extension = MimeTypeMap.getFileExtensionFromUrl(url).ifEmpty { "jpg" }
    val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "image/jpeg"
    val fullFileName = "walldo_$fileName.$extension"

    val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
    val file = File(picturesDir, "Walldo/$fullFileName")
    if (file.exists()) return DownloadUtils.AlreadyDownloaded(file)

    val request = DownloadManager.Request(url.toUri()).apply {
        setTitle(fullFileName)
        setDescription("Saved to Pictures/Walldo")

        // 2. CRUCIAL: Explicitly set the MIME type so the notification knows it's an image!
        setMimeType(mimeType)

        // 3. Tell MediaScanner to index it immediately (helps on older Android versions)
        allowScanningByMediaScanner()

        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "Walldo/$fullFileName")
        setAllowedOverMetered(true)
        setAllowedOverRoaming(true)
    }

    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    return DownloadUtils.Started(downloadManager.enqueue(request))
}

@Composable
fun DownloadListener(downloadId: Long, onDownloadComplete: (Uri?) -> Unit) {
    val context = LocalContext.current
    val currentCallback by rememberUpdatedState(onDownloadComplete)

    DisposableEffect(downloadId) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                if (id == downloadId) {
                    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    currentCallback(downloadManager.getUriForDownloadedFile(downloadId))
                }
            }
        }

        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_EXPORTED
        )

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }
}

@Composable
fun rememberDownloadStarter(): (download: () -> Unit) -> Unit {

    val context = LocalContext.current
    var pending by remember { mutableStateOf<(() -> Unit)?>(null) }
    val storagePermission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE


    //android 9 and below need storage pernmissions
    val storageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if(granted) pending?.invoke()
        else Toast.makeText(context, "Storage permission is necessary", Toast.LENGTH_SHORT).show()
        pending = null
    }

    //android 13+ notifications
    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    return remember {
        {
            download : ()-> Unit ->
            val needsStorage = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
                    !context.isGranted(storagePermission)

            if(needsStorage){
                pending = download
                storageLauncher.launch(storagePermission)
            }
            else {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !context.isGranted(android.Manifest.permission.POST_NOTIFICATIONS))
                {
                    notificationLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
                download()
            }
        }
    }

}

private fun Context.isGranted(permission: String) =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

