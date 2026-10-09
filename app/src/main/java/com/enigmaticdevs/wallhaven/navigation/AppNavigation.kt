package com.enigmaticdevs.wallhaven.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.enigmaticdevs.wallhaven.domain.repository.GlobalFilterRepository
import com.enigmaticdevs.wallhaven.ui.screens.detail.WallpaperDetailScreen
import com.enigmaticdevs.wallhaven.ui.screens.homescreen.HomeScreen
import com.enigmaticdevs.wallhaven.ui.screens.searchscreen.SearchScreen
import com.enigmaticdevs.wallhaven.ui.screens.settings.SettingsScreen


@Composable
fun AppNavigation(
    globalFilterRepository: GlobalFilterRepository
) {
    val backStack = remember { mutableStateListOf<Screen>(Screen.Home) }

    NavDisplay(
        backStack = backStack,
        onBack = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) },
        // Use entryProvider block to securely map key models to composable items
        entryProvider = entryProvider {

            entry<Screen.Home> {
                HomeScreen(
                    globalFilterRepository = globalFilterRepository,
                    onNavigateToSearch = { backStack.add(Screen.Search) },
                    onNavigateToSettings = { backStack.add(Screen.Settings) },
                    onPhotoClick = { id -> backStack.add(Screen.WallpaperDetail(id)) }
                )
            }

            entry<Screen.Search> {
                SearchScreen(
                    onWallpaperClick = { id -> backStack.add(Screen.WallpaperDetail(id)) },
                    onBack = { backStack.removeAt(backStack.lastIndex) }
                )
            }

            entry<Screen.Settings> {
                SettingsScreen(
                    //globalFilterRepository = globalFilterRepository,
                    onBack = { backStack.removeAt(backStack.lastIndex) }
                )
            }

            // Extract route properties securely for parametric screens
            entry<Screen.WallpaperDetail> { route ->
                WallpaperDetailScreen(
                    wallpaperId = route.wallpaperId,
                    onBack = {backStack.removeAt(backStack.lastIndex) }
                )
            }
        }
    )
}
