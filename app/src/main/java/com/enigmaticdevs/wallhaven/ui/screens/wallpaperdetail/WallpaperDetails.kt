package com.enigmaticdevs.wallhaven.ui.screens.wallpaperdetail

import android.content.ClipData
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Size
import com.enigmaticdevs.wallhaven.LocalToastTrigger
import com.enigmaticdevs.wallhaven.R
import com.enigmaticdevs.wallhaven.data.remote.model.Tag
import com.enigmaticdevs.wallhaven.data.remote.model.WallpaperDetail
import com.enigmaticdevs.wallhaven.ui.screens.wallpaperdetail.bottomsheet.CollectionsSheet
import com.enigmaticdevs.wallhaven.ui.screens.wallpaperdetail.bottomsheet.InfoBottomSheet
import com.enigmaticdevs.wallhaven.ui.util.DownloadListener
import com.enigmaticdevs.wallhaven.ui.util.DownloadUtils
import com.enigmaticdevs.wallhaven.ui.util.PhotoView
import com.enigmaticdevs.wallhaven.ui.util.downloadWallpaper
import com.enigmaticdevs.wallhaven.ui.util.rememberDownloadStarter
import com.enigmaticdevs.wallhaven.ui.util.setAsWallpaperWithSystem
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException


private val TAG = "WallpaperDetailScreen"

@Composable
fun WallpaperDetailScreen(
    wallpaperId: String,
    onBack: () -> Unit,
    onTagClick: (Tag) -> Unit,
    viewModel: WallpaperDetailViewModel = metroViewModel()
) {
    Log.d("$TAG(wallpaperId)", wallpaperId)
    val wallpaper = viewModel.wallpaper.collectAsState()
    val memberships by viewModel.memberships.collectAsStateWithLifecycle()
    var showSheet by remember { mutableStateOf(false) }
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var isPreparing by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val showToast = LocalToastTrigger.current

    LaunchedEffect(wallpaperId) { viewModel.getWallpaperDetails(wallpaperId) }
    WallpaperDetailScreenContent(
        isFavorite = isFavorite,
        onFavoriteClick = { showSheet = true },
        wallpaper = wallpaper.value,
        onBack = onBack,
        onTagClick = onTagClick,
        onSetAsWallpaperClick = {
            wallpaper.value?.let { w ->
                if (!isPreparing) scope.launch {
                    isPreparing = true
                    try {
                        val uri = viewModel.shareableWallpapers.uriFor(w.data.id, w.data.path)
                        setAsWallpaperWithSystem(context, uri)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.e(TAG, "Set wallpaper failed", e)
                        showToast("Couldn't prepare the wallpaper")
                    } finally {
                        isPreparing = false
                    }
                }
            }

        })
    if (isPreparing) PreparingOverlay()

    if (showSheet) {
        CollectionsSheet(
            memberships = memberships,
            onToggle = viewModel::setMembership,
            onCreate = viewModel::createCollectionAndAdd,
            onDismiss = { showSheet = false }
        )
    }
}

@Composable
fun WallpaperDetailScreenContent(
    isFavorite: Boolean?,
    onFavoriteClick: () -> Unit,
    wallpaper: WallpaperDetail?,
    onBack: () -> Unit,
    onTagClick: (Tag) -> Unit,
    onSetAsWallpaperClick: () -> Unit
) {
    var showInfoSheet by remember { mutableStateOf(false) }
    var currentDownload by remember { mutableLongStateOf(-1L) }
    var showRetry by remember { mutableStateOf(false) }
    var attempt by remember { mutableIntStateOf(0) }
    val startDownload = rememberDownloadStarter()
    val scope = rememberCoroutineScope()
    val showToast = LocalToastTrigger.current
    val context = LocalContext.current
    val clipboard = LocalClipboard.current

    if (currentDownload != -1L) {
        DownloadListener(currentDownload) { uri ->
            when {
                uri == null -> showToast("Download Failed")
                else -> showToast("Saved to Pictures/Walldo")
            }
            currentDownload = -1L
        }
    }

    fun runDownload(w: WallpaperDetail) {
        startDownload {
            when (val result = downloadWallpaper(context, w.data.path, w.data.id)) {
                is DownloadUtils.AlreadyDownloaded -> showToast("Already Downloaded")
                is DownloadUtils.Started -> {
                    currentDownload = result.id
                    showToast("Download Started")
                }
            }
        }
    }
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
                        wallpaper?.let {
                            runDownload(it)
                        }
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
        floatingActionButton = {
            ExtendedFloatingActionButton(
                modifier = Modifier
                    .padding(end = 8.dp, bottom = 8.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                onClick = {
                    onSetAsWallpaperClick()
                },
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_wallpaper),
                        contentDescription = "Info"
                    )
                },
                text = { Text("Set as wallpaper") }

            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                .clip(RoundedCornerShape(24.dp))
        ) {
            if (wallpaper == null) {
                CenteredLoadingIndicator()
            } else {
                val context = LocalContext.current
                val request = remember(wallpaper.data.path, attempt) {
                    ImageRequest.Builder(context)
                        .data(wallpaper.data.path)
                        .crossfade(true)
                        .size(Size.ORIGINAL)
                        .placeholderMemoryCacheKey(wallpaper.data.id)
                        .listener(
                            onError = { _, result ->
                                Log.e(
                                    "WallpaperImage",
                                    "Load failed: ${wallpaper.data.path}",
                                    result.throwable
                                )
                            },
                            onCancel = { Log.w("WallpaperImage", "Load cancelled") },
                        )
                        .build()
                }
                PhotoView(
                    model = request,
                    fillScreen = true,
                    modifier = Modifier.fillMaxSize()
                )
                // Top end: add to collection / favorite state
                FilledTonalIconButton(
                    onClick = onFavoriteClick,
                    enabled = isFavorite != null,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_bookmark_heart),
                        contentDescription = "Add to collection",
                    )
                }

                // Bottom start: palette. Vertical so it never competes with the FAB for width.
                CircleColorPalette(
                    colors = wallpaper.data.colors,
                    onClick = { hex -> copyToClipboard(clipboard, hex, scope) },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                )

                // Bottom end: primary action
                ExtendedFloatingActionButton(
                    onClick = onSetAsWallpaperClick,
                    icon = {
                        Icon(painterResource(R.drawable.ic_wallpaper), contentDescription = null)
                    },
                    text = { Text("Set as wallpaper") },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                )
            }
        }
    }
    if (showInfoSheet && wallpaper != null)
        InfoBottomSheet(
            wallpaperInfo = wallpaper.data,
            onTagClick = onTagClick,
            onDismiss = {
                showInfoSheet = false
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


fun copyToClipboard(clipboard: Clipboard, color: String, scope: CoroutineScope) {
    scope.launch {
        clipboard.setClipEntry(
            ClipEntry(
                ClipData.newPlainText(
                    "Wallpaper Color",
                    color
                )
            )
        )
        //   Toast.makeText(context, "$color copied!", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun CircleColorPalette(
    colors: List<String>,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            colors.forEach { hex ->
                val color = runCatching { Color(hex.toColorInt()) }
                    .getOrDefault(Color.Transparent)

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color)
                        // thin outline so very light/dark swatches don't vanish
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            CircleShape
                        )
                        .clickable(onClickLabel = "Copy $hex") { onClick(hex) }
                )
            }
        }
    }
}

@Composable
fun PreparingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .pointerInput(Unit) {}   // swallows touches so nothing underneath is clickable
    ) {
        CenteredLoadingIndicator()
    }
}

@Preview
@Composable
fun WallpaperDetailContentPreview() {
    // WallpaperDetailScreenContent(null, onBack = { Screen.Home}, onTagClick = {})
}