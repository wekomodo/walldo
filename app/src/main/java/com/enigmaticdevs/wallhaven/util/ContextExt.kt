package com.enigmaticdevs.wallhaven.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

fun Context.hasWritePermission(): Boolean {
    return hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) || Build.VERSION.SDK_INT > Build.VERSION_CODES.P
}

fun Context.hasReadPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        hasPermission(Manifest.permission.READ_MEDIA_IMAGES)
    else
        hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
}

fun Context.hasPermission(vararg permissions: String): Boolean {
    return permissions.all { singlePermission ->
        ContextCompat.checkSelfPermission(
            this,
            singlePermission
        ) == PackageManager.PERMISSION_GRANTED
    }
}


@SuppressLint("UnspecifiedRegisterReceiverFlag")
fun registerBroadcastReceiver(context : Context, receiver: BroadcastReceiver){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        ContextCompat.registerReceiver(context,
            receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    else
        context.registerReceiver(
            receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
        )
}

fun Context.hasNotificationPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        hasPermission(Manifest.permission.POST_NOTIFICATIONS)
    else
        true
}