package com.enigmaticdevs.wallhaven.di

import android.content.Context
import androidx.room.Room
import com.enigmaticdevs.wallhaven.autoWallpaperdb.AutoWallpaperDatabase
import com.enigmaticdevs.wallhaven.autoWallpaperdb.AutoWallpaperRepository
import com.enigmaticdevs.wallhaven.data.autowallpaper.AutoWallpaperDao
import com.enigmaticdevs.wallhaven.domain.favorite.FavoriteRepository
import com.enigmaticdevs.wallhaven.favoritedb.FavoriteImagesDao
import com.enigmaticdevs.wallhaven.favoritedb.FavoriteImagesDatabase
import com.enigmaticdevs.wallhaven.util.Constant
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {

    @Provides
    @Singleton
    fun provideAutoWallpaperDatabase(@ApplicationContext appContext: Context): AutoWallpaperDatabase {
        return Room.databaseBuilder(
            appContext,
            AutoWallpaperDatabase::class.java,
            Constant.autoWallpaperHistoryDatabaseName
        ).build()
    }
    @Provides
    fun provideAutoWallpaperDao(autoWallpaperDatabase: AutoWallpaperDatabase): AutoWallpaperDao {
        return autoWallpaperDatabase.autoWallpaperDao()
    }
    @Provides
    fun provideAutoWallpaperRepository(autoWallpaperDao: AutoWallpaperDao): AutoWallpaperRepository = AutoWallpaperRepository(autoWallpaperDao)


    @Provides
    @Singleton
    fun provideFavoriteDatabase(@ApplicationContext appContext: Context): FavoriteImagesDatabase {
        return Room.databaseBuilder(
            appContext,
            FavoriteImagesDatabase::class.java,
            Constant.favoriteDatabaseName
        ).build()
    }

    @Provides
    fun provideFavoriteDao(favoriteImagesDatabase: FavoriteImagesDatabase): FavoriteImagesDao {
        return favoriteImagesDatabase.favoriteImagesDao()
    }

    @Provides
    fun provideFavoriteRepository(favoriteImagesDao: FavoriteImagesDao): FavoriteRepository = FavoriteRepository(favoriteImagesDao)

}