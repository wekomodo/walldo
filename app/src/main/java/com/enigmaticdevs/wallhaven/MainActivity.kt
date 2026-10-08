package com.enigmaticdevs.wallhaven

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.enigmaticdevs.wallhaven.ui.screens.homescreen.HomeScreen
import com.enigmaticdevs.wallhaven.ui.theme.WallhavenTheme
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val metroViewModelFactory = (application as MyApp).appGraph.metroViewModelFactory

        enableEdgeToEdge()
        setContent {
            CompositionLocalProvider( LocalMetroViewModelFactory provides metroViewModelFactory) {
                WallhavenTheme {
                    HomeScreen() {

                    }
                }

            }

        }
    }
}
