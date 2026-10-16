package com.enigmaticdevs.wallhaven.ui.screens.homescreen.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp





// Used in FilterBottomSheet
@Composable
fun FilterSelectionDialog(
    title : String,
    options : List<Pair<String,String>>,
    currentSelection: String,
    onSelect : (String) -> Unit,
    onDismiss: () -> Unit
){
    // 1. Convert the comma-separated string from your global state into a Set
    var selectedItems by remember {
        mutableStateOf(currentSelection.split(",").filter { it.isNotBlank() }.toSet())
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {Text(title)},
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(options){ (label, apiValue) ->
                    val isSelected = if(apiValue.isEmpty()){
                        selectedItems.isEmpty()
                    }
                    else{
                        selectedItems.contains(apiValue)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedItems = if (apiValue.isEmpty()) {
                                    emptySet()
                                } else {
                                    if (isSelected) {
                                        selectedItems - apiValue
                                    } else {
                                        selectedItems + apiValue
                                    }
                                }
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp)
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = null
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(text = label, style = MaterialTheme.typography.bodyLarge)


                    }

                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSelect(selectedItems.joinToString(","))
                    onDismiss()
                }
            ) { Text("Apply") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun MultiChoiceSegmentedButton(
    options: List<String>,
    currentSelection: String, //eg 110
    onStringChanged: (String) -> Unit
) {
    // var selectedIndex by remember { mutableIntStateOf(0) }
    val selectedOptions = remember {
        mutableStateListOf(false, false, false)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MultiChoiceSegmentedButtonRow() {
            options.forEachIndexed { index, label ->
                val isChecked = currentSelection.getOrNull(index) == '1'
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    checked = isChecked,
                    onCheckedChange = {
                        val charArray = currentSelection.toCharArray()
                        charArray[index] = if (isChecked) '0' else '1'
                        onStringChanged(String(charArray))
                    },
                    label = { Text(label) })
            }
        }
    }

}

