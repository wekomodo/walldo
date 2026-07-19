package com.enigmaticdevs.wallhaven.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.enigmaticdevs.wallhaven.data.Objects.App
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@DependencyGraph(AppScope::class)
interface AppGraph {

    @DependencyGraph.Factory
    interface Factory {
        fun create(@Provides context: Context): AppGraph
    }

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
}


