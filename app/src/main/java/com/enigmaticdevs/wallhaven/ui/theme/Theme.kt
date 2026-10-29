package com.enigmaticdevs.wallhaven.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.enigmaticdevs.wallhaven.ui.screens.settings.SettingsViewModel
import com.materialkolor.DynamicMaterialExpressiveTheme
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicMaterialThemeState
import dev.zacsweers.metrox.viewmodel.metroViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.input.nestedscroll.NestedScrollSource.Companion.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WallhavenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    viewmodel : SettingsViewModel = metroViewModel(),
    content: @Composable () -> Unit
    ) {
    //enable dynamic theme
    val activeSeedColor = rememberDynamicSeedColor(fallbackSeed = SeedColor)

    //change theme based on settings
    val uiState = viewmodel.uiState.collectAsState()
    val isDark = when (uiState.value.theme) {
        ThemeMode.System -> darkTheme
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    val view = LocalView.current

    // Update the status bar styling whenever the theme changes
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !isDark
                isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    val dynamicThemeState = rememberDynamicMaterialThemeState(
        isDark = isDark,
        style = PaletteStyle.Expressive,
        specVersion = ColorSpec.SpecVersion.SPEC_2025,
        seedColor = activeSeedColor,
    )
    DynamicMaterialExpressiveTheme(
        state = dynamicThemeState,
        motionScheme = MotionScheme.expressive(),
        animate = true,
        content = content,
        typography = Typography
    )
}

@Composable
fun rememberDynamicSeedColor(fallbackSeed: Color): Color {
    val context = LocalContext.current
    return remember(context, fallbackSeed) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Fetch Android's core wallpaper-generated accent color
            val colorInt = ContextCompat.getColor(context, android.R.color.system_accent1_500)
            Color(colorInt)
        } else {
            fallbackSeed
        }
    }
}

enum class ThemeMode {
    System, Light, Dark;

    companion object {
        fun from(name: String?) = entries.firstOrNull { it.name == name } ?: System
    }
}