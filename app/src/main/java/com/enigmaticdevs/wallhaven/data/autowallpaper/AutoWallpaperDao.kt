package com.enigmaticdevs.wallhaven.data.autowallpaper

import androidx.lifecycle.LiveData
import androidx.room.*
import com.enigmaticdevs.wallhaven.data.autowallpaper.models.AutoWallpaper
import com.enigmaticdevs.wallhaven.util.Constant

@Dao
interface  AutoWallpaperDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
     fun addImage(autoWallpaper: AutoWallpaper)


    @Delete
    suspend fun deleteImage(autoWallpaper: AutoWallpaper)

    @Query("DELETE FROM ${Constant.AutoWallpaperHistoryTableName} ")
    suspend fun deleteAll()

    @Query("SELECT * FROM ${Constant.AutoWallpaperHistoryTableName} ORDER BY id ASC")
    fun readAllData(): LiveData<MutableList<AutoWallpaper>>
}