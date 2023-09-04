
package com.enigmaticdevs.wallhaven.domain.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.enigmaticdevs.wallhaven.data.model.Params
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject


class DataStoreRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val api_Key = stringPreferencesKey("api_key")
        val purity = stringPreferencesKey("purity")
        val category = stringPreferencesKey("category")
        val ratio = stringPreferencesKey("ratio")
        val resolution = stringPreferencesKey("resolution")
        val settingsMigrated = booleanPreferencesKey("settingsMigrated")
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
        return dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map{
            it[api_Key].toString()
        }
    }
      fun readSettings() : Flow<Params?> {
        return dataStore.data.map {
                Params(
                    purity = it[purity].toString(),
                    category = it[category].toString(),
                    ratio = it[ratio].toString(),
                    resolution = it[resolution].toString()
                )
        }
    }

    suspend fun setSettingsMigrated() {
        dataStore.edit {
            it[settingsMigrated] = true
        }
    }

    fun getSettingsMigrated(): Flow<Boolean?> {
       return dataStore.data.map {
           it[settingsMigrated]
       }
    }
}