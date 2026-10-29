package com.enigmaticdevs.wallhaven.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.enigmaticdevs.wallhaven.data.remote.model.AuthenticateAPIkey
import com.enigmaticdevs.wallhaven.data.remote.WallhavenAPI
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map



object PreferenceKeys {
    val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
    val THEME = stringPreferencesKey("theme")
    val API_KEY = stringPreferencesKey("api_key")
    val WALLPAPER_SETTER = stringPreferencesKey("wallpaper_setter") // Native vs Custom
}

@Inject
@SingleIn(AppScope::class)
class SettingsRepository (
    private val dataStore : DataStore<Preferences>,
    private val api : WallhavenAPI
){
    val dynamicColorsFlow : Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferenceKeys.DYNAMIC_COLORS] ?: false
    }
    val themeFlow : Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferenceKeys.THEME] ?: "system" //default system
    }

    val apiKeyFlow : Flow<String> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.API_KEY].orEmpty()
    }

    val wallpaperSetterFlow : Flow<String> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.WALLPAPER_SETTER] ?: "native"
    }


    suspend fun updateTheme(newTheme : String){
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.THEME] = newTheme
        }
    }

    suspend fun updateApiKey(key : String) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.API_KEY] = key
        }
    }
    suspend fun updateSetter(setter : String){
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.WALLPAPER_SETTER] = setter
        }
    }

    suspend fun updateDynamicColors(value : Boolean){
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.DYNAMIC_COLORS] = value
        }
    }

    suspend fun authenticateAPIkey(key: String): AuthenticateAPIkey? {
       return api.authenticateApiKey(key)
    }

}