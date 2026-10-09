package com.enigmaticdevs.wallhaven.ui.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.enigmaticdevs.wallhaven.data.model.Tag
import com.enigmaticdevs.wallhaven.data.model.Wallpaper


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoBottomSheet(wallpaperInfo: Wallpaper, onDismiss: () -> Unit) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden
    )
    // val scope = rememberCoroutineScope()
    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
        },
        sheetState = sheetState
    ) {
        Text("THIS IS BOTTOM SHEET YEEEHAW")
        BottomSheetContent(
            wallpaperInfo.id,
            wallpaperInfo.resolution,
            wallpaperInfo.colors,
            wallpaperInfo.tags
        )

    }
}

@Composable
fun BottomSheetContent(id: String, resolution: String, colors: List<String>, tags: List<Tag>?) {
    Column(
        modifier = Modifier
            .height(96.dp)
            .fillMaxWidth()
    ) {
        Text("Info (tap on image ID to copy)")
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.Start,
        ) {
            Column(
                horizontalAlignment = Alignment.Start,

            ) {
                Text("Image ID ")
                Text("Resolution ")
            }
            Column() {
                Text(" : $id")
                Text(" : $resolution")
            }
        }
    }
}


@Preview
@Composable
fun InfoBottomSheetPreview() {
    BottomSheetContent(
        "12355",
        "1920x1080",
        listOf("red", "black", "yellow"),
        listOf(Tag(id = 1), Tag(id = 2))
    )
}