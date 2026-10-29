package com.enigmaticdevs.wallhaven.ui.util

import android.app.WallpaperManager
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

fun Context.launchSystemWallpaperSetter(contentUri: Uri) {
    try {
        // Primary: Try Android's built-in Crop & Set Wallpaper intent
        val intent = WallpaperManager.getInstance(this)
            .getCropAndSetWallpaperIntent(contentUri)
            .apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        startActivity(intent)
    } catch (e: IllegalArgumentException) {
        // Fallback: Standard ACTION_ATTACH_DATA if the OEM gallery doesn't support the direct crop intent
        val fallbackIntent = Intent(Intent.ACTION_ATTACH_DATA).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            setDataAndType(contentUri, "image/*")
            putExtra("mimeType", "image/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(fallbackIntent, "Set as:"))
    } catch (e: Exception) {
        Toast.makeText(this, "No app found to handle wallpaper setting", Toast.LENGTH_SHORT).show()
    }
}

fun setAsWallpaperWithSystem(context: Context, uri: Uri) {

    // Other apps can't open file:// URIs, so wrap those in a FileProvider URI
    val shareable = if (uri.scheme == ContentResolver.SCHEME_FILE) {
        File(requireNotNull(uri.path)).toContentUri(context)
    } else {
        uri
    }

    val intent = Intent(Intent.ACTION_ATTACH_DATA).apply {
        addCategory(Intent.CATEGORY_DEFAULT)
        setDataAndType(uri, "image/*")
        putExtra("mimeType", "image/*")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }


    try {
        context.startActivity(Intent.createChooser(intent, "Set as"))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No app available to set a wallpaper", Toast.LENGTH_SHORT).show()
    }
}

fun File.toContentUri(context: Context): Uri =
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", this)


suspend fun File.toMediaUri(context: Context): Uri? = suspendCancellableCoroutine { cont ->
    MediaScannerConnection.scanFile(context, arrayOf(path), null) { _, uri ->
        cont.resume(uri)
    }
}