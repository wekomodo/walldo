package com.enigmaticdevs.wallhaven.ui.screens.detail

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.enigmaticdevs.wallhaven.R
import com.enigmaticdevs.wallhaven.data.model.WallpaperDetail
import com.enigmaticdevs.wallhaven.domain.viewmodels.WallpaperDetailViewModel
import com.enigmaticdevs.wallhaven.navigation.Screen
import com.enigmaticdevs.wallhaven.ui.presentation.InfoBottomSheet
import com.enigmaticdevs.wallhaven.ui.util.PhotoView
import com.wekomodo.huntshowdownwiki.ui.components.ZoomableBox
import dev.zacsweers.metrox.viewmodel.metroViewModel


private val TAG = "WallpaperDetailScreen"

@Composable
fun WallpaperDetailScreen(
    wallpaperId: String,
    onBack: () -> Screen,
    viewModel: WallpaperDetailViewModel = metroViewModel()
) {
    Log.d("$TAG(wallpaperId)", wallpaperId)
    LaunchedEffect(wallpaperId) {
        viewModel.getWallpaperDetails(wallpaperId)
    }
    val wallpaper = viewModel.wallpaper.collectAsState().value
    WallpaperDetailScreenContent(wallpaper,onBack)
}

@Composable
fun WallpaperDetailScreenContent(wallpaper: WallpaperDetail?, onBack: () -> Screen) {
    var showInfoSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(),
                title = { Text("Details") },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Localized description"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {

                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_download),
                            contentDescription = "Localized description"
                        )
                    }
                    IconButton(onClick = {
                        showInfoSheet = true
                        Toast.makeText(context, "Info Sheet Shown", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = "Localized description"
                        )
                    }
                }
            )
        },
        content = { paddingValues ->
           // Text(modifier = Modifier.padding(paddingValues), text = "")
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {

                    wallpaper?.let {
                        ZoomableBox(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            PhotoView(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(wallpaper.data.path).crossfade(true)
                                    .placeholderMemoryCacheKey(wallpaper.data.id).build(),
                               // contentDescription = "Wallpaper Image",
                                fillScreen = true,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        if(showInfoSheet)
                            InfoBottomSheet(wallpaper.data){
                                showInfoSheet = false
                            }
                    }



                }
            }
        })

}

@Preview
@Composable
fun WallpaperDetailContentPreview() {
    WallpaperDetailScreenContent(null, onBack =  { Screen.Home})
}