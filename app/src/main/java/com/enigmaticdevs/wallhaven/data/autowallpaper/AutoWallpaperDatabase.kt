package com.enigmaticdevs.wallhaven.autoWallpaperdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.enigmaticdevs.wallhaven.data.autowallpaper.AutoWallpaperDao
import com.enigmaticdevs.wallhaven.data.autowallpaper.models.AutoWallpaper
import com.enigmaticdevs.wallhaven.util.Constant


@Database(entities = [AutoWallpaper::class],version = 1,exportSchema = false)
abstract class AutoWallpaperDatabase : RoomDatabase() {
    abstract fun autoWallpaperDao() : AutoWallpaperDao

    companion object{
        @Volatile
        private var INSTANCE : AutoWallpaperDatabase? = null
        fun getDatabase(context: Context) : AutoWallpaperDatabase {
            val tempInstance = INSTANCE
            if(tempInstance!=null)
                return tempInstance
            else
                synchronized(this){
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AutoWallpaperDatabase::class.java,
                        Constant.autoWallpaperHistoryDatabaseName
                    ).build()
                    INSTANCE = instance
                    return instance
                }
        }
        @Volatile
        private var instance: AutoWallpaperDatabase? = null

        fun getInstance(context: Context): AutoWallpaperDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }
        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, AutoWallpaperDatabase::class.java,Constant.autoWallpaperHistoryDatabaseName)
                .build()
    }
}