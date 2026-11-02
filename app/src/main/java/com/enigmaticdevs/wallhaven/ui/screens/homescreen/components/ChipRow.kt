package com.enigmaticdevs.wallhaven.ui.screens.homescreen.components



import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.enigmaticdevs.wallhaven.data.remote.model.Objects.Sorting

@Composable
fun SortChipRow(
    currentSort: String,
    onSortSelected: (String) -> Unit
) {
    val sortOptions = listOf(
        Sorting.topList,
        Sorting.dateAdded,
        Sorting.views,
        Sorting.favorites,
        Sorting.relevance,
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(sortOptions) { option ->
            FilterChip(
                selected = currentSort == option,
                onClick = { onSortSelected(option) },
                label = {
                    Text(option.replace("_", " ").replaceFirstChar { it.uppercase() })
                }
            )
        }
    }
}