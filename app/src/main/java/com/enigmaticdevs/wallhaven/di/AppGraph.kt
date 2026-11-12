package com.enigmaticdevs.wallhaven.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.work.WorkerFactory
import coil3.ImageLoader
import com.enigmaticdevs.wallhaven.data.billing.BillingRepository
import com.enigmaticdevs.wallhaven.data.billing.legacy.LegacyBillingMigration
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionSyncManager
import com.enigmaticdevs.wallhaven.data.local.collections.legacy.LegacyFavoritesMigration
import com.enigmaticdevs.wallhaven.data.repository.GlobalFilterRepository
import com.enigmaticdevs.wallhaven.ui.screens.login.WallhavenAccountApi
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.reflect.KClass

val Context.filterDataStore: DataStore<Preferences> by preferencesDataStore(name = "filters")


@DependencyGraph(AppScope::class)
interface AppGraph : ViewModelGraph {


    val globalFilterRepository: GlobalFilterRepository
    val collectionSyncManager: CollectionSyncManager
    val legacyBillingMigration: LegacyBillingMigration
    val legacyFavoritesMigration : LegacyFavoritesMigration
    val billingRepository: BillingRepository
    val appScope: CoroutineScope
    val workerFactory: WorkerFactory

    @Binds
    val MyViewModelFactory.bind: MetroViewModelFactory

    @Provides
    fun provideMyViewModelFactory(
        providers: Map<KClass<out ViewModel>,() -> ViewModel>,
        assistedProviders: Map<KClass<out ViewModel>, () -> ViewModelAssistedFactory>,
        manualAssistedFactoryProviders: Map<KClass<out ManualViewModelAssistedFactory>,() -> ManualViewModelAssistedFactory>
    ): MyViewModelFactory =
        MyViewModelFactory(providers, assistedProviders, manualAssistedFactoryProviders)


    @DependencyGraph.Factory
    interface Factory {
        fun create(@Provides context: Context, @Provides application: Application): AppGraph
    }

    @SingleIn(AppScope::class)
    @Provides
    fun providesWallhavenAPI() : WallhavenAccountApi = WallhavenAccountApi()


    @Provides
    @SingleIn(AppScope::class)
    fun provideAppScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)



    // Expose the ImageLoader so your Application class can safely grab it
    fun provideImageLoaderInstance(): ImageLoader

}

