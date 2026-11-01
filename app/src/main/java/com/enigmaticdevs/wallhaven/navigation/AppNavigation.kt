package com.enigmaticdevs.wallhaven.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.SaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.enigmaticdevs.wallhaven.LocalToastTrigger
import com.enigmaticdevs.wallhaven.data.repository.GlobalFilterRepository
import com.enigmaticdevs.wallhaven.ui.presentation.DynamicToastOverlay
import com.enigmaticdevs.wallhaven.ui.presentation.FloatingNavBar
import com.enigmaticdevs.wallhaven.ui.screens.features.autowallpaper.AutoWallpaperRoute
import com.enigmaticdevs.wallhaven.ui.screens.features.favorites.FavoritesRoute
import com.enigmaticdevs.wallhaven.ui.screens.features.login.WallhavenLoginRoute
import com.enigmaticdevs.wallhaven.ui.screens.homescreen.HomeScreen
import com.enigmaticdevs.wallhaven.ui.screens.settings.SettingsScreen
import com.enigmaticdevs.wallhaven.ui.screens.wallpaperdetail.WallpaperDetailScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.milliseconds


sealed interface Screen : NavKey {
    @Serializable
    data object Home : Screen

    @Serializable
    data object AutoWallpaper : Screen // New Tab

    @Serializable
    data object Favorites : Screen // New Tab

    @Serializable
    data object Settings : Screen

    @Serializable
    data object WallhavenLogin : Screen

    @Serializable
    data class WallpaperDetail(val wallpaperId: String) : Screen
}

@Composable
fun AppNavigation() {
    val nav = remember { TopLevelBackStack<Screen>(Screen.Home) }
    val tabs = listOf(Screen.Home, Screen.Favorites, Screen.AutoWallpaper, Screen.Settings)
    val showBar = nav.backStack.lastOrNull() in tabs
    val scope = rememberCoroutineScope()

    //global custom toast
    var toastMessage by remember { mutableStateOf("") }
    var isToastVisible by remember { mutableStateOf(false) }
    CompositionLocalProvider(
        LocalToastTrigger provides { message ->
            toastMessage = message
            isToastVisible = true
            scope.launch {
                delay(2500.milliseconds)
                isToastVisible = false
            }
        },
    ) {
        Box(Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)) {
            NavDisplay(
                backStack = nav.backStack,
                onBack = { nav.removeLast() },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()   // needs lifecycle-viewmodel-navigation3
                ),
                entryProvider = entryProvider(
                    fallback = { key ->
                        NavEntry(key) {
                            Text("No entry for key ${key::class}")
                        }
                    }
                ) {
                    entry<Screen.Home> {
                        HomeScreen(
                            onPhotoClick = { id -> nav.add(Screen.WallpaperDetail(id)) }
                            // onOpenDrawer is gone along with the drawer
                            , onBack = {}
                        )
                    }

                    entry<Screen.Favorites> {
                        FavoritesRoute() {

                        }
                    }

                    entry<Screen.AutoWallpaper> {
                        AutoWallpaperRoute() {

                        }
                    }

                    entry<Screen.Settings> {
                        SettingsScreen(onBack = { nav.switchTopLevel(Screen.Home) })
                    }

                    entry<Screen.WallpaperDetail> { route ->
                        WallpaperDetailScreen(
                            wallpaperId = route.wallpaperId,
                            onBack = { nav.removeLast() },
                            onTagClick = { nav.switchTopLevel(Screen.Home) }
                        )
                    }

                    entry<Screen.WallhavenLogin> {
                        WallhavenLoginRoute(onBack = { nav.switchTopLevel(Screen.Home) })
                    }
                }
            )

            if (showBar) {
                FloatingNavBar(
                    selectedIndex = tabs.indexOf(nav.topLevelKey),
                    onSelect = { nav.switchTopLevel(tabs[it]) },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
    DynamicToastOverlay(isVisible = isToastVisible, message = toastMessage)
}

class TopLevelBackStack<T : NavKey>(startKey : T){
    private val stacks = LinkedHashMap<T, SnapshotStateList<T>>()
        .apply { put(startKey,mutableStateListOf(startKey)) }

    var topLevelKey by mutableStateOf(startKey)
        private set

    val backStack = mutableStateListOf(startKey)

    private fun update() {
        backStack.clear()
        backStack.addAll(stacks.flatMap { it.value })
    }

    fun switchTopLevel(key: T) {
        val existing = stacks.remove(key)          // move tab to the end so it's on top
        stacks[key] = existing ?: mutableStateListOf(key)
        topLevelKey = key
        update()
    }

    fun add(key: T) {
        stacks[topLevelKey]?.add(key)
        update()
    }

    fun removeLast() {
        val stack = stacks[topLevelKey] ?: return
        stack.removeLastOrNull()
        if (stack.isEmpty()) {
            stacks.remove(topLevelKey)
            topLevelKey = stacks.keys.last()
        }
        update()
    }
}