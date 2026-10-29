package com.enigmaticdevs.wallhaven.ui.screens.features.autowallpaper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.enigmaticdevs.wallhaven.R
import com.enigmaticdevs.wallhaven.ui.presentation.CategoryTitle
import com.enigmaticdevs.wallhaven.ui.presentation.ClickablePrefWithButton
import com.enigmaticdevs.wallhaven.ui.presentation.ClickablePrefWithCheckbox
import com.enigmaticdevs.wallhaven.ui.presentation.PrefTopAppBar
import com.enigmaticdevs.wallhaven.ui.presentation.asSource
import kotlinx.coroutines.Job


@Composable
fun AutoWallpaperRoute(onOpenDrawer: () -> Job) {

    AutoWallpaperContent(
        onBack = {  }
    )
}


@Composable
fun AutoWallpaperContent(
    onBack : () -> Unit
) {

    val snackBarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
      //  viewModel.events.collect { snackBarHostState.showSnackbar(it) }
        //  viewModel.getCurrentKey()
    }
    Scaffold(
        topBar = {
            PrefTopAppBar("Auto Wallpaper") {
                onBack()
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues),

            ) {
            ClickablePrefWithButton(
                isChecked = false,
                text = "Auto Wallpaper",
                description = "Change wallpaper automatically",
                imageSource = R.drawable.ic_auto_wallpaper.asSource(),
                onCheckedChange = {}
            )
            HorizontalDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 12.dp, bottom = 8.dp)
                    .wrapContentHeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = "info"
                )
                Text("Random wallpapers with your specified conditions")
            }
            HorizontalDivider()
            CategoryTitle("Conditions")
           /* ClickablePrefWithCheckbox(
                isChecked = false,
                text = R.string.onWifi
            ) { }
*/


        }
    }

}


@Preview
@Composable
fun Preview() {
    AutoWallpaperContent(onBack = {})
}
