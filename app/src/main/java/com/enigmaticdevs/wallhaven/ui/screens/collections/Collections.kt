package com.enigmaticdevs.wallhaven.ui.screens.collections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.enigmaticdevs.wallhaven.R
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionCover
import com.enigmaticdevs.wallhaven.ui.presentation.DraggableItem
import com.enigmaticdevs.wallhaven.ui.presentation.NavigationIcon
import com.enigmaticdevs.wallhaven.ui.presentation.PrefTopAppBar
import com.enigmaticdevs.wallhaven.ui.presentation.rememberDragDropState
import com.enigmaticdevs.wallhaven.ui.screens.collections.components.CreateCollectionDialog
import com.enigmaticdevs.wallhaven.ui.screens.collections.components.CreateCollectionRequest
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun CollectionsRoute(
    onBack: () -> Unit,
    onCollectionDetail : (CollectionCover) -> Unit,
    viewModel: CollectionsViewModel = metroViewModel()
) {
    val collections by viewModel.collectionsInfo.collectAsStateWithLifecycle()
    CollectionsContent(
        collections,
        onClick = onCollectionDetail,
        onAddClick = viewModel::createCollection,
        onDragDone = viewModel::updateSortOrder
    )
}

@Composable
fun CollectionsContent(
    collections : List<CollectionCover>?,
    onClick : (CollectionCover) -> Unit,
    onAddClick : (CreateCollectionRequest) -> Unit,
    onDragDone : (List<Long>) -> Unit
) {
    var showCollectionDialog by  remember  { mutableStateOf(false)}
    var localList by remember { mutableStateOf(collections.orEmpty()) }
    val lazyListState = rememberLazyListState()
    val dragDropState = rememberDragDropState(
        lazyListState = lazyListState,
        onMove = { fromIndex, toIndex ->
            localList = localList.toMutableList().apply {
                add(toIndex, removeAt(fromIndex))
            }
        },
        onDragDone = { onDragDone(localList.map { it.id }) }
    )
    LaunchedEffect(collections) {
        if (collections != null && dragDropState.draggedKey == null) localList = collections
    }


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { PrefTopAppBar(
            title = "Collections",
            onBack = {},
            actions = NavigationIcon(Icons.Rounded.Add,true, onClick = {showCollectionDialog = true})
        ) },

    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding()
                .padding(paddingValues)
        ) {
            if(collections.isNullOrEmpty())
                Text("collections is empty")
           // val navBarBottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 102.dp ) //+ navBarBottomInset)
            ) {
                collections?.let {
                    itemsIndexed(items = localList, key = { _, item -> item.id }) { _, item ->
                        DraggableItem(dragDropState = dragDropState, key = item.id) { isDragging, handleModifier ->
                            CollectionItem(
                                item = item,
                                onClick = onClick,
                                modifier = handleModifier
                            )
                        }
                    }
                }

            }
        }
        if(showCollectionDialog)
            CreateCollectionDialog(
                onConfirm =  onAddClick,
                onDismiss = {showCollectionDialog = false}
            )


    }
}


@Composable
fun CollectionItem(
    item : CollectionCover,
    onClick : (CollectionCover) -> Unit,
    modifier: Modifier = Modifier
) {

    Box(
        modifier = Modifier.height(120.dp).fillMaxWidth().padding(4.dp)
            .clickable{
                onClick(item)
            }
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
                Text(
                    text = item.count.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)
                )
                if(item.isSynced)
                Icon(
                    painter = painterResource(R.drawable.ic_synced_collections),
                    contentDescription = stringResource(R.string.synced),
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                )

                // Drag handle, drawn last so it sits on top
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight()
                        .width(56.dp)
                        .then(modifier),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        painter = painterResource(R.drawable.ic_drag_handle),
                        contentDescription = "drag handle"
                    )
                }
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
        count = 45,
        isAutoWallpaper = false
    )
    CollectionItem(item,
        onClick = {})
}
