package com.enigmaticdevs.wallhaven.ui.screens.features.autowallpaper

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enigmaticdevs.wallhaven.R
import com.enigmaticdevs.wallhaven.data.repository.AutoWallpaperState
import com.enigmaticdevs.wallhaven.ui.presentation.CategoryTitle
import com.enigmaticdevs.wallhaven.ui.presentation.ClickablePrefWithButton
import com.enigmaticdevs.wallhaven.ui.presentation.PrefTopAppBar
import com.enigmaticdevs.wallhaven.ui.presentation.UiText.DynamicString
import com.enigmaticdevs.wallhaven.ui.presentation.UiText.ResourceString
import com.enigmaticdevs.wallhaven.ui.presentation.asSource
import dev.zacsweers.metrox.viewmodel.metroViewModel


@Composable
fun AutoWallpaperRoute(
    onBack: () -> Unit,
    viewModel: AutoWallpaperViewModel = metroViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AutoWallpaperContent(
        state = state,
        onBack = onBack,
        onSwitchUpdate = { viewModel.updateSwitch(!state.switch) },
        onWifiChange = { viewModel.updateOnWifi(!state.onWifi) },
        onChargingChange = { viewModel.updateOnCharging(!state.onCharging) },
        onIdleChange = { viewModel.updateOnIdle(!state.onIdle) }
    )
}


@Composable
fun AutoWallpaperContent(
    state: AutoWallpaperState,
    onBack: () -> Unit,
    onSwitchUpdate: () -> Unit,
    onWifiChange: () -> Unit,
    onChargingChange: () -> Unit,
    onIdleChange: () -> Unit
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
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),

            ) {
            ClickablePrefWithButton(
                isChecked = state.switch,
                text = ResourceString(R.string.autoWallpaper),
                description = ResourceString(R.string.autoWallpaperDesc),
                imageSource = R.drawable.ic_auto_wallpaper.asSource(),
                onCheckedChange = onSwitchUpdate,
                isEnabled = true
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
            CategoryTitle(ResourceString(R.string.autoWallpaper_category_conditions))
                ClickablePrefWithButton(
                    isChecked = state.onWifi,
                    text = ResourceString(R.string.onWifi),
                    description = ResourceString(R.string.onWifidesc),
                    imageSource =
                        if (state.onWifi) R.drawable.ic_wifi_on.asSource()
                        else
                            R.drawable.ic_wifi_off.asSource(),
                    onCheckedChange = onWifiChange,
                    isEnabled = state.switch
                )
                ClickablePrefWithButton(
                    isChecked = state.onCharging,
                    text = ResourceString(R.string.onCharing),
                    description = ResourceString(R.string.onChargingdesc),
                    imageSource = if (state.onCharging) R.drawable.ic_charging_on.asSource()
                    else
                        R.drawable.ic_charging_off.asSource(),
                    onCheckedChange = onChargingChange,
                    isEnabled = state.switch
                )
                ClickablePrefWithButton(
                    isChecked = state.onIdle,
                    text = ResourceString(R.string.onIdle),
                    description = ResourceString(R.string.onIdleDesc),
                    imageSource = if (state.onIdle) R.drawable.ic_idle_on.asSource()
                    else
                        R.drawable.ic_idle_off.asSource(),
                    onCheckedChange = onIdleChange,
                    isEnabled = state.switch
                )
        }
    }

}


fun stringFromBool(vararg options: Boolean): String {
    return options.joinToString("") { if (it) "1" else "0" }
}

fun boolFromString(bitmask: String): List<Boolean> {
    return bitmask.map { it == '1' }
}


@Preview
@Composable
fun Preview() {
    AutoWallpaperContent(
        AutoWallpaperState(),
        onBack = {},
        onSwitchUpdate = {},
        onWifiChange = {},
        onChargingChange = {},
        onIdleChange = {},
    )
}
