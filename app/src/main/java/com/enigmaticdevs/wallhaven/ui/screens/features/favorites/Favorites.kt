package com.enigmaticdevs.wallhaven.ui.screens.features.favorites

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.contentType
import com.enigmaticdevs.wallhaven.ui.presentation.PrefTopAppBar
import kotlinx.coroutines.Job

@Composable
fun FavoritesRoute(onBack: () -> Unit) {
    CollectionsContent()
}

@Composable
fun CollectionsContent() {

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { PrefTopAppBar(title = "Collections", onBack = {}) }
    ) { paddingValues ->

        Column(
            modifier = Modifier.fillMaxSize()
                .padding(paddingValues)
        ) {
            Text("Collections screen")
        }




    }
}
