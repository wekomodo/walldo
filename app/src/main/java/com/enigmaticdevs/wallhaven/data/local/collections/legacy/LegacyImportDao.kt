package com.enigmaticdevs.wallhaven.data.local.collections.legacy

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionItemEntity

/**
 * Tiny DAO just for the one-time import of old favorites.
 * Add `abstract fun legacyImportDao(): LegacyImportDao` to CollectionsDatabase.
 * No schema change, so no version bump.
 */
@Dao
interface LegacyImportDao {

    /** The first local collection, i.e. the "Favorites" row created when the DB is first made. */
    @Query("SELECT id FROM collection WHERE isSynced = 0 ORDER BY id ASC LIMIT 1")
    suspend fun defaultCollectionId(): Long?

    /** One call = one transaction. Duplicate (wallpaperId, collectionId) rows are skipped. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertItems(items: List<CollectionItemEntity>)
}