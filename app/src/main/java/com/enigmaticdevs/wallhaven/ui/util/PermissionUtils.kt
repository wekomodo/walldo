package com.enigmaticdevs.wallhaven.ui.util

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

class PermissionUtilsState(
    val isGranted : Boolean,
    val requestPermission : ()-> Unit
)


@Composable
fun rememberPermissionUtilsState(): PermissionUtilsState {
    val context = LocalContext.current
    val permission = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> Manifest.permission.POST_NOTIFICATIONS
        Build.VERSION.SDK_INT <= Build.VERSION_CODES.P -> Manifest.permission.WRITE_EXTERNAL_STORAGE
        else -> null
    }

    fun check() = permission == null ||
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    var isGranted by remember { mutableStateOf(check()) }

    // Re-check when the user comes back, e.g. from system settings
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) isGranted = check()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> isGranted = granted }

    return remember(isGranted, permission) {
        PermissionUtilsState(
            isGranted = isGranted,
            requestPermission = {
                if (!isGranted && permission != null) launcher.launch(permission)
            }
        )
    }
}