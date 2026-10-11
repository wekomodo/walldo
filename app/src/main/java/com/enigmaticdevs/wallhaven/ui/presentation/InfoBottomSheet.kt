package com.enigmaticdevs.wallhaven.ui.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ChipElevation
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.enigmaticdevs.wallhaven.data.model.Tag
import com.enigmaticdevs.wallhaven.data.model.Wallpaper


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoBottomSheet(wallpaperInfo: Wallpaper, onDismiss: () -> Unit) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,

    )
    // val scope = rememberCoroutineScope()
    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
        },
        sheetState = sheetState
    ) {
       // Text("THIS IS BOTTOM SHEET YEEEHAW")
        BottomSheetContent(
            wallpaperInfo.id,
            wallpaperInfo.resolution,
            wallpaperInfo.colors,
            wallpaperInfo.tags
        )

    }
}

@Composable
fun BottomSheetContent(id: String, resolution: String, colors: List<String>, tags: List<Tag>) {
    Column(
        modifier = Modifier
            .wrapContentSize()
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Wallpaper Details",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
         //   horizontalAlignment = Alignment.SpaceBetween,
        ) {
            InfoItem(title = "ID", value = id)
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            InfoItem(title = "Resolution", value = resolution)
        }

        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

        Text(
            text = "Tags",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            tags.forEach {
                AssistChip(
                    onClick = { /* Optional: Navigate to search screen with this tag */ },
                    label = { Text(it.name) },
                    //elevation = AssistChipDefaults.elevatedAssistChipElevation(8.dp)
                )
            }
        }
    }
}

@Composable
fun InfoItem(title: String, value: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
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