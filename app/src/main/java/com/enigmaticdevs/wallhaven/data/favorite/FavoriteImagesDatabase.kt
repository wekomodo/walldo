package com.enigmaticdevs.wallhaven.favoritedb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.enigmaticdevs.wallhaven.data.favorite.models.FavoriteImages
import com.enigmaticdevs.wallhaven.util.Constant

@Database(entities = [FavoriteImages::class],version = 1,exportSchema = false)
abstract class FavoriteImagesDatabase :RoomDatabase(){

    abstract fun favoriteImagesDao() : FavoriteImagesDao

    companion object{
        @Volatile
        private var INSTANCE : FavoriteImagesDatabase? = null
        fun getDatabase(context: Context) : FavoriteImagesDatabase{
                 val tempInstance = INSTANCE
            if(tempInstance!=null)
                return tempInstance
            else
                synchronized(this){
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        FavoriteImagesDatabase::class.java,
                        Constant.favoriteDatabaseName
                    ).build()
                    INSTANCE = instance
                    return instance
                }
        }
        private var instance: FavoriteImagesDatabase? = null

        fun getInstance(context: Context): FavoriteImagesDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context)
                    .also { instance = it }
            }
        }
        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, FavoriteImagesDatabase::class.java,Constant.favoriteDatabaseName)
                .build()
    }
}
