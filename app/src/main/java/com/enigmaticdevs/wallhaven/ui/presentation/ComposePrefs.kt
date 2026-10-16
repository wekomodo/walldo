package com.enigmaticdevs.wallhaven.ui.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


// title for different categories
@Composable
fun CategoryTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
    )
}



// Clickable Prefs replicated to feel like Old xml Prefs
@Composable
fun ClickablePref(text: String, description: String, icon: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = true, onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            painter = painterResource(icon),
            contentDescription = text
        )
        Column(
            modifier = Modifier
                .wrapContentSize()
                .padding(start = 8.dp, top = 8.dp)
        ) {
            Text(text = text, style = MaterialTheme.typography.bodyLarge)
            Text(text = description, style = MaterialTheme.typography.bodySmall)

        }
    }

}

@Composable
fun ClickablePref(text: String, description: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = true, onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            imageVector = icon,
            contentDescription = text
        )
        Column(
            modifier = Modifier
                .wrapContentSize()
                .padding(start = 8.dp, top = 8.dp)
        ) {
            Text(text = text, style = MaterialTheme.typography.bodyLarge)
            Text(text = description, style = MaterialTheme.typography.bodySmall)

        }
    }

}

@Composable
fun PrefTopAppBar(title: String, onBack: () -> Unit){

    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = {onBack()}) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
        }
    )
}