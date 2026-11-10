package com.enigmaticdevs.wallhaven.data.local.collections

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


@Database(
    entities = [
        CollectionEntity::class,
        CollectionItemEntity::class,
        PendingOpEntity::class],
    version = 1,
    exportSchema = true,
)


abstract class CollectionsDatabase : RoomDatabase() {
    abstract fun collectionDao(): CollectionDao
}
