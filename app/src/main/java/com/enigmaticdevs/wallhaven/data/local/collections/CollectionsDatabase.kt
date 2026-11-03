package com.enigmaticdevs.wallhaven.data.local.collections

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(
    entities = [
        WallpaperEntity::class,
        CollectionEntity::class,
        CollectionItemEntity::class,
        PendingOpEntity::class],
    version = 1,
    exportSchema = true,
)


abstract class CollectionsDatabase : RoomDatabase() {
    abstract fun collectionDao(): CollectionDao
}