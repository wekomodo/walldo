package com.enigmaticdevs.wallhaven.data.local.collections

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(
    entities = [CollectionEntity::class, WallpaperEntity::class, CollectionItem::class],
    version = 1,
    exportSchema = true
)


abstract class CollectionsDatabase : RoomDatabase() {
    abstract fun collectionDao(): CollectionDao
    abstract fun wallpaperDao(): WallpaperDao
    abstract fun itemDao(): ItemDao
}