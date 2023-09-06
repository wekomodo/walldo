package com.enigmaticdevs.wallhaven.util.download

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.enigmaticdevs.wallhaven.BuildConfig
import com.enigmaticdevs.wallhaven.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

const val WALLDO_DIRECTORY = "Walldo"

const val FILE_PROVIDER_AUTHORITY = "${BuildConfig.APPLICATION_ID}.provider"

val WALLDO_RELATIVE_PATH = "${Environment.DIRECTORY_PICTURES}${File.separator}$WALLDO_DIRECTORY"

val WALLDO_LEGACY_PATH = "${Environment.getExternalStoragePublicDirectory(
    Environment.DIRECTORY_PICTURES)}${File.separator}$WALLDO_DIRECTORY"

fun Context.fileExists(fileName: String): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
        val selection = "${MediaStore.MediaColumns.RELATIVE_PATH} like ? and " +
                "${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
        val relativePath = WALLDO_RELATIVE_PATH
        val selectionArgs = arrayOf("%$relativePath%", fileName)
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        contentResolver.query(uri, projection, selection, selectionArgs, null)?.use {
            return it.count > 0
        } ?: return false
    } else {
        return File(WALLDO_RELATIVE_PATH, fileName).exists()
    }
}

fun Context.getUriForPhoto(fileName: String): Uri? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val projection = arrayOf(MediaStore.MediaColumns._ID)
        val selection = "${MediaStore.MediaColumns.RELATIVE_PATH} like ? and " +
                "${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
        val relativePath = WALLDO_RELATIVE_PATH
        val selectionArgs = arrayOf("%$relativePath%", fileName)
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        contentResolver.query(uri, projection, selection, selectionArgs, null)?.use {
            return if (it.moveToFirst()) {
                ContentUris.withAppendedId(uri, it.getLong(
                    it.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)))
            } else {
                null
            }
        } ?: return null
    } else {
        val file = File(WALLDO_LEGACY_PATH, fileName)
        return FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, file)
    }
}

fun showFileExistsDialog(context: Context, action: () -> Unit) {
    MaterialAlertDialogBuilder(context)
        .setTitle(R.string.file_exists_title)
        .setMessage(R.string.file_exists_message)
        .setPositiveButton(R.string.yes) { _, _ -> action.invoke() }
        .setNegativeButton(R.string.no, null)
        .show()
}