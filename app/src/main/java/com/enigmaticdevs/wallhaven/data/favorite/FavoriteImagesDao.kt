package com.enigmaticdevs.wallhaven.favoritedb

import androidx.lifecycle.LiveData
import androidx.room.*
import com.enigmaticdevs.wallhaven.data.favorite.models.FavoriteImages
import com.enigmaticdevs.wallhaven.data.favorite.models.ImageData
import com.enigmaticdevs.wallhaven.util.Constant


@Dao
interface FavoriteImagesDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addImage(favoriteImages: FavoriteImages)


    @Delete
    suspend fun deleteImage(favoriteImages: FavoriteImages)

    @Query("DELETE FROM ${Constant.FavoriteTableName} ")
    suspend fun deleteAll()

    @Query("SELECT * FROM ${Constant.FavoriteTableName} ORDER BY id ASC")
    fun readAllData(): LiveData<MutableList<FavoriteImages>>

    @Query("SELECT imageUrl,dimension_x,dimension_y from ${Constant.FavoriteTableName}")
    fun getOnlyUrls(): MutableList<ImageData>
}