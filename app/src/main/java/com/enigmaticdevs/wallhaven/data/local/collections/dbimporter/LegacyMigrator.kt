package com.enigmaticdevs.wallhaven.data.local.collections.dbimporter/*
package com.enigmaticdevs.wallhaven.dbimporter

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Inject
@SingleIn(AppScope::class)
class LegacyMigrator(
    private val pro: LegacyProImporter,
    private val favorites: LegacyFavoritesImporter,
    private val dataStore: DataStore<Preferences>,
    private val scope: CoroutineScope
) {
    fun runAll() = scope.launch {
        runOnce("legacy_pro_imported") { pro.import() }              // first: it unlocks features
        runOnce("legacy_favorites_imported") { favorites.import() }
    }

    private suspend fun runOnce(name: String, step: suspend () -> Unit) {
        val flag = booleanPreferencesKey(name)
        if (dataStore.data.first()[flag] == true) return
        try {
            step()
            dataStore.edit { it[flag] = true }   // only after the step fully succeeded
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("LegacyMigrator", "$name failed, will retry next launch", e)
        }
    }
}*/
