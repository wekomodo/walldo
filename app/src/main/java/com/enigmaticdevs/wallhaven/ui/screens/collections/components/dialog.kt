package com.enigmaticdevs.wallhaven.ui.screens.collections.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.enigmaticdevs.wallhaven.R
import com.enigmaticdevs.wallhaven.ui.screens.wallpaperdetail.bottomsheet.CheckboxWithText



data class CreateCollectionRequest(
    val name: String,
    val isSynced: Boolean,
    val isPublic: Boolean
)

@Composable
fun CreateCollectionDialog(onConfirm: (CreateCollectionRequest) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var sync by remember { mutableStateOf(false) }
    var public by remember { mutableStateOf(false) }
    Dialog(
        onDismissRequest = onDismiss,
        content = {
            CreateCollectionDialogContent(
                name = name,
                isSynced = sync,
                isPublic = public,
                onTextChange = { name = it.take(40) },
                onSyncedChange = {sync =it},
                onPublicChange = {public = it},
                onConfirm = {onConfirm(CreateCollectionRequest(name,sync,public))},
                onDismiss = onDismiss
            )
        }
    )
}

@Composable
fun CreateCollectionDialogContent(
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
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.new_collection),
                    style = MaterialTheme.typography.titleLarge
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp,Alignment.End)
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