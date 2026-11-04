package com.enigmaticdevs.wallhaven.ui.screens.wallpaperdetail.bottomsheet

import android.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.enigmaticdevs.wallhaven.R
import com.enigmaticdevs.wallhaven.data.repository.CollectionMembership


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsSheet(
    memberships: List<CollectionMembership>,
    onToggle: (collectionId: Long, shouldContain: Boolean) -> Unit,
    onCreate: (name: String, isSynced: Boolean, isPublic: Boolean) -> Unit,
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
        CustomDialog(
            onConfirm = { string, bool, bool1 ->
                onCreate(string, bool, bool1); showCreate = false
            },
            onDismiss = { showCreate = false }
        )
    }
}


@Composable
fun CustomDialog(onConfirm: (String, Boolean, Boolean) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var sync by remember { mutableStateOf(false) }
    var public by remember { mutableStateOf(false) }
    Dialog(
        onDismissRequest = onDismiss,
        content = {
            CustomDialogContent(
                name = name,
                isSynced = sync,
                isPublic = public,
                onTextChange = { name = it.take(40) },
                onSyncedChange = {sync =it},
                onPublicChange = {public = it},
                onConfirm = {onConfirm(name,sync,public)},
                onDismiss = onDismiss
            )
        }
    )
}

@Composable
fun CustomDialogContent(
    name : String,
    isSynced : Boolean,
    isPublic : Boolean,
    onTextChange : (String) -> Unit,
    onSyncedChange: (Boolean) -> Unit,
    onPublicChange: (Boolean) -> Unit,
    onConfirm : ()-> Unit,
    onDismiss: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(25)
    ) {

        Column(
            modifier = Modifier.padding(24.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.new_collection),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    value = name,
                    onValueChange = onTextChange,
                    singleLine = true,
                    label = { Text("Name") }
                )
                CheckboxWithText(
                    text = stringResource(R.string.isSynced),
                    isChecked = isSynced,
                    checkedChange =  onSyncedChange,
                    enabled = true
                )
                CheckboxWithText(
                    text = stringResource(R.string.isPublic),
                    isChecked = isPublic,
                    checkedChange = onPublicChange,
                    enabled = isSynced
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = onConfirm
                    ) {
                        Text("Submit")
                    }
                }

            }
        }
    }
}


@Composable
fun CheckboxWithText(text: String, isChecked: Boolean, checkedChange: (Boolean) -> Unit, enabled : Boolean) {
    val alpha = if(enabled) 1f else 0.38f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
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
    CustomDialog(
        onConfirm = { string, bool, bool1 -> },
        onDismiss = {}
    )
    CustomDialogContent(
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