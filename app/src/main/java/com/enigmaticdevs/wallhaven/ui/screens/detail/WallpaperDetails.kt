package com.enigmaticdevs.wallhaven.ui.screens.detail

import android.content.ClipData
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
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
import androidx.core.graphics.toColorInt
import kotlinx.coroutines.launch


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
    WallpaperDetailScreenContent(wallpaper, onBack)
}

@Composable
fun WallpaperDetailScreenContent(wallpaper: WallpaperDetail?, onBack: () -> Screen) {
    var showInfoSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val clipboardManager = LocalClipboard.current
    val scope = rememberCoroutineScope() // 1. Grab the scope
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(0.8f)
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

                        if (showInfoSheet)
                            InfoBottomSheet(wallpaper.data) {
                                showInfoSheet = false
                            }
                    }



                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Row(
                       //  modifier = Modifier.fillMaxWidth().weight(.2f)
                    ) {

                        wallpaper?.let {
                            wallpaper.data.colors.forEach { hexString ->
                                // 2. Convert the Hex string to a Compose Color safely
                                val color = try {
                                    Color(hexString.toColorInt())

                                } catch (e: Exception) {
                                    Color.Transparent // Fallback in case of a bad hex code
                                }
                                CircleColorPalette(color) {
                                    scope.launch {
                                        clipboardManager.setClipEntry(
                                            ClipEntry(
                                                ClipData.newPlainText(
                                                    "Wallpaper Color",
                                                    hexString
                                                )
                                            )
                                        )
                                        Toast.makeText(context,"$hexString copied!", Toast.LENGTH_SHORT).show()
                                    }
                                }

                            }

                        }
                        Spacer(
                            modifier = Modifier.weight(1f)
                        )

                        ExtendedFloatingActionButton(
                            modifier = Modifier.padding(end = 8.dp, bottom = 8.dp),
                            elevation = FloatingActionButtonDefaults.elevation(8.dp),
                            onClick = {},
                            icon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_wallpaper),
                                    contentDescription = "Info"
                                )
                            },
                            text = { Text("Set as wallpaper") }

                        )

                    }
                }
            }
        })
}

@Composable
fun CircleColorPalette(color: Color, onClick: (Color) -> Unit) {

    Box(
        modifier = Modifier
            .size(48.dp)
            .padding(4.dp)// Size of your circles
            .clip(CircleShape) // Makes it perfectly round
            .background(color = color)
            .clickable {
                // 4. Copy to clipboard on tap
                onClick(color)
            }
    )
}

@Preview
@Composable
fun WallpaperDetailContentPreview() {
    WallpaperDetailScreenContent(null, onBack = { Screen.Home })
}