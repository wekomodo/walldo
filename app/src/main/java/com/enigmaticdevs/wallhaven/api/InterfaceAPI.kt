package com.enigmaticdevs.wallhaven.api

import com.enigmaticdevs.wallhaven.response.Wallpaper
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface InterfaceAPI {

    @GET("search")
    suspend fun listSearchWallpapers(
        @Query("sorting") sorting: String
    ): Response<Wallpaper>
}