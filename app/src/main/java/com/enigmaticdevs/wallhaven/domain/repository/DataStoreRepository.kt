
package com.enigmaticdevs.wallhaven.domain.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.enigmaticdevs.wallhaven.data.model.Params
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class DataStoreRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val api_Key = stringPreferencesKey("api_key")
        val purity = stringPreferencesKey("api_key")
        val category = stringPreferencesKey("api_key")
        val ratio = stringPreferencesKey("api_key")
        val resolution = stringPreferencesKey("api_key")
    }

    suspend fun saveAPIkey(key: String) {
        dataStore.edit {
            it[api_Key] = key
        }
    }

    suspend fun saveSettings(params : Params) {
        dataStore.edit {
            it[purity] = params.purity
            it[category] = params.category
            it[ratio] = params.ratio
            it[resolution] = params.resolution

        }
    }

    fun  getAPIkey() : Flow<String> {
        return dataStore.data.map {
            it[api_Key].toString()
        }
    }
      fun getSettings() : Flow<Params?> {
        return dataStore.data.map {
                Params(
                    purity = it[purity].toString(),
                    category = it[category].toString(),
                    ratio = it[ratio].toString(),
                    resolution = it[resolution].toString()
                )
        }
    }
}