package com.enigmaticdevs.wallhaven.ui.screens.autowallpaper

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionCover

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionPickerBottomSheet(
    sheetState : SheetState,
    collections: List<CollectionCover>,
    selectedCollectionId: Long,
    onSelectCollection: (Long) -> Unit,
    onDismiss: () -> Unit
) {

    LaunchedEffect(Unit) {
        sheetState.expand()
    }
    ModalBottomSheet(
       onDismissRequest = onDismiss,
        sheetState = sheetState,
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Choose Collection",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(
                    items = collections,
                    key = { it.id }
                ) { collection ->
                    val isSelected = collection.id == selectedCollectionId
                    val isEnabled = collection.count > 0

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = isSelected,
                                enabled = isEnabled,
                                role = Role.RadioButton,
                                onClick = {
                                    onSelectCollection(collection.id)
                                   // onDismiss()
                                }
                            )
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                            .alpha(if (isEnabled) 1f else 0.4f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Small 48.dp cover thumbnail preview
                        AsyncImage(
                            model = collection.coverThumbUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = collection.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "${collection.count} wallpapers" +
                                    if (collection.isSynced) " • Synced" else "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        RadioButton(
                            selected = isSelected,
                            onClick = null, // Handled by parent Row's selectable modifier
                            enabled = isEnabled
                        )
                    }
                }
            }
        }
    }
}