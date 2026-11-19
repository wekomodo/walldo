package com.enigmaticdevs.wallhaven.ui.screens.tagwallpapers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.enigmaticdevs.wallhaven.data.remote.model.Tag
import com.enigmaticdevs.wallhaven.domain.pagingsource.WallpaperListItem
import com.enigmaticdevs.wallhaven.ui.presentation.NavigationIcon
import com.enigmaticdevs.wallhaven.ui.presentation.PrefTopAppBar
import com.enigmaticdevs.wallhaven.ui.screens.homescreen.components.WallpaperListScreen
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

    Scaffold(
        topBar = {
            PrefTopAppBar(
                title = tag.name,
                onBack = onBack,
                navigationIconEnabled = true
            )
        },

    ) { paddingValues ->

        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            WallpaperListScreen(
                wallpapers = wallpapers,
                onPhotoClick = onWallpaperClick,
                onLoadMore = {}
            )
        }

    }


}