package com.enigmaticdevs.wallhaven.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


@SingleIn(AppGraph::class)
@Inject
class ApiKeyProvider(
    private val userPreferences: UserPrefrences,
    scope: CoroutineScope
) {


    var currentKey: String? = null
        private set

    init {
        scope.launch {
            userPreferences.apiKey.collect{
                currentKey = it
            }
        }
    }
}

@Inject
class UserPrefrences(
    private val dataStore: DataStore<Preferences>
) {
    private val API_KEY = stringPreferencesKey("api_key")
    val apiKey: Flow<String?> = dataStore.data.map { it[API_KEY] }
    suspend fun setApiKey(key: String) {

        dataStore.edit { it[API_KEY] = key }
    }
}