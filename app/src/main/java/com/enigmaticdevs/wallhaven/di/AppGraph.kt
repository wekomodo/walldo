package com.enigmaticdevs.wallhaven.di

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
import com.enigmaticdevs.wallhaven.data.Objects.App
import com.enigmaticdevs.wallhaven.data.collections.CollectionsDatabase
import com.enigmaticdevs.wallhaven.data.remote.WallhavenAPI
import com.enigmaticdevs.wallhaven.di.flag.savedApiKeyPlugin
import com.enigmaticdevs.wallhaven.domain.repository.GlobalFilterRepository
import com.enigmaticdevs.wallhaven.domain.repository.WallpaperRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.Provider
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
import io.ktor.client.request.header
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


    @Binds
    val MyViewModelFactory.bind: MetroViewModelFactory

    @Provides
    fun provideMyViewModelFactory(
        providers: Map<KClass<out ViewModel>, Provider<ViewModel>>,
        assistedProviders: Map<KClass<out ViewModel>, Provider<ViewModelAssistedFactory>>,
        manualAssistedFactoryProviders: Map<KClass<out ManualViewModelAssistedFactory>, Provider<ManualViewModelAssistedFactory>>
    ): MyViewModelFactory =
        MyViewModelFactory(providers, assistedProviders, manualAssistedFactoryProviders)


    @DependencyGraph.Factory
    interface Factory {
        fun create(@Provides context: Context): AppGraph
    }

    /*@SingleIn(AppScope::class)
    @Provides
    fun providesAPI(ktor)*/

    @SingleIn(AppScope::class)
    @Provides
    fun provideRepository(wallhavenApi: WallhavenAPI): WallpaperRepository =
        WallpaperRepository(wallhavenApi)

    @SingleIn(AppScope::class)
    @Provides
    fun provideDatastore(context: Context): DataStore<Preferences> = context.dataStore


    // not used yet
    @Named("filters")
    @SingleIn(AppScope::class)
    @Provides
    fun provideFilterDataStore(context: Context): DataStore<Preferences> = context.filterDataStore

    @SingleIn(AppScope::class)
    @Provides
    fun provideKtorClient(apiKeyProvider: ApiKeyProvider): HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        defaultRequest {
            url(App.API_URL)
        }
        install(savedApiKeyPlugin(apiKeyProvider))
    }

    // Image client: plain, no API headers, no JSON plugin
    @Named("images")
    @SingleIn(AppScope::class)
    @Provides
    fun provideImageHttpClient(): HttpClient = HttpClient(CIO)


    // --- COIL 3 IMAGE LOADER ADDITION ---
    @OptIn(ExperimentalCoilApi::class)
    @SingleIn(AppScope::class)
    @Provides
    fun provideImageLoader(
        context: Context,
        @Named("images")httpClient: HttpClient
    ): ImageLoader = ImageLoader.Builder(context)
        .components {
            // uses a new ktor client that doesnt need headers
            add(KtorNetworkFetcherFactory(httpClient))
        }
        .memoryCache {
            MemoryCache.Builder()
                .maxSizePercent(context, 0.20)
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(context.cacheDir.resolve("walldo_cache").absolutePath.toPath())
                .maxSizeBytes(1024L * 1024 * 1024) // 1 GB cache limit for wallpapers
                .build()
        }
        .build()

    // Provide Room Db
    @SingleIn(AppScope::class)
    @Provides
    fun provideCollectionsDatabase(context : Context): CollectionsDatabase =
        Room.databaseBuilder(context, CollectionsDatabase::class.java, "walldo_collections.db")
            .addCallback(object : RoomDatabase.Callback() {
                // runs once, when the file is first created: guarantees exactly one "Favorites"
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "INSERT INTO collections (name, isDefault, createdAt) " +
                                "VALUES ('Favorites', 1, ${System.currentTimeMillis()})"
                    )
                }
            })
            .build()




    @Provides
    @SingleIn(AppScope::class)
    fun provideAppScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun provideGlobalFiltersRepository(): GlobalFilterRepository

    // Expose the ImageLoader so your Application class can safely grab it
    fun provideImageLoaderInstance(): ImageLoader

}

