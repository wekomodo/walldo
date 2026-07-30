package com.enigmaticdevs.wallhaven

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import com.enigmaticdevs.wallhaven.ui.screens.HomeScreen
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
                    HomeScreen()
                }

            }

        }
    }
}
