package com.enigmaticdevs.wallhaven.ui.screens.wallpaperdetail.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
    val sheetState = rememberBottomSheetState(SheetValue.Expanded )

    ModalBottomSheet(onDismissRequest = onDismiss,
        sheetState = sheetState) {
        Text(
            "Add to collection",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
        LazyColumn {
            items(memberships, key = { it.collection.id }) { item ->
                ListItem(
                    headlineContent = { Text(item.collection.name) },
                    supportingContent = if (item.collection.isSynced) {
                        { Text("Wallhaven Synced", style = MaterialTheme.typography.bodySmall) }
                    } else null,
                    leadingContent = { Checkbox(checked = item.contains, onCheckedChange = null) },
                    modifier = Modifier.clickable { onToggle(item.collection.id, !item.contains) }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("New collection") },
                    leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
                    modifier = Modifier.clickable { showCreate = true }
                )
            }
        }
        Spacer(Modifier.height(24.dp))
    }

    if (showCreate) {
        CreateCollectionDialog(
            onConfirm = { it ->
                onCreate(CreateCollectionRequest(it.name,it.isSynced,it.isPublic)); showCreate = false
            },
            onDismiss = { showCreate = false }
        )
    }
}





@Composable
fun CheckboxWithText(text: String, isChecked: Boolean, checkedChange: (Boolean) -> Unit, enabled : Boolean) {
    val alpha = if(enabled) 1f else 0.38f
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
        name ="",
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