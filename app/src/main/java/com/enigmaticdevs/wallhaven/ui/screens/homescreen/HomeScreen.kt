package com.enigmaticdevs.wallhaven.ui.screens.homescreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.enigmaticdevs.wallhaven.R
import com.enigmaticdevs.wallhaven.data.remote.model.Wallpaper
import com.enigmaticdevs.wallhaven.data.remote.model.local.WallhavenAPIparams
import com.enigmaticdevs.wallhaven.ui.screens.homescreen.bottomsheet.FilterBottomSheet
import com.enigmaticdevs.wallhaven.ui.screens.homescreen.components.WallpaperListScreen
import com.enigmaticdevs.wallhaven.ui.screens.homescreen.components.SimpleSearchBar
import com.enigmaticdevs.wallhaven.ui.screens.homescreen.components.SortChipRow
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.flow.filterNotNull


val TAG = "Homescreen"

@Composable
fun HomeScreen(
    onPhotoClick: (String) -> Unit,
    onOpenDrawer: () -> Unit,
    viewModel: HomeViewModel = metroViewModel()
) {
    val currentFilters by viewModel.globalFilterRepository.filters.collectAsState()
    val lazyWallpapers = viewModel.wallpapers.collectAsLazyPagingItems()
    val activeSort by viewModel.activeSort.collectAsState()
    val textFieldState = rememberTextFieldState()
    LaunchedEffect(Unit) {
        viewModel.savedQuery.filterNotNull().collect {
            textFieldState.setTextAndPlaceCursorAtEnd(it)
        }
    }

    LaunchedEffect(key1 = currentFilters) {
        val params = WallhavenAPIparams(
            sorting = activeSort,
            purity = currentFilters.purity,
            category = currentFilters.category,
            ratio = currentFilters.ratio,
            resolution = currentFilters.resolution,
            topRange = currentFilters.topRange
        )
    }
    HomeScreenContent(
        textFieldState = textFieldState,
        activeSort = activeSort,
        wallpapers = lazyWallpapers,
        onPhotoClick = onPhotoClick,
        updateSort = { viewModel.updateSort(it) },
        onSearch = { viewModel.triggerSearch(it) },
        onOpenDrawer = onOpenDrawer
    )

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    textFieldState : TextFieldState,
    activeSort: String,
    wallpapers: LazyPagingItems<Wallpaper>,
    onPhotoClick: (String) -> Unit,
    updateSort: (String) -> Unit,
    onSearch: (String) -> Unit,
    onOpenDrawer: () -> Unit
) {

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    var showFilterSheet by remember { mutableStateOf(false) }
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    // containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Row(
                        modifier = Modifier.wrapContentSize()
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(R.drawable.wall),
                            contentDescription = "",
                            tint = Color.Unspecified
                        )
                        Text(
                            stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                },
                navigationIcon = {
                    IconButton(
                        onClick = onOpenDrawer
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Menu Button"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showFilterSheet = true
                }
            ) {
                Icon(imageVector = Icons.Rounded.Settings, contentDescription = "Settings")
            }

        }
    ) { innerPadding ->

        //old way using two tabs
        // Tabs(innerPadding, onPhotoClick = onPhotoClick )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {

            // 1. The Search Bar
            SimpleSearchBar(
                textFieldState = textFieldState,
                searchResults = emptyList(), // Hook this up to local search history later
                onSearch = { query ->
                    onSearch(query)
                    /*if (query.isNotBlank())
                        updateSort(Sorting.relevance)
                    else
                        updateSort(Sorting.topList)*/
                },
                onBack = { FocusRequester() }
            )

            // 2. The Sort Chips
            SortChipRow(
                currentSort = activeSort,
                onSortSelected = { newSort -> updateSort(newSort) }
            )

            // 3. The Paging Grid (Takes remaining space)
            WallpaperListScreen(
                uiState = wallpapers,
                onPhotoClick = onPhotoClick,
                onLoadMore = {}
            )


        }
        if (showFilterSheet)
            FilterBottomSheet(onDismiss = { showFilterSheet = false })

    }


}


@Composable
fun SpacerX(height: Int) {
    Spacer(modifier = Modifier.height(height.dp))
}