package com.enigmaticdevs.wallhaven.ui.screens.homescreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import com.enigmaticdevs.wallhaven.MyApp
import com.enigmaticdevs.wallhaven.data.Objects.Sorting
import com.enigmaticdevs.wallhaven.domain.viewmodels.WallpaperListViewModel
import com.enigmaticdevs.wallhaven.domain.viewmodels.WallpaperListViewModel.*
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel


@Composable
fun WallpaperListScreen(sorting: String, onPhotoClick: (String) -> Unit) {
    val appGraph = (LocalContext.current.applicationContext as MyApp).appGraph

    val viewModel: WallpaperListViewModel = assistedMetroViewModel(
        extras = remember(sorting) {
            MutableCreationExtras().apply { set(WallpaperListViewModel.SortingKey, sorting) }
        }
    )
    val state by viewModel.uiState.collectAsState()
}

