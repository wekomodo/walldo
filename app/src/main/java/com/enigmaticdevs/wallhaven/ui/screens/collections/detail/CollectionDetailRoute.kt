package com.enigmaticdevs.wallhaven.ui.screens.collections.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.enigmaticdevs.wallhaven.LocalToastTrigger
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionCover
import com.enigmaticdevs.wallhaven.domain.pagingsource.WallpaperListItem
import com.enigmaticdevs.wallhaven.ui.presentation.PrefTopAppBar
import com.enigmaticdevs.wallhaven.ui.screens.homescreen.bottomsheet.FilterBottomSheet
import com.enigmaticdevs.wallhaven.ui.screens.homescreen.components.WallpaperListScreen
import com.enigmaticdevs.wallhaven.ui.screens.wallpaperdetail.rememberWallpaperDownloader
import com.enigmaticdevs.wallhaven.ui.util.rememberPermissionUtilsState
import dev.zacsweers.metrox.viewmodel.metroViewModel


@Composable
fun CollectionDetailRoute(
    collection: CollectionCover,
    onBack: () -> Unit,
    onWallpaperClick: (String) -> Unit,
    viewModel: CollectionDetailViewModel = metroViewModel()
) {

    viewModel.setCollectionId(collection.id)
    val wallpapers = viewModel.wallpapersFlow.collectAsLazyPagingItems()

    CollectionDetailContent(
        collection = collection.name,
        wallpapers = wallpapers,
        onWallpaperClick = onWallpaperClick
    )
}

@Composable
fun CollectionDetailContent(
    collection: String,
    wallpapers: LazyPagingItems<WallpaperListItem>,
    onWallpaperClick: (String) -> Unit
) {
    var showFilterSheet by remember { mutableStateOf(false) }
    val permissionState = rememberPermissionUtilsState()
    val downloader = rememberWallpaperDownloader(LocalToastTrigger.current)
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { PrefTopAppBar(title = collection, onBack = {}) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showFilterSheet = true }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = ""
                )
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier.padding(paddingValues)
        ) {
            WallpaperListScreen(
                wallpapers = wallpapers,
                onPhotoClick = onWallpaperClick,
                onDownloadClick = {
                    if (permissionState.isGranted) {
                        downloader(it)
                    } else {
                        permissionState.requestPermission()
                    }
                }
            ) { }
        }

        if (showFilterSheet)
            FilterBottomSheet(
                onDismiss = { showFilterSheet = false }
            )

    }
}