package com.enigmaticdevs.wallhaven.ui.screens.features.collections.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionCover
import com.enigmaticdevs.wallhaven.ui.presentation.PrefTopAppBar
import com.enigmaticdevs.wallhaven.ui.screens.homescreen.components.WallpaperListScreen


@Composable
fun CollectionDetailRoute(
    collection : CollectionCover,
    onBack : () -> Unit,
    onWallpaperClick : (String) -> Unit,
){


    CollectionDetailContent(
        collection
    )
}

@Composable
fun CollectionDetailContent(collection: CollectionCover) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { PrefTopAppBar(title = collection.name , onBack = {}) }
    ) { paddingValues ->

        Column(
            modifier = Modifier.padding(paddingValues)
        ) {
            //WallpaperListScreen() { }
        }

    }
}