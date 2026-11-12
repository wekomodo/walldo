package com.enigmaticdevs.wallhaven.di

import android.content.Context
import com.enigmaticdevs.wallhaven.data.billing.BillingRepository
import com.enigmaticdevs.wallhaven.data.billing.ProEntitlementStore
import com.enigmaticdevs.wallhaven.data.billing.legacy.LegacyBillingMigration
import com.enigmaticdevs.wallhaven.data.billing.legacy.LegacyBillingReader
import com.enigmaticdevs.wallhaven.data.repository.GlobalFlagsRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope

/**
 * Everything billing-related. Contributed to AppScope, so it merges into AppGraph
 * automatically. Context and CoroutineScope come from bindings already in AppGraph.
 */
@BindingContainer
@ContributesTo(AppScope::class)
object BillingModule {

    @SingleIn(AppScope::class)
    @Provides
    fun provideLegacyBillingReader(context: Context): LegacyBillingReader =
        LegacyBillingReader(context)

    // GlobalFlagsRepository (scoped singleton) is the pro store.
    @Provides
    fun provideProEntitlementStore(flags: GlobalFlagsRepository): ProEntitlementStore = flags

    @SingleIn(AppScope::class)
    @Provides
    fun provideBillingRepository(
        context: Context,
        store: ProEntitlementStore,
        scope: CoroutineScope,
    ): BillingRepository = BillingRepository(context, store, scope)

    @SingleIn(AppScope::class)
    @Provides
    fun provideLegacyBillingMigration(
        reader: LegacyBillingReader,
        store: ProEntitlementStore,
    ): LegacyBillingMigration = LegacyBillingMigration(reader, store)
}