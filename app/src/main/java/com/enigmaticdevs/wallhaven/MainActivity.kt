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

   /* override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() = (application as MyApp).appGraph.metroViewModelFactory*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appGraph = (application as MyApp).appGraph

        enableEdgeToEdge()
        setContent {
            CompositionLocalProvider( LocalMetroViewModelFactory provides appGraph.metroViewModelFactory) {
                WallhavenTheme {
                    HomeScreen() {

                    }
                }

            }

        }
    }
}
