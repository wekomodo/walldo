package com.enigmaticdevs.wallhaven.ui.screens.tagwallpapers

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
import com.enigmaticdevs.wallhaven.data.remote.model.Tag
import com.enigmaticdevs.wallhaven.domain.pagingsource.WallpaperListItem
import com.enigmaticdevs.wallhaven.ui.presentation.PrefTopAppBar
import com.enigmaticdevs.wallhaven.ui.screens.homescreen.bottomsheet.FilterBottomSheet
import com.enigmaticdevs.wallhaven.ui.screens.homescreen.components.WallpaperListScreen
import com.enigmaticdevs.wallhaven.ui.screens.wallpaperdetail.rememberWallpaperDownloader
import dev.zacsweers.metrox.viewmodel.metroViewModel


@Composable
fun TagWallpapersRoute(
    tag: Tag,
    onBack : ()-> Unit,
    onWallpaperClick : (String)-> Unit,
    viewModel: TagWallpapersViewModel = metroViewModel()
){
    val query = "id:${tag.id}"
    viewModel.updateQuery(query)
    val wallpapers = viewModel.wallpapers.collectAsLazyPagingItems()

    TagScreenContent(
        tag = tag,
        wallpapers = wallpapers,
        onWallpaperClick = onWallpaperClick,
        onBack = onBack
    )

}

@Composable
fun TagScreenContent(
    tag : Tag,
    wallpapers : LazyPagingItems<WallpaperListItem>,
    onWallpaperClick: (String) -> Unit,
    onBack: () -> Unit
) {
    var showFilterSheet by remember { mutableStateOf(false) }
    val downloader = rememberWallpaperDownloader(LocalToastTrigger.current)
    Scaffold(
        topBar = {
            PrefTopAppBar(
                title = tag.name,
                onBack = onBack,
                navigationIconEnabled = true
            )
        },
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
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            WallpaperListScreen(
                wallpapers = wallpapers,
                onPhotoClick = onWallpaperClick,
                onLoadMore = {},
                onDownloadClick = {
                    downloader(it)
                }
            )
        }
        if (showFilterSheet)
            FilterBottomSheet(
                onDismiss = { showFilterSheet = false }
            )


    }


}