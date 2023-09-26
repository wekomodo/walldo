package com.enigmaticdevs.wallhaven.autoWallpaperdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.enigmaticdevs.wallhaven.data.autowallpaper.AutoWallpaperDao
import com.enigmaticdevs.wallhaven.data.autowallpaper.models.AutoWallpaper


@Database(entities = [AutoWallpaper::class],version = 1,exportSchema = false)
abstract class AutoWallpaperDatabase : RoomDatabase() {
    abstract fun autoWallpaperDao() : AutoWallpaperDao
}