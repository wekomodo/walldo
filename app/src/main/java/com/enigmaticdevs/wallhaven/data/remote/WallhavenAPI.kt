package com.enigmaticdevs.wallhaven.data.remote

import com.enigmaticdevs.wallhaven.data.remote.model.AuthenticateAPIkey
import com.enigmaticdevs.wallhaven.data.remote.model.WallpaperDetail
import com.enigmaticdevs.wallhaven.data.remote.model.Wallpapers
import com.enigmaticdevs.wallhaven.di.flag.SkipSavedApiKey
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import okhttp3.Response


@Inject
class WallhavenAPI(
    private val client : HttpClient
){

    suspend fun getWallpapers(
        query : String ="",
        sorting: String,
        purity: String,
        category: String,
        topRange: String,
        ratio: String,
        resolution: String,
        page: Int
    ): Wallpapers = client.get("search") {
        if(query.isNotBlank())
            parameter("q",query)
        parameter("sorting", sorting)
        parameter("purity", purity)
        parameter("categories", category)
        parameter("topRange", topRange)
        parameter("ratios", ratio)
        parameter("atleast", resolution)
        parameter("page", page)
    }.body<Wallpapers>()


    suspend fun getSearchWallpapers(
        query: String,
        sorting: String,
        purity: String,
        category: String,
        topRange: String,
        ratio: String,
        resolution: String,
        page: Int
    ): Wallpapers = client.get("search") {
        parameter("q",query)
        parameter("sorting", sorting)
        parameter("purity", purity)
        parameter("categories", category)
        parameter("topRange", topRange)
        parameter("ratios", ratio)
        parameter("atleast", resolution)
        parameter("page", page)
    }.body<Wallpapers>()

    suspend fun getWallpaper(id: String): WallpaperDetail? = client.get("w/$id") {
        parameter("id", id)
    }.body()

    suspend fun authenticateApiKey(apiKey: String): AuthenticateAPIkey? {
        val response = client.get("settings") {
            attributes.put(SkipSavedApiKey, true)
            header("X-API-Key", apiKey)
        }
        return when {
            response.status == HttpStatusCode.Unauthorized -> null
            response.status.isSuccess() -> response.body()
            else -> error("HTTP ${response.status}")
        }
    }

    suspend fun createCollection(label : String,public : Boolean, token: String): HttpResponse = client.post("collection/new"){
       // parameter()


    }.body()

}