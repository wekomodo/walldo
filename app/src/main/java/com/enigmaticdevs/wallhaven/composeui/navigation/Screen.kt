package com.enigmaticdevs.wallhaven.composeui.navigation

sealed class Screen(val route: String){
    data object Home : Screen (route = "home_screen")
    data object Search : Screen (route = "search_screen")
}
