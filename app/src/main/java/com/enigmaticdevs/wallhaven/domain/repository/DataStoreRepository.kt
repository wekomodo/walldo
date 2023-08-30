package com.enigmaticdevs.wallhaven.domain.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.enigmaticdevs.wallhaven.data.model.Params
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


const val DataStore_NAME = "user_settings"

val Context.datastore : DataStore< Preferences> by  preferencesDataStore(name = DataStore_NAME)


class DataStoreRepository(private val context: Context) {

    companion object{
        val api_Key = stringPreferencesKey("api_key")
        val purity = stringPreferencesKey("api_key")
    }

   /*  suspend fun saveParams(userSettings: UserSettings) {
        context.datastore.edit { phonebooks->
            phonebooks[NAME] = phonebook.name
            phonebooks[PHONE_NUMBER]= phonebook.phone
            phonebooks[address]= phonebook.address

        }

    }*/

    suspend fun saveAPIkey(key: String) {
        context.datastore.edit {
            it[api_Key] = key
        }
    }
 /*   fun getSettings() : Flow<Params>{
        return context.datastore.data.map {
            Params(
                purity = it[]
            )
        }
    }*/

    /* suspend fun getUserSettings() = context.datastore.data.map { settings ->
        Phonebook(
            name = phonebook[NAME]!!,
            address =  phonebook[address]!!,
            phone = phonebook[PHONE_NUMBER]!!
        )
    }*/
}