package com.enigmaticdevs.wallhaven.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.work.impl.model.Preference
import com.enigmaticdevs.wallhaven.data.billing.ProEntitlementStore
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private object GlobalFlagsKeys {
    val LOGIN_STATUS = booleanPreferencesKey("wallhaven_login")

    val IS_PRO = booleanPreferencesKey("is_pro")


    val LEGACY_BILLING_IMPORTED = booleanPreferencesKey("legacy_billing_imported")
}

@Inject
class GlobalFlagsRepository(
    private val dataStore : DataStore<Preferences>,
    private val scope : CoroutineScope
) : ProEntitlementStore {

    val loginStatus : Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[GlobalFlagsKeys.LOGIN_STATUS] ?: false //default system
    }

    // Default is false: nobody is pro until Play (or the legacy import) says so.
    val proStatus: StateFlow<Boolean> = dataStore.data.map { preferences ->
        preferences[GlobalFlagsKeys.IS_PRO] ?: false
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    // ---- ProEntitlementStore (used by the billing code) ----

    override val isPro: Flow<Boolean> get() = proStatus

    override suspend fun setPro(value: Boolean) {
        dataStore.edit { it[GlobalFlagsKeys.IS_PRO] = value }
    }

    override suspend fun isLegacyBillingImported(): Boolean =
        dataStore.data.first()[GlobalFlagsKeys.LEGACY_BILLING_IMPORTED] ?: false

    override suspend fun markLegacyBillingImported() {
        dataStore.edit { it[GlobalFlagsKeys.LEGACY_BILLING_IMPORTED] = true }
    }


    fun updateProStatus(value : Boolean) = save {
        it[GlobalFlagsKeys.IS_PRO] = value
    }

    private fun save( block : (MutablePreferences) -> Unit) {
        scope.launch { dataStore.edit { block(it) } }
    }

    fun updateLoginStatus(value : Boolean) = save {
        it[GlobalFlagsKeys.LOGIN_STATUS] = value
    }


}