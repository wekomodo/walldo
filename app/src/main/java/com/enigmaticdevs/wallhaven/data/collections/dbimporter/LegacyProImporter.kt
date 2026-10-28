package com.enigmaticdevs.wallhaven.data.collections.dbimporter

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import dev.zacsweers.metro.Inject


private const val BILLING_DB = "purchase_db"   // file name from the old Room.databaseBuilder(...)

@Inject
class LegacyProImporter(
    private val context: Context,
    private val dataStore: DataStore<Preferences>
) {
    suspend fun import() {
        val wasPro = context.readLegacyDb(BILLING_DB) { old ->
            old.rawQuery("SELECT entitled FROM walldo_pro WHERE id = 1", null)
                .use { it.moveToFirst() && it.getInt(0) == 1 }
        } ?: return

        if (wasPro) dataStore.edit { it[LEGACY_PRO] = true }
    }

    companion object {
        val LEGACY_PRO = booleanPreferencesKey("legacy_pro_entitled")
    }
}