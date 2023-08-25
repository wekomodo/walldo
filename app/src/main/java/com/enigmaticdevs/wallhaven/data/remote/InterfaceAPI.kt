package com.enigmaticdevs.wallhaven.data.remote

import com.enigmaticdevs.wallhaven.data.model.AuthenticateAPIkey
import com.enigmaticdevs.wallhaven.data.model.Photo
import com.enigmaticdevs.wallhaven.data.model.Wallpapers
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface InterfaceAPI {

    @GET("search")
    suspend fun getWallpaperBySort(
        @Query("sorting") sorting : String,
        @Query("purity") purity : String,
        @Query("categories") category: String,
        @Query("topRange")  topRange : String,
        @Query("ratios") ratio: String,
        @Query("atleast") resolution: String,
        @Query("page") page: Int
    ): Response<Wallpapers>

    @GET("search")
    suspend fun getSearchWallpapers(
        @Query("q") query: String,
        @Query("sorting") sorting: String,
        @Query("purity") purity : String,
        @Query("categories") category: String,
        @Query("topRange")  topRange : String,
        @Query("ratios") ratio: String,
        @Query("atleast") resolution: String,
        @Query("page") page: Int
    ): Response<Wallpapers>

    @GET("w/{id}")
    suspend fun getWallpaper(
        @Path("id") id: String,
    ): Response<Photo>

    @GET("settings")
    suspend fun authenticateApiKey(
        @Query("apikey") apiKey: String,
    ): Response<AuthenticateAPIkey>
}