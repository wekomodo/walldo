package com.enigmaticdevs.wallhaven.ui.screens.upgrade

// Import metroViewModel the same way your other screens do.
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enigmaticdevs.wallhaven.ui.screens.features.pro.ProViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel

/**
 * The one entry point. Show it from any screen:
 *
 *   var showPro by remember { mutableStateOf(false) }
 *   Button(onClick = { if (isPro) doThing() else showPro = true }) { ... }
 *   if (showPro) ProDialog(onDismiss = { showPro = false })
 *
 * It owns its ProViewModel, so the calling screen needs no billing code.
 * Not Pro -> upgrade dialog. Pro -> thank-you dialog (also what an upgrade
 * or restore turns into while the dialog is open).
 */
@Composable
fun ProDialog(
    onDismiss: () -> Unit,
    viewModel: ProViewModel = metroViewModel(),
) {
    val isPro by viewModel.isPro.collectAsStateWithLifecycle()
    val price by viewModel.price.collectAsStateWithLifecycle()
    val isRestoring by viewModel.isRestoring.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val activity = LocalActivity.current

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
    }

    if (isPro) {
        ThankYouDialog(onDismiss)
    } else {
        UpgradeDialog(
            price = price,
            isRestoring = isRestoring,
            onUpgrade = { activity?.let(viewModel::buy) },
            onRestore = viewModel::restore,
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun UpgradeDialog(
    price: String?,
    isRestoring: Boolean,
    onUpgrade: () -> Unit,
    onRestore: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Walldo Pro") },
        text = {
            Column {
                Text("Pay once, keep it forever.")
                Spacer(Modifier.height(12.dp))
                // Edit this list to match exactly what Pro unlocks.
                Text("• Create your own collections")
                Text("• Sync collections with wallhaven.cc")
                Text("• Use your favorites as an auto-wallpaper playlist")
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onRestore, enabled = !isRestoring) {
                    Text(if (isRestoring) "Checking…" else "Restore purchase")
                }
            }
        },
        confirmButton = {
            Button(onClick = onUpgrade) {
                Text(if (price != null) "Upgrade · $price" else "Upgrade")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Not now") }
        },
    )
}

@Composable
private fun ThankYouDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thank you!") },
        text = {
            Text(
                "You've upgraded to Walldo Pro. Thanks for supporting the app, " +
                    "it genuinely helps keep it going."
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
    )
}