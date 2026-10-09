package com.enigmaticdevs.wallhaven.ui.screens.homescreen

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.enigmaticdevs.wallhaven.data.Objects.TopRange
import com.enigmaticdevs.wallhaven.data.model.Wallpaper
import com.enigmaticdevs.wallhaven.data.model.Wallpapers
import com.enigmaticdevs.wallhaven.data.model.local.Category
import com.enigmaticdevs.wallhaven.data.model.local.Purity
import com.enigmaticdevs.wallhaven.data.model.local.WallhavenAPIparams
import com.enigmaticdevs.wallhaven.domain.viewmodels.WallpaperListViewModel
import com.enigmaticdevs.wallhaven.domain.viewmodels.WallpaperListViewModel.Factory
import com.enigmaticdevs.wallhaven.ui.presentation.ErrorOccurred
import com.enigmaticdevs.wallhaven.util.aspectRatio
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel


val TAG = "WallpaperListScreen"


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WallpaperListScreen(
    uiState: LazyPagingItems<Wallpaper>,
    onPhotoClick: () -> Unit,
    onLoadMore: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        when (uiState.loadState.refresh) {

            /* is WallpaperUiState.Success -> {
                val list = uiState.wallpapers.wallpaperList
                WallpaperListGrid(list, onPhotoClick)
            }


            is WallpaperUiState.Error -> {
                val message = uiState.message
                ErrorOccurred(message) { }
            }

            is WallpaperUiState.Idle -> {

            }
            is WallpaperUiState.Loading -> {
                ContainedLoadingIndicator()
            }*/
            is LoadState.Error -> {
                val message = "Something went wrong!"
                ErrorOccurred(message) { }
            }
            is LoadState.Loading ->   ContainedLoadingIndicator()
            is LoadState.NotLoading -> {

                val list = uiState
                WallpaperListGrid(list, onPhotoClick)
            }
        }
    }
}


@Composable
fun WallpaperListGrid(photos: LazyPagingItems<Wallpaper>, onPhotoClick: () -> Unit) {

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        verticalItemSpacing = 8.dp,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
    ) {
       // Log.d("$TAG(wallpaperList)", photos.toString())

        items(count = photos.itemCount) { index ->
            val wallpaper = photos[index]
            if (wallpaper != null) {
                WallpaperCard(wallpaper, onPhotoClick)
            }
        }
    }
}

@Composable
fun WallpaperCard(wallpaper: Wallpaper, onPhotoClick: () -> Unit) {
    val aspectRatio = aspectRatio(wallpaper.dimension_x, wallpaper.dimension_y)
    Card(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                onClick = onPhotoClick
            ),
        shape = CardDefaults.elevatedShape,

        ) {
        val context = LocalContext.current
        val imageRequest = ImageRequest.Builder(context)
            .data(wallpaper.thumbs.original) // Your https://th.wallhaven.cc/... link
            .crossfade(true)
            .build()

       // Log.d("$TAG(wallpaperCard)", wallpaper.thumbs.original)
        AsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(aspectRatio.toFloat())
                .defaultMinSize(minHeight = 100.dp),
            model = wallpaper.thumbs.original,
            contentScale = ContentScale.Crop,
            contentDescription = "image_name"
        )

    }
}

@Composable
fun WallpaperListRoute(
    sorting: String,
    onPhotoClick: () -> Unit,
    viewModel: WallpaperListViewModel = assistedMetroViewModel<WallpaperListViewModel, Factory> (key = sorting){
        create(
            sorting
        )
    }
) {
    val lazyWallpapers = viewModel.wallpaperFLow.collectAsLazyPagingItems()
    //passing data to the stateless screen
    WallpaperListScreen(
        lazyWallpapers,
        onPhotoClick = onPhotoClick,
        onLoadMore = {
        }
    )
}
/*
@Preview
@Composable
fun WallpaperListScreenPreview() {
    WallpaperListScreen(
        uiState = LoadState.Loading,
        onPhotoClick = {},
        onLoadMore = {}
    )
}*/

