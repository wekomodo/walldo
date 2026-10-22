package com.enigmaticdevs.wallhaven.ui.screens.homescreen.components

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


object QueryNames {
    const val HOME = "home"
    const val AUTOWALLPAPER = "auto_wallpaper"
}

@Inject
@SingleIn(AppScope::class)
class SavedQueries(
    private val dataStore: DataStore<Preferences>,
    private val scope : CoroutineScope
) {

    private fun key(name : String) = stringPreferencesKey("query_$name")

    fun observe(name : String): Flow<String> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[key(name)] ?: "" }
        .distinctUntilChanged()

    fun set (name : String, value : String){
        scope.launch {
            dataStore.edit {
                it[key(name)] = value
            }
        }
    }


}