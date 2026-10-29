package com.enigmaticdevs.wallhaven.ui.screens.homescreen.components

import android.graphics.Color.HSVToColor
import android.graphics.Color.colorToHSV
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.enigmaticdevs.wallhaven.data.remote.model.Wallpaper
import com.enigmaticdevs.wallhaven.ui.presentation.ErrorOccurred
import com.enigmaticdevs.wallhaven.ui.util.aspectRatio
import androidx.core.graphics.toColorInt
import com.enigmaticdevs.wallhaven.LocalToastTrigger
import com.enigmaticdevs.wallhaven.R
import com.enigmaticdevs.wallhaven.ui.util.rememberDownloadStarter
import com.enigmaticdevs.wallhaven.ui.util.rememberPermissionUtilsState


val TAG = "WallpaperListScreen"


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WallpaperListScreen(
    uiState: LazyPagingItems<Wallpaper>,
    onPhotoClick: (String) -> Unit,
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
                ErrorOccurred(message) {
                    uiState.refresh()
                }
            }

            is LoadState.Loading -> ContainedLoadingIndicator()
            is LoadState.NotLoading -> {

                WallpaperListGrid(uiState, onPhotoClick, Modifier)
            }
        }
    }
}


@Composable
fun WallpaperListGrid(
    photos: LazyPagingItems<Wallpaper>,
    onPhotoClick: (String) -> Unit,
    modifier: Modifier
) {

    if (photos.itemCount == 0) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Try moving a few filters")
        }
    } else
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            verticalItemSpacing = 8.dp,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier
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
fun WallpaperCard(wallpaper: Wallpaper, onPhotoClick: (String) -> Unit) {
    val aspectRatio = aspectRatio(wallpaper.dimension_x, wallpaper.dimension_y)
    val startDownload = rememberDownloadStarter()
    val context = LocalContext.current
    val showToast = LocalToastTrigger.current
    val permissionState = rememberPermissionUtilsState()
    Box(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    onClick = { onPhotoClick(wallpaper.id) }
                ),
            shape = CardDefaults.elevatedShape,

            ) {
            // Log.d("$TAG(wallpaperCard)", wallpaper.thumbs.original)
            AsyncImage(
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(aspectRatio.toFloat())
                    .defaultMinSize(minHeight = 100.dp),
                model = ImageRequest.Builder(context)
                    .data(wallpaper.thumbs.original)  // https://th.wallhaven.cc/... link
                    .crossfade(true)
                    .memoryCacheKey(wallpaper.id)
                    .build(),
                placeholder = ColorPainter(getGreenShade(wallpaper.colors[0])),
                contentScale = ContentScale.Crop,
                contentDescription = "image_name"
            )
        }

        IconButton(
            modifier = Modifier.align(Alignment.BottomEnd),
            onClick = {

            }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_download),
                contentDescription = "favorite toggle"
            )
        }

    }


    /*      Column(
              modifier = Modifier.fillMaxSize()
          ) {
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.End
              ) {
                  IconButton(
                      onClick = {}
                  ) {
                      Icon(imageVector = Icons.Rounded.Favorite, contentDescription = "favorite toggle")
                  }
              }
          }
  */


}


@Composable
fun SnackText(text: String) {
    val snackBarHostState = remember { SnackbarHostState() }
    LaunchedEffect(text) {
        snackBarHostState.showSnackbar("Already Downloaded")
    }
}

/*
@Composable
fun WallpaperListRoute(
    sorting: String,
    onPhotoClick: (String) -> Unit,
    viewModel: WallpaperListViewModel = assistedMetroViewModel<WallpaperListViewModel, Factory> (key = sorting){
        create(
            sorting
        )
    }
) {
    val currentFilters by viewModel.globalFilterRepository.filters.collectAsState()
    val lazyWallpapers = viewModel.wallpaperFLow.collectAsLazyPagingItems()
    LaunchedEffect(key1 =currentFilters) {
        val params = WallhavenAPIparams(
            sorting = sorting,
            purity = currentFilters.purity,
            category = currentFilters.category,
            ratio = currentFilters.ratio,
            resolution = currentFilters.resolution,
            topRange = currentFilters.topRange
        )
        viewModel
    }
    //passing data to the stateless screen
    WallpaperListScreen(
        lazyWallpapers,
        onPhotoClick = onPhotoClick,
        onLoadMore = {
        }
    )
}
*/

fun getGreenShade(hexString: String): Color {
    val parsedColor = try {
        hexString.toColorInt()
    } catch (e: Exception) {
        return Color(0xFF81C784) // Fallback green
    }

    val hsv = FloatArray(3)
    colorToHSV(parsedColor, hsv)

    // Force Hue to Green (~120 degrees)
    hsv[0] = 120f
    // Leave hsv[1] (Saturation) and hsv[2] (Value) untouched

    return Color(HSVToColor(hsv))
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

