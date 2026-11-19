package com.enigmaticdevs.wallhaven.ui.screens.homescreen.components



import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.enigmaticdevs.wallhaven.R
import com.enigmaticdevs.wallhaven.data.remote.model.objects.Sorting

@Composable
fun SortChipRowWButton(
    currentSort: String,
    onSortSelected: (String) -> Unit,
    onFilterClick : () -> Unit,
) {
    val sortOptions = listOf(
        Sorting.topList,
        Sorting.relevance,
        Sorting.favorites,
        Sorting.views,
        Sorting.dateAdded,
        Sorting.random
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilledIconButton(
                modifier = Modifier.minimumInteractiveComponentSize(),
                onClick = onFilterClick
            ) {
                Icon(painter = painterResource(R.drawable.ic_filter_tune), "Search Info")
            }
        }
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