package com.enigmaticdevs.wallhaven.di

import android.content.Context
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.enigmaticdevs.wallhaven.data.remote.ApiKeyProvider
import com.enigmaticdevs.wallhaven.data.remote.WallhavenAPI
import com.enigmaticdevs.wallhaven.data.remote.model.Objects.App
import com.enigmaticdevs.wallhaven.data.repository.WallpaperRepository
import com.enigmaticdevs.wallhaven.di.flag.RateLimit
import com.enigmaticdevs.wallhaven.di.flag.RateLimiter
import com.enigmaticdevs.wallhaven.di.flag.savedApiKeyPlugin
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okio.Path.Companion.toPath


@BindingContainer
@ContributesTo(AppScope::class)
object NetworkModule {


    @SingleIn(AppScope::class)
    @Provides
    fun provideRepository(wallhavenApi: WallhavenAPI): WallpaperRepository =
        WallpaperRepository(wallhavenApi)




    /** KTOR clients for different purposes */

    @SingleIn(AppScope::class)
    @Provides
    fun provideKtorClient(
        apiKeyProvider: ApiKeyProvider,
        rateLimiter: RateLimiter
    ): HttpClient = HttpClient(CIO) {
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
        install(RateLimit) {limiter = rateLimiter}
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
        @Named("images") httpClient: HttpClient
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
                .maxSizeBytes(1024L*1024*1024) // 1 GB cache limit for wallpapers
                .build()
        }
        .build()

}