package com.enigmaticdevs.wallhaven.data.remote

import com.enigmaticdevs.wallhaven.data.Objects.Sorting
import com.enigmaticdevs.wallhaven.data.Objects.TopRange
import com.enigmaticdevs.wallhaven.data.model.AuthenticateAPIkey
import com.enigmaticdevs.wallhaven.data.model.Wallpaper
import com.enigmaticdevs.wallhaven.data.model.Wallpapers
import com.enigmaticdevs.wallhaven.data.model.local.Category
import com.enigmaticdevs.wallhaven.data.model.local.Purity
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import okhttp3.Response


@Inject
class WallhavenAPI(
    private val client : HttpClient
){

    suspend fun getWallpapersBySort(
        sorting: String,
        purity: String,
        category: String,
        topRange: String,
        ratio: String,
        resolution: String,
        page: Int
    ): Wallpapers = client.get("search") {
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

    suspend fun getWallpaper(id: String): Wallpaper? = client.get("w/$id") {
        parameter("id", id)
    }.body()

    suspend fun authenticateApiKey(apiKey: String): AuthenticateAPIkey? = client.get("settings") {
        parameter("apiKey",apiKey)
    }.body()

}