package com.enigmaticdevs.wallhaven.main

import com.enigmaticdevs.wallhaven.api.InterfaceAPI
import com.enigmaticdevs.wallhaven.response.Wallpaper
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val api : InterfaceAPI
) {
     suspend fun getWallpapers(sorting: String,purity : String,category : String,topRange : String,page : Int): Wallpaper? {
            val response = api.listWallpapers(sorting,purity,category, topRange,page)
            val result = response.body()
         return if(response.isSuccessful && result !=null ){
             response.body()
         } else {
             null
         }

    }
}