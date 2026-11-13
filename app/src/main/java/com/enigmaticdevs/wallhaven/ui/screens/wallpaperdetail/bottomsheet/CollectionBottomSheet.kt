package com.enigmaticdevs.wallhaven.ui.screens.wallpaperdetail.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.enigmaticdevs.wallhaven.R
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionEntity
import com.enigmaticdevs.wallhaven.data.repository.CollectionMembership
import com.enigmaticdevs.wallhaven.ui.screens.collections.components.CreateCollectionDialog
import com.enigmaticdevs.wallhaven.ui.screens.collections.components.CreateCollectionDialogContent
import com.enigmaticdevs.wallhaven.ui.screens.collections.components.CreateCollectionRequest


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsSheet(
    memberships: List<CollectionMembership>,
    onToggle: (collectionId: Long, shouldContain: Boolean) -> Unit,
    onCreate: (CreateCollectionRequest) -> Unit,
    onDismiss: () -> Unit
) {
    var showCreate by remember { mutableStateOf(false) }
    val sheetState = rememberBottomSheetState(SheetValue.Expanded)

    val syncedCollections = remember(memberships) { memberships.filter { it.collection.isSynced } }
    val localCollections  = remember(memberships) { memberships.filterNot { it.collection.isSynced } }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Absolute.SpaceBetween
        ) {
            Text(
                text = "Add to collection",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            Button(
                onClick = {
                    showCreate = true
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.new_collection),
                )
            }

        }
        LazyColumn {

            if (syncedCollections.isNotEmpty()) {
                item(
                    key = "synced_collections",
                    content = {
                        SectionHeader(
                            "Wallhaven Synced",
                            "Wallpapers only belong to one collection"
                        )
                    })
                items(syncedCollections,key = {it.collection.id}) { item ->
                    CollectionSheetItem(
                        item = item,
                        isEnabled = true,
                        isRadio = true,
                        onToggle = onToggle
                    )
                }

            }
            if(localCollections.isNotEmpty()){
                item(key = "local_collections") {
                    SectionHeader("On Device")
                }
                items(localCollections, key = { it.collection.id }) { item ->
                    CollectionSheetItem(
                        item = item,
                        isEnabled = true,
                        isRadio = false,
                        onToggle = onToggle
                    )
            }

                /*ListItem(
                    headlineContent = { Text(item.collection.name) },
                    supportingContent = if (item.collection.isSynced) {
                        { Text("Wallhaven Synced", style = MaterialTheme.typography.bodySmall) }
                    } else null,
                    leadingContent = { Checkbox(checked = item.contains, onCheckedChange = null) },
                    modifier = Modifier.clickable { onToggle(item.collection.id, !item.contains) }
                )*/
            }
            /* item {
                 ListItem(
                     headlineContent = { Text("New collection") },
                     leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
                     modifier = Modifier.clickable { showCreate = true }
                 )
             }*/
        }
        Spacer(Modifier.height(24.dp))
    }

    if (showCreate) {
        CreateCollectionDialog(
            onConfirm = { it ->
                onCreate(CreateCollectionRequest(it.name, it.isSynced, it.isPublic)); showCreate =
                false
            },
            onDismiss = { showCreate = false }
        )
    }
}


@Composable
private fun SectionHeader(title: String, subtitle: String? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CollectionSheetItem(
    item: CollectionMembership,
    isEnabled: Boolean,
    isRadio: Boolean,
    onToggle: (Long, Boolean) -> Unit,
) {
    val collection = item.collection
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = item.contains,
                enabled = isEnabled,
                role = if(isRadio) Role.RadioButton else Role.Checkbox,
                onClick = {
                    onToggle(collection.id, !item.contains)
                }
            )
            .padding(horizontal = 24.dp, vertical = 12.dp),
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
        if(isRadio)
            RadioButton(
                enabled = isEnabled,
                selected = item.contains,
                onClick = null // handled by the row's selectable modifier
            )
        else
        Checkbox(
            enabled = isEnabled,
            checked = item.contains,
            onCheckedChange = null // handled by the row's selectable modifier
        )

    }
}


@Composable
fun CheckboxWithText(
    text: String,
    isChecked: Boolean,
    checkedChange: (Boolean) -> Unit,
    enabled: Boolean
) {
    val alpha = if (enabled) 1f else 0.38f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .alpha(alpha),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, modifier = Modifier.weight(1f))
        Checkbox(
            enabled = enabled,
            checked = isChecked,
            onCheckedChange = checkedChange
        )
    }
}


@Preview
@Composable
fun Preview() {
    /*CustomDialog(
        onConfirm = { string, bool, bool1 -> },
        onDismiss = {}
    )*/
    CreateCollectionDialogContent(
        name = "",
        isSynced = false,
        isPublic = false,
        onSyncedChange = {},
        onPublicChange = {},
        onTextChange = {},
        onConfirm = {},
        onDismiss = {}
    )
}

@Composable
fun NewCollectionDialog(onConfirm: (String, Boolean, Boolean) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New collection") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(40) },
                singleLine = true,
                label = { Text("Name") }
            )
        },
        confirmButton = {
            TextButton(
                onClick = { },
                enabled = name.isNotBlank()
            ) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}