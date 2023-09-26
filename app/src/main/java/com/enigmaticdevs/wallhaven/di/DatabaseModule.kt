package com.enigmaticdevs.wallhaven.di

import android.content.Context
import androidx.room.Room
import com.enigmaticdevs.wallhaven.autoWallpaperdb.AutoWallpaperDatabase
import com.enigmaticdevs.wallhaven.autoWallpaperdb.AutoWallpaperRepository
import com.enigmaticdevs.wallhaven.data.autowallpaper.AutoWallpaperDao
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
    fun provideAppDatabase(@ApplicationContext appContext: Context): AutoWallpaperDatabase {
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
}