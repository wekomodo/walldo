package com.enigmaticdevs.wallhaven.autoWallpaperdb

import com.enigmaticdevs.wallhaven.data.autowallpaper.AutoWallpaperDao
import com.enigmaticdevs.wallhaven.data.autowallpaper.models.AutoWallpaper
import javax.inject.Inject

class AutoWallpaperRepository @Inject constructor(private val autoWallpaperDao: AutoWallpaperDao) {



    fun readAllData() = autoWallpaperDao.readAllData()


     fun addImage(autoWallpaper: AutoWallpaper){
        autoWallpaperDao.addImage(autoWallpaper)
    }

    suspend fun deleteImage(autoWallpaper: AutoWallpaper) {
        autoWallpaperDao.deleteImage(autoWallpaper)
    }

    suspend fun deleteAll(){
        autoWallpaperDao.deleteAll()
    }
}