package com.enigmaticdevs.wallhaven.ui.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.enigmaticdevs.wallhaven.R
import com.enigmaticdevs.wallhaven.ui.util.themedAppDrawable


@Composable
fun ErrorOccurred(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        Modifier.wrapContentSize().padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.wrapContentSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
               // modifier = Modifier.padding(vertical = 8.dp),
                text = message)

            val painter = themedAppDrawable(
                R.drawable.ic_error_bg,
                MaterialTheme.colorScheme.surfaceContainerHigh
            )
            Image(
                painter = painter, // Your custom path-tinted VectorPainter
                contentDescription = "Descriptive visual text",
                modifier = Modifier
                  //  .padding(4.dp)
                    .width(200.dp) // Safely scales up to fit the container
                    .aspectRatio(1f) // Keeps its aspect ratio intact
            )
            Button(
                onClick = onRetry
            ) {
                Icon(imageVector = Icons.Rounded.Refresh, contentDescription = "Retry")
                Text("Retry")
            }
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