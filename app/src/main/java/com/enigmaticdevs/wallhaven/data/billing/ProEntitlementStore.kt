package com.enigmaticdevs.wallhaven.data.billing

import kotlinx.coroutines.flow.Flow

/**
 * The only thing the billing code knows about storage.
 * Implement it on top of your existing DataStore code (IS_PRO key),
 * plus one extra Boolean key for the legacy-import marker.
 */
interface ProEntitlementStore {
    val isPro: Flow<Boolean>
    suspend fun setPro(value: Boolean)

    suspend fun isLegacyBillingImported(): Boolean
    suspend fun markLegacyBillingImported()
}