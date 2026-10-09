package com.enigmaticdevs.wallhaven.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable


sealed interface Screen : NavKey {

    @Serializable data object Home : Screen
    @Serializable data object Search : Screen
    @Serializable data object Settings : Screen

    @Serializable data class WallpaperDetail(val wallpaperId: String) : Screen
}