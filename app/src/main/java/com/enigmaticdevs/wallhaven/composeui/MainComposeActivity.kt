package com.enigmaticdevs.wallhaven.composeui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.enigmaticdevs.wallhaven.composeui.navigation.SetupNavGraph
import com.enigmaticdevs.wallhaven.composeui.ui.theme.AppTheme
import com.enigmaticdevs.wallhaven.domain.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
@AndroidEntryPoint
class MainComposeActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var navController : NavHostController
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                navController =  rememberNavController()
                SetupNavGraph(navController = navController)
                // A surface container using the 'background' color from the theme

            }
        }
    }
}

/*
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WallhavenTheme {
        MainScreen()
    }
}*/
