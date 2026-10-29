package com.enigmaticdevs.wallhaven.data.local.collections

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.IGNORE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface CollectionDao {
    @Query("SELECT id FROM collections WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultId(): Long?

    @Query("SELECT * FROM collections ORDER BY isDefault DESC, createdAt ASC")
    fun observeAll() : Flow<List<CollectionEntity>>

    // CollectionDao
    @Insert
    suspend fun insert(collection: CollectionEntity): Long


}

@Dao
interface WallpaperDao {
    @Insert(onConflict = IGNORE)
    suspend fun insertIgnore(wallpaper : WallpaperEntity)
}

@Dao
interface ItemDao {
    @Insert(onConflict = IGNORE)
    suspend fun insertIgnore(item : CollectionItem)

    @Query("DELETE FROM collection_items WHERE collectionId = :collectionId AND wallpaperId = :wallpaperId")
    suspend fun remove(collectionId: Long, wallpaperId: String)

    // Drives the heart icon: is this wallpaper in Favorites?
    @Query(
        "SELECT EXISTS(SELECT 1 FROM collection_items i JOIN collections c ON c.id = i.collectionId " +
                "WHERE c.isDefault = 1 AND i.wallpaperId = :wallpaperId)"
    )
    fun observeIsFavorite(wallpaperId: String): Flow<Boolean>

    // The grid inside one collection, newest first
    @Query(
        "SELECT w.* FROM wallpapers w JOIN collection_items i ON i.wallpaperId = w.wallpaperId " +
                "WHERE i.collectionId = :collectionId ORDER BY i.addedAt DESC"
    )
    fun observeWallpapers(collectionId: Long): Flow<List<WallpaperEntity>>

    // ItemDao
    @Query("SELECT collectionId FROM collection_items WHERE wallpaperId = :wallpaperId")
    fun observeCollectionIdsContaining(wallpaperId: String): Flow<List<Long>>


}