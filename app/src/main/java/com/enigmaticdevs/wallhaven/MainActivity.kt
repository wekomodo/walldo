package com.enigmaticdevs.wallhaven

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.enigmaticdevs.wallhaven.navigation.AppNavigation
import com.enigmaticdevs.wallhaven.ui.screens.homescreen.HomeScreen
import com.enigmaticdevs.wallhaven.ui.theme.WallhavenTheme
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory


val LocalToastTrigger = staticCompositionLocalOf<(String) -> Unit> {
    error("No Toast Trigger provided")
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appGraph = (application as MyApp).appGraph
        val metroViewModelFactory = appGraph.metroViewModelFactory
        val globalFilterRepository = appGraph.provideGlobalFiltersRepository()
        enableEdgeToEdge()
        setContent {
            CompositionLocalProvider( LocalMetroViewModelFactory provides metroViewModelFactory) {
                WallhavenTheme {
                    AppNavigation(globalFilterRepository)
                }

            }

        }
    }
}
