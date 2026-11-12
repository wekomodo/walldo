package com.enigmaticdevs.wallhaven.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.enigmaticdevs.wallhaven.data.billing.BillingRepository
import com.enigmaticdevs.wallhaven.data.billing.legacy.LegacyBillingMigration
import com.enigmaticdevs.wallhaven.data.billing.legacy.LegacyBillingReader
import com.enigmaticdevs.wallhaven.data.local.SecureStorage
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionSyncManager
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionsDatabase
import com.enigmaticdevs.wallhaven.data.remote.ApiKeyProvider
import com.enigmaticdevs.wallhaven.data.remote.WallhavenAPI
import com.enigmaticdevs.wallhaven.data.remote.model.Objects.App
import com.enigmaticdevs.wallhaven.data.repository.GlobalFilterRepository
import com.enigmaticdevs.wallhaven.data.repository.WallpaperRepository
import com.enigmaticdevs.wallhaven.di.flag.RateLimit
import com.enigmaticdevs.wallhaven.di.flag.RateLimiter
import com.enigmaticdevs.wallhaven.di.flag.savedApiKeyPlugin
import com.enigmaticdevs.wallhaven.ui.screens.features.login.WallhavenAccountApi
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import okio.Path.Companion.toPath
import kotlin.reflect.KClass

val Context.filterDataStore: DataStore<Preferences> by preferencesDataStore(name = "filters")

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "com.enigmaticdevs.wallhaven_dataStore"
)

@DependencyGraph(AppScope::class)
interface AppGraph : ViewModelGraph {


    val globalFilterRepository: GlobalFilterRepository
    val collectionSyncManager: CollectionSyncManager
    val legacyBillingMigration: LegacyBillingMigration
    val billingRepository: BillingRepository
    val appScope: CoroutineScope  

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

