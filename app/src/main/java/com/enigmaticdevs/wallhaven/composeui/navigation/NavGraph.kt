package com.enigmaticdevs.wallhaven.composeui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.enigmaticdevs.wallhaven.composeui.screen.HomeScreen
import com.enigmaticdevs.wallhaven.composeui.screen.SearchScreen

@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ){
        composable(
            route = Screen.Home.route
        ){
            HomeScreen(navController = navController)
        }
        composable(
            route = Screen.Search.route
        ){
            SearchScreen()
        }

    }
}
