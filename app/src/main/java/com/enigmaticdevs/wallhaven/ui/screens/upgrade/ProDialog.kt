package com.enigmaticdevs.wallhaven.ui.screens.upgrade

// Import metroViewModel the same way your other screens do.
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
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
private data class ProFeature(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
)

private val proFeatures = listOf(
    ProFeature(Icons.Rounded.Add, "Your own collections", "Organize wallpapers your way"),
        ProFeature(Icons.Rounded.Refresh, "wallhaven.cc sync", "Keep collections in sync with your account"),
    ProFeature(Icons.Rounded.PlayArrow, "Auto-wallpaper playlist", "Rotate through your favorites"),
)


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
        shape = RoundedCornerShape(28.dp),
        icon = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(56.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Star, contentDescription = null, modifier = Modifier.size(28.dp))
                }
            }
        },
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Walldo Pro", textAlign = TextAlign.Center)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Pay once, keep it forever.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                proFeatures.forEach { FeatureRow(it) }
            }
        },
        // Buttons are stacked, full width, so the primary action is obvious.
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    onClick = onUpgrade,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                ) {
                    Text(if (price != null) "Upgrade · $price" else "Upgrade")
                }
                Spacer(Modifier.height(4.dp))
                TextButton(onClick = onRestore, enabled = !isRestoring) {
                    Text(if (isRestoring) "Checking…" else "Restore purchase")
                }
                TextButton(onClick = onDismiss) {
                    Text("Not now", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
    )
}

@Composable
private fun FeatureRow(feature: ProFeature) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(40.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(feature.icon, contentDescription = null, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(feature.title, style = MaterialTheme.typography.titleSmall)
            Text(
                feature.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
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