package com.enigmaticdevs.wallhaven.navigation

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.enigmaticdevs.wallhaven.LocalToastTrigger
import com.enigmaticdevs.wallhaven.R
import com.enigmaticdevs.wallhaven.data.repository.GlobalFilterRepository
import com.enigmaticdevs.wallhaven.ui.presentation.DynamicToastOverlay
import com.enigmaticdevs.wallhaven.ui.screens.homescreen.HomeScreen
import com.enigmaticdevs.wallhaven.ui.screens.settings.SettingsScreen
import com.enigmaticdevs.wallhaven.ui.screens.features.autowallpaper.AutoWallpaperRoute
import com.enigmaticdevs.wallhaven.ui.screens.features.favorites.FavoritesRoute
import com.enigmaticdevs.wallhaven.ui.screens.features.login.WallhavenLoginRoute
import com.enigmaticdevs.wallhaven.ui.screens.features.login.WallhavenLoginScreen
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
fun AppNavigation(
    globalFilterRepository: GlobalFilterRepository,

) {
    val backStack = remember { mutableStateListOf<Screen>(Screen.Home) }
    val currentScreen = backStack.lastOrNull()
    val context = LocalContext.current

    //drawerState
    val drawerState = rememberDrawerState(DrawerValue.Closed)
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
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Walldo",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    HorizontalDivider()

                    NavigationDrawerItem(
                        label = { Text("Home") },
                        selected = currentScreen is Screen.Home,
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (currentScreen !is Screen.Home) {
                                backStack.clear()
                                backStack.add(Screen.Home)
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )

                    NavigationDrawerItem(
                        label = { Text("Auto-Wallpaper") },
                        selected = currentScreen is Screen.AutoWallpaper,
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_auto_wallpaper),
                                contentDescription = "Auto"
                            )
                        },
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (currentScreen !is Screen.AutoWallpaper) {
                                backStack.add(Screen.AutoWallpaper)
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )

                    NavigationDrawerItem(
                        label = { Text("Favorites") },
                        selected = currentScreen is Screen.Favorites,
                        icon = { Icon(Icons.Filled.Favorite, contentDescription = "Favorites") },
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (currentScreen !is Screen.Favorites) {
                                backStack.add(Screen.Favorites)
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    NavigationDrawerItem(
                        label = { Text("Settings") },
                        selected = currentScreen is Screen.Settings,
                        icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (currentScreen !is Screen.Settings) {
                                backStack.add(Screen.Settings)
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                    NavigationDrawerItem(
                        label = { Text("Login") },
                        selected = currentScreen is Screen.WallhavenLogin,
                        icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (currentScreen !is Screen.WallhavenLogin) {
                                backStack.add(Screen.WallhavenLogin)
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        ) {

            NavDisplay(
                backStack = backStack,
                onBack = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) },
                // Use entryProvider block to securely map key models to composable items
                entryProvider = entryProvider {

                    entry<Screen.Home> { route ->
                        HomeScreen(
                            onPhotoClick = { id -> backStack.add(Screen.WallpaperDetail(id)) },
                            onOpenDrawer = {
                                scope.launch { drawerState.open() }
                            }
                        )
                    }

                    entry<Screen.AutoWallpaper> {
                        AutoWallpaperRoute(
                            onOpenDrawer = {
                                scope.launch { drawerState.open() }
                            }
                            //onBack = { backStack.removeAt(backStack.lastIndex)}
                        )
                    }
                    entry<Screen.Favorites> {
                        FavoritesRoute(
                            onOpenDrawer = {
                                scope.launch { drawerState.open() }
                            }
                        )
                    }
                    entry<Screen.Settings> {
                        SettingsScreen(
                            //globalFilterRepository = globalFilterRepository,
                            onBack = {
                                backStack.removeAt(backStack.lastIndex)
                            }
                        )
                    }

                    // Extract route properties securely for parametric screens
                    entry<Screen.WallpaperDetail> { route ->
                        WallpaperDetailScreen(
                            wallpaperId = route.wallpaperId,
                            onBack = { backStack.removeAt(backStack.lastIndex) },
                            onTagClick = {
                                backStack.clear()
                                backStack.add(Screen.Home)
                            }
                        )
                    }
                    entry<Screen.WallhavenLogin> {
                        WallhavenLoginRoute(
                            onBack = {  backStack.clear()
                                backStack.add(Screen.Home) }
                        )
                    }
                }
            )
        }
        DynamicToastOverlay(isVisible = isToastVisible, message = toastMessage)
    }
}

