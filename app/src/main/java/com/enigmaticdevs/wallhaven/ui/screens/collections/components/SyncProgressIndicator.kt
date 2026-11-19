package com.enigmaticdevs.wallhaven.ui.screens.collections.components

import android.util.Log
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.enigmaticdevs.wallhaven.data.local.collections.SyncResult
import com.enigmaticdevs.wallhaven.data.local.collections.SyncState

@Composable
fun SyncProgressIndicator(
    syncState: SyncState,
    lastResult: SyncResult?,
    onLoginClick: () -> Unit = {},
    onCloudflareClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isVisible = syncState.isSyncing || syncState.isIncomplete
    Log.d("SyncState",syncState.toString())

    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            color = if (syncState.isIncomplete && !syncState.isSyncing) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (syncState.isSyncing) {
                    CircularWavyProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        stroke = Stroke(5f),
                        wavelength = 10.dp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    val label = when {
                        syncState.pushing -> "Uploading changes..."
                        syncState.pulling && syncState.totalCollections > 0 ->
                            "Syncing collections (${syncState.doneCollections}/${syncState.totalCollections})..."
                        else -> "Syncing collections..."
                    }
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                } else if (syncState.isIncomplete) {
                    Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Sync incomplete. Will retry automatically.",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }

    // Handle one-off blockers/errors
    LaunchedEffect(lastResult) {
        when (lastResult) {
            is SyncResult.NeedsLogin -> onLoginClick()
            is SyncResult.NeedsCloudflare -> onCloudflareClick()
            is SyncResult.Failed -> {
                val msg = lastResult.message ?: "Collection sync failed"
                makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
            else -> Unit
        }
    }
}

@Preview
@Composable
fun Preview() {
    SyncProgressIndicator(
        syncState = SyncState(
            pushing = false,
            pulling = true,
            doneCollections = 4,
            totalCollections = 12,
        ),
        lastResult = SyncResult.NeedsCloudflare,
        onLoginClick = {},
        onCloudflareClick = {},
    )
}