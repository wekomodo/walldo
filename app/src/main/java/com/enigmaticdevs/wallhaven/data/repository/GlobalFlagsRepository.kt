package com.enigmaticdevs.wallhaven.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.work.impl.model.Preference
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private object GlobalFlagsKeys {
    val LOGIN_STATUS = booleanPreferencesKey("wallhaven_login")
}

@Inject
class GlobalFlagsRepository(
    private val dataStore : DataStore<Preferences>,
    private val scope : CoroutineScope
) {

    val loginStatus : Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[GlobalFlagsKeys.LOGIN_STATUS] ?: false //default system
    }

    private fun save( block : (MutablePreferences) -> Unit) {
        scope.launch { dataStore.edit { block(it) } }
    }

    fun updateLoginStatus(value : Boolean) = save {
        it[GlobalFlagsKeys.LOGIN_STATUS] = value
    }


}