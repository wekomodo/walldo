package com.enigmaticdevs.wallhaven.ui.screens.wallpaperdetail

import android.content.ClipData
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.enigmaticdevs.wallhaven.R
import com.enigmaticdevs.wallhaven.data.model.WallpaperDetail
import com.enigmaticdevs.wallhaven.navigation.Screen
import com.enigmaticdevs.wallhaven.ui.screens.wallpaperdetail.bottomsheet.InfoBottomSheet
import com.enigmaticdevs.wallhaven.ui.util.PhotoView
import com.wekomodo.huntshowdownwiki.ui.components.ZoomableBox
import dev.zacsweers.metrox.viewmodel.metroViewModel
import androidx.core.graphics.toColorInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


private val TAG = "WallpaperDetailScreen"

@Composable
fun WallpaperDetailScreen(
    wallpaperId: String,
    onBack: () -> Screen,
    onTagClick : () -> Unit,
    viewModel: WallpaperDetailViewModel = metroViewModel()
) {
    Log.d("$TAG(wallpaperId)", wallpaperId)
    LaunchedEffect(wallpaperId) {
        viewModel.getWallpaperDetails(wallpaperId)
    }
    val wallpaper = viewModel.wallpaper.collectAsState().value
    WallpaperDetailScreenContent(wallpaper, onBack, onTagClick = { tag ->
        viewModel.searchTag(tag)
        onTagClick()
    })
}

@Composable
fun WallpaperDetailScreenContent(wallpaper: WallpaperDetail?, onBack: () -> Screen,onTagClick: (String) -> Unit) {
    var showInfoSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val clipboardManager = LocalClipboard.current
    var favorite by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
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
                Box(
                    modifier = Modifier
                        .fillMaxHeight(.92f)
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .shadow(elevation = 8.dp, shape = RoundedCornerShape(8.dp))
                        // 2. Clip the content to match the shadow's shape
                        .clip(RoundedCornerShape(8.dp))
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
                            InfoBottomSheet(wallpaperInfo = wallpaper.data,
                                onTagClick = onTagClick,
                                onDismiss = {
                                showInfoSheet = false
                            })
                    } ?: run {
                        CenteredLoadingIndicator()
                    }

                    ExtendedFloatingActionButton(
                        modifier = Modifier
                            .padding(end = 8.dp, bottom = 8.dp)
                            .align(Alignment.BottomEnd),
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp, start = 8.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(.8f)
                    )
                    {
                        wallpaper?.let {
                            CircleColorPalette(wallpaper.data.colors) {
                                copyToClipboard(clipboardManager, it, scope, context)
                            }
                        } ?: run {
                            CenteredLoadingIndicator()
                        }
                    }
                    FilledTonalIconButton(
                        modifier = Modifier.fillMaxSize(),
                        onClick = {
                            favorite= !favorite
                            Toast.makeText(context, "Favorite Clicked : $favorite", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        if (favorite)
                            Icon(
                                painterResource(R.drawable.ic_heart_filled),
                                contentDescription = "Favorite Icon"
                            )
                        else
                            Icon(
                                painterResource(R.drawable.ic_heart_outlined),
                                contentDescription = "Favorite Icon"
                            )
                    }

                    /*Spacer(
                        modifier = Modifier.weight(1f)
                    )*/
                }
            }
        })
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CenteredLoadingIndicator() {
    Box(modifier = Modifier.fillMaxSize()) {
        ContainedLoadingIndicator(
            modifier = Modifier.align(Alignment.Center)
        )
    }
}


fun copyToClipboard(clipboard: Clipboard, color: String, scope: CoroutineScope, context: Context) {
    scope.launch {
        clipboard.setClipEntry(
            ClipEntry(
                ClipData.newPlainText(
                    "Wallpaper Color",
                    color
                )
            )
        )
        Toast.makeText(context, "$color copied!", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun CircleColorPalette(colors: List<String>, onClick: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        colors.forEach { hexString ->
            // 2. Convert the Hex string to a Compose Color safely
            val color = try {
                Color(hexString.toColorInt())

            } catch (e: Exception) {
                Color.Transparent // Fallback in case of a bad hex code
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)// Size of your circles
                    .aspectRatio(1f)
                    .clip(CircleShape) // Makes it perfectly round
                    .background(color = color)
                    .clickable {
                        // 4. Copy to clipboard on tap
                        onClick(hexString)
                    }

            )
        }
    }
}

@Preview
@Composable
fun WallpaperDetailContentPreview() {
    WallpaperDetailScreenContent(null, onBack = { Screen.Home}, onTagClick = {})
}