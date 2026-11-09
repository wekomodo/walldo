package com.enigmaticdevs.wallhaven.ui.screens.features.collections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionCover
import com.enigmaticdevs.wallhaven.data.remote.model.RemoteCollection
import com.enigmaticdevs.wallhaven.ui.presentation.PrefTopAppBar
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun CollectionsRoute(
    onBack: () -> Unit,
    viewModel: CollectionsViewModel = metroViewModel()
) {
    val collections by viewModel.collectionsInfo.collectAsStateWithLifecycle()
    CollectionsContent(
        collections
    )
}

@Composable
fun CollectionsContent(
    collections : List<CollectionCover>?
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { PrefTopAppBar(title = "Collections", onBack = {}) }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if(collections.isNullOrEmpty())
                Text("collections is empty")
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                collections?.let {
                    items(collections.size){
                        CollectionItem(collections[it])
                    }
                }

            }
        }


    }
}


@Composable
fun CollectionItem(
    item : CollectionCover
) {

    Box(
        modifier = Modifier.height(100.dp).fillMaxWidth().padding(4.dp)
    ) {
        Card(
            Modifier.fillMaxSize()//.alpha(.7f)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                // Background Image
                if (item.coverThumbUrl != null) {
                    AsyncImage(
                        model = item.coverThumbUrl,
                        contentDescription = "${item.name} cover",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.4f) // Dim the image so the text remains readable
                    )
                }

                // Foreground Text
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}



@Preview
@Composable
fun Preview(){
    val item = CollectionCover(
       name = "walldo",
        id = 123,
        coverThumbUrl = "",
        isSynced = false,

    )
    CollectionItem(item)
}
