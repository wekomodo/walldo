package com.enigmaticdevs.wallhaven.api

import com.enigmaticdevs.wallhaven.response.AuthenticateAPIkey
import com.enigmaticdevs.wallhaven.response.Data
import com.enigmaticdevs.wallhaven.response.Wallpaper
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface InterfaceAPI {

    @GET("search")
    suspend fun listSearchWallpapers(
        @Query("q") query: String,
        @Query("sorting") sorting : String,
        @Query("purity") purity : String,
        @Query("categories") category: String,
        @Query("topRange")  topRange : String,
        @Query("page") page: Int
    ): Response<Wallpaper>

    @GET("search")
    suspend fun listWallpaperBySort(
        @Query("sorting") sorting: String,
        @Query("purity") purity : String,
        @Query("categories") category: String,
        @Query("topRange")  topRange : String,
        @Query("ratios") ratio: String,
        @Query("atleast") resolution: String,
        @Query("page") page: Int
    ): Response<Wallpaper>

    @GET("w/{id}")
    suspend fun getImageDetails(
        @Path("id") id: String,
    ): Response<Data>

    @GET("settings")
    suspend fun authenticateApiKey(
        @Query("apikey") apiKey: String,
    ): Response<AuthenticateAPIkey>
}