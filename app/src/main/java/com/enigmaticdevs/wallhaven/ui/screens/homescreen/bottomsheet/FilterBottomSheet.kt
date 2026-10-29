package com.enigmaticdevs.wallhaven.ui.screens.homescreen.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.enigmaticdevs.wallhaven.R
import com.enigmaticdevs.wallhaven.data.repository.FilterState
import com.enigmaticdevs.wallhaven.ui.presentation.CategoryTitle
import com.enigmaticdevs.wallhaven.ui.presentation.ClickablePref
import com.enigmaticdevs.wallhaven.ui.util.ratioOptions
import com.enigmaticdevs.wallhaven.ui.util.resolutionOptions
import dev.zacsweers.metrox.viewmodel.metroViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    onDismiss: () -> Unit,
    viewModel : FilterViewModel = metroViewModel()
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,

        )
    val filterState by viewModel.filters.collectAsState()
// val scope = rememberCoroutineScope()
    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
        }, sheetState = sheetState
    ) {
        // Text("THIS IS BOTTOM SHEET YEEEHAW")
        FilterBottomSheetContent(
            filterState = filterState,
            onResolutionChange = viewModel::updateResolutions ,
            onRatioChange = viewModel::updateRatios,
            onCategoryChange = viewModel::updateCategory,
            onPurityChange = viewModel::updatePurity,
        )

    }
}


@Composable
fun FilterBottomSheetContent(
    filterState: FilterState,
    onCategoryChange: (String) -> Unit,
    onPurityChange: (String) -> Unit,
    onRatioChange: (String) -> Unit,
    onResolutionChange: (String) -> Unit
) {
    var showRatioDialog by remember { mutableStateOf(false) }
    var showResolutionDialog by remember { mutableStateOf(false) }
    val categories = listOf("General", "Anime", "People")
    val purity = listOf("SFW", "SKETCHY", "NSFW (API)")
    val currentRatioLabel = ratioOptions.find { it.second == filterState.ratio }?.first ?: "None"
    val currentResolutionLabel =
        resolutionOptions.find { it.second == filterState.resolution }?.first ?: "None"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize()
    ) {

        CategoryTitle("General")
        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            ClickablePref(
                text = "Resolution Filter",
                description = "Only greater than your chosen resolution will be shown",
                icon = R.drawable.ic_resolution,
                onClick = { showResolutionDialog = true }
            )
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            ClickablePref(
                text = "Ratio Filter",
                description = "Selected Ratios will be shown",
                icon = R.drawable.ic_ratio,
                onClick = { showRatioDialog = true })
        }

        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
        CategoryTitle("Category Filter")
        MultiChoiceSegmentedButton(
            options = categories,
            currentSelection = filterState.category,
            onStringChanged = onCategoryChange
        )
        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
        CategoryTitle("Purity Filter")
        MultiChoiceSegmentedButton(
            options = purity,
            currentSelection = filterState.purity,
            onStringChanged = onPurityChange
        )

        if (showResolutionDialog) {
            FilterSelectionDialog(
                title = "Select Resolution",
                options = resolutionOptions,
                currentSelection = filterState.resolution,
                onSelect = onResolutionChange,
                onDismiss = { showResolutionDialog = false }
            )
        }

        if (showRatioDialog) {
            FilterSelectionDialog(
                title = "Select Ratio",
                options = ratioOptions,
                currentSelection = filterState.ratio,
                onSelect = onRatioChange,
                onDismiss = { showRatioDialog = false }
            )
        }

    }
}


@Preview
@Composable
fun FilterBottomSheetPreview() {
    // FilterBottomSheetContent()
}