package com.enigmaticdevs.wallhaven.domain.favorite

import com.enigmaticdevs.wallhaven.data.favorite.models.FavoriteImages
import com.enigmaticdevs.wallhaven.favoritedb.FavoriteImagesDao
import javax.inject.Inject

class FavoriteRepository @Inject constructor(private val favoriteImagesDao: FavoriteImagesDao) {
    fun readAllData() = favoriteImagesDao.readAllData()

    suspend fun addImage(favoriteImages: FavoriteImages) =
        favoriteImagesDao.addImage(favoriteImages)

    suspend fun deleteImage(favoriteImage: FavoriteImages) =
        favoriteImagesDao.deleteImage(favoriteImage)


    suspend fun deleteAll() = favoriteImagesDao.deleteAll()

    fun getOnlyUrls() = favoriteImagesDao.getOnlyUrls()


}