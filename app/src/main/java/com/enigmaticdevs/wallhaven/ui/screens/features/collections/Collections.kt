package com.enigmaticdevs.wallhaven.ui.screens.features.collections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.enigmaticdevs.wallhaven.ui.presentation.PrefTopAppBar

@Composable
fun CollectionsRoute(onBack: () -> Unit) {
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
