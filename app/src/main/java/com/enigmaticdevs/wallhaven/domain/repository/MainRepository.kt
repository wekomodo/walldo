package com.enigmaticdevs.wallhaven.domain.repository

import com.enigmaticdevs.wallhaven.data.remote.InterfaceAPI
import com.enigmaticdevs.wallhaven.data.model.Data
import com.enigmaticdevs.wallhaven.data.model.Params
import com.enigmaticdevs.wallhaven.data.model.Wallpaper
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val api : InterfaceAPI
) {
     suspend fun getWallpaperBySort(
         params : Params,
         page : Int): Wallpaper? {
            val response = api.getWallpaperBySort(params.sorting,params.purity,params.category, params.topRange,params.ratio,params.resolution,page)
            val result = response.body()
         return if(response.isSuccessful && result !=null ){
             response.body()
         } else {
             null
         }

    }

    suspend fun getSearchWallpapers(
        query: String,
        params : Params,
        page : Int): Wallpaper? {
        val response = api.getSearchWallpapers(query,params.sorting,params.purity,params.category, params.topRange,params.ratio,params.resolution,page)
        val result = response.body()
        return if(response.isSuccessful && result !=null ){
            response.body()
        } else {
            null
        }

    }
    suspend fun getWallpaper(id : String) : Data?{
        val response = api.getWallpaper(id)
        val result = response.body()
        return if(response.isSuccessful && result !=null ){
            response.body()
        } else {
            null
        }
    }
}