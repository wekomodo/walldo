package com.enigmaticdevs.wallhaven.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import com.enigmaticdevs.wallhaven.data.Objects.App
import com.enigmaticdevs.wallhaven.data.remote.WallhavenAPI
import com.enigmaticdevs.wallhaven.domain.repository.WallpaperRepository
import com.enigmaticdevs.wallhaven.domain.viewmodels.WallpaperListViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@DependencyGraph(AppScope::class)
interface AppGraph : ViewModelGraph{


    @Binds
    val MyViewModelFactory.bind: MetroViewModelFactory

    @Provides
    fun provideMyViewModelFactory(
        providers: Map<KClass<out ViewModel>, Provider<ViewModel>>,
        assistedProviders: Map<KClass<out ViewModel>, Provider<ViewModelAssistedFactory>>
    ): MyViewModelFactory = MyViewModelFactory(providers,assistedProviders)


    @DependencyGraph.Factory
    interface Factory {
        fun create(@Provides context: Context): AppGraph
    }

    /*@SingleIn(AppScope::class)
    @Provides
    fun providesAPI(ktor)*/

    @SingleIn(AppScope::class)
    @Provides
    fun provideRepository(wallhavenApi : WallhavenAPI) : WallpaperRepository = WallpaperRepository(wallhavenApi)

    @SingleIn(AppScope::class)
    @Provides
    fun provideDatastore(context : Context) : DataStore<Preferences> = context.dataStore

    @Provides
    fun provideKtorClient(apiKeyProvider: ApiKeyProvider): HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys= true })
        }
        defaultRequest {
            url(App.API_URL)
            apiKeyProvider.currentKey?.let {
                header("apiKey",it)
            }
        }
    }

    @Provides
    @SingleIn(AppScope::class)
    fun provideAppScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}


