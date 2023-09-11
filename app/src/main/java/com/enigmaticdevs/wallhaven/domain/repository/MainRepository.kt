package com.enigmaticdevs.wallhaven.domain.repository

import com.enigmaticdevs.wallhaven.data.model.AuthenticateAPIkey
import com.enigmaticdevs.wallhaven.data.model.Params
import com.enigmaticdevs.wallhaven.data.model.Photo
import com.enigmaticdevs.wallhaven.data.model.Wallpapers
import com.enigmaticdevs.wallhaven.data.remote.InterfaceAPI
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val api: InterfaceAPI
) {
    suspend fun getWallpaperBySort(
        sorting: String,
        topRange: String,
        params: Params,
        page: Int
    ): Wallpapers? {
        val response = api.getWallpaperBySort(
            sorting,
            params.purity,
            params.category,
            topRange,
            params.ratio,
            params.resolution,
            page
        )
        val result = response.body()
        return if (response.isSuccessful && result != null) {
            response.body()
        } else {
            null
        }

    }

    suspend fun getSearchWallpapers(
        query: String,
        sorting: String,
        topRange: String,
        params: Params,
        page: Int
    ): Wallpapers? {
        val response = api.getSearchWallpapers(
            query,
            sorting,
            params.purity,
            params.category,
            topRange,
            params.ratio,
            params.resolution,
            page
        )
        val result = response.body()
        return if (response.isSuccessful && result != null) {
            response.body()
        } else {
            null
        }

    }

    suspend fun getWallpaper(id: String): Photo? {
        return try {
            val response = api.getWallpaper(id)
            val result = response.body()
            return if (response.isSuccessful && result != null)
                result
            else
                null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun authenticateAPIkey(key: String): AuthenticateAPIkey? {
        return try {
            val response = api.authenticateApiKey(key)
            val result = response.body()
            if (response.isSuccessful && result != null)
                result
            else
                null
        } catch (e: Exception) {
            null
        }
    }
}