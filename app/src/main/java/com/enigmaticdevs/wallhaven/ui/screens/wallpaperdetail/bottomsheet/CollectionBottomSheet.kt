package com.enigmaticdevs.wallhaven.ui.screens.wallpaperdetail.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.enigmaticdevs.wallhaven.data.repository.CollectionMembership


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsSheet(
    memberships: List<CollectionMembership>,
    onToggle: (collectionId: Long, shouldContain: Boolean) -> Unit,
    onCreate: (name: String) -> Unit,
    onDismiss: () -> Unit
) {
    var showCreate by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            "Add to collection",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
        LazyColumn {
            items(memberships, key = { it.collection.id }) { m ->
                ListItem(
                    headlineContent = { Text(m.collection.name) },
                    leadingContent = { Checkbox(checked = m.contains, onCheckedChange = null) },
                    modifier = Modifier.clickable { onToggle(m.collection.id, !m.contains) }
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
        NewCollectionDialog(
            onConfirm = { onCreate(it); showCreate = false },
            onDismiss = { showCreate = false }
        )
    }
}

@Composable
fun NewCollectionDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
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
            TextButton(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}