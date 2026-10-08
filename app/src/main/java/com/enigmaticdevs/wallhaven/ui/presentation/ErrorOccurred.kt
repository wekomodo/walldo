package com.enigmaticdevs.wallhaven.ui.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.tooling.preview.Preview


@Composable
fun ErrorOccurred(
    message : String,
    onRetry : () -> Unit
){

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment =  Alignment.CenterHorizontally
    ) {
        Text(message)
        IconButton(
            onClick = onRetry
        ) {
            Icon( imageVector = Icons.Rounded.Refresh, contentDescription = "Retry")
        }
    }
}





@Preview
@Composable
fun ErrorPreview(){
    ErrorOccurred(
        "Oops, something broke"
    ) {}
}