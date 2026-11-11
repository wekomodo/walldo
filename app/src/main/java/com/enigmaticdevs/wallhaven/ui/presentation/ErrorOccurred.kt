package com.enigmaticdevs.wallhaven.ui.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.enigmaticdevs.wallhaven.R
import com.enigmaticdevs.wallhaven.ui.util.themedAppDrawable


@Composable
fun ErrorOccurred(
    message: String,
    onRetry: () -> Unit
) {

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message)
        val painter = themedAppDrawable(R.drawable.ic_waiting_bg, MaterialTheme.colorScheme.surfaceContainerHigh)
        Image(
            painter = painter, // Your custom path-tinted VectorPainter
            contentDescription = "Descriptive visual text",
            modifier = Modifier
                .width(200.dp) // Safely scales up to fit the container
                .aspectRatio(1f) // Keeps its aspect ratio intact
        )
        FilledTonalButton(
            onClick = onRetry
        ) {
            Icon(imageVector = Icons.Rounded.Refresh, contentDescription = "Retry")
        }
    }
}


@Preview
@Composable
fun ErrorPreview() {
    ErrorOccurred(
        "Oops, something broke"
    ) {}
}