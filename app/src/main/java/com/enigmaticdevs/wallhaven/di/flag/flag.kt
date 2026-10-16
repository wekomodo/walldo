package com.enigmaticdevs.wallhaven.di.flag

import com.enigmaticdevs.wallhaven.di.ApiKeyProvider
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.header
import io.ktor.util.AttributeKey


val SkipSavedApiKey = AttributeKey<Boolean>("SkipSavedApiKey")

fun savedApiKeyPlugin(provider: ApiKeyProvider) = createClientPlugin("SavedApiKey") {
    onRequest { request, _ ->
        if (!request.attributes.contains(SkipSavedApiKey)) {
            provider.currentKey?.let { request.header("X-API-Key", it) }
        }
    }
}