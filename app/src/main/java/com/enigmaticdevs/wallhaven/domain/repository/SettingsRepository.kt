package com.enigmaticdevs.wallhaven.domain.repository

import android.content.Context
import android.util.Log
import androidx.activity.contextaware.ContextAware
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.enigmaticdevs.wallhaven.data.model.AuthenticateAPIkey
import com.enigmaticdevs.wallhaven.data.remote.WallhavenAPI
import com.enigmaticdevs.wallhaven.di.dataStore
import com.enigmaticdevs.wallhaven.ui.screens.homescreen.components.TAG
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_preferences")


object PreferenceKeys {
    val THEME = stringPreferencesKey("theme")
    val API_KEY = stringPreferencesKey("api_key")
    val WALLPAPER_SETTER = stringPreferencesKey("wallpaper_setter") // Native vs Custom
}

@Inject
@SingleIn(AppScope::class)
class SettingsRepository (
    private val context : Context,
    private val api : WallhavenAPI
){
    val themeFlow : Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferenceKeys.THEME] ?: "system" //default system
    }

    val apiKeyFlow : Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferenceKeys.API_KEY].orEmpty()
    }

    val wallpaperSetterFlow : Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferenceKeys.WALLPAPER_SETTER] ?: "native"
    }

    suspend fun updateTheme(newTheme : String){
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.THEME] = newTheme
        }
    }

    suspend fun updateApiKey(key : String) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.API_KEY] = key
        }
    }
    suspend fun updateSetter(setter : String){
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.WALLPAPER_SETTER] = setter
        }
    }

    suspend fun authenticateAPIkey(key: String): AuthenticateAPIkey? {
       return api.authenticateApiKey(key)
    }

}