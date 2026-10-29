package com.enigmaticdevs.wallhaven.data.repository

import androidx.room.withTransaction
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionEntity
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionItem
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionsDatabase
import com.enigmaticdevs.wallhaven.data.local.collections.WallpaperEntity
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class CollectionMembership(val collection: CollectionEntity, val contains: Boolean)

@Inject
class CollectionsRepository(
    private val db : CollectionsDatabase
) {
    private val collections = db.collectionDao()
    private val wallpapers = db.wallpaperDao()
    private val items = db.itemDao()

    fun observeIsFavorite(wallpaperId : String) = items.observeIsFavorite(wallpaperId)

    fun observeCollections()  = collections.observeAll()

    fun observeWallpapers(collectionId : Long) = items.observeWallpapers(collectionId)

    // Every collection, with "is this wallpaper in it?"
    fun observeMemberships(wallpaperId: String): Flow<List<CollectionMembership>> =
        combine(collections.observeAll(), items.observeCollectionIdsContaining(wallpaperId)) { all, ids ->
            val inside = ids.toSet()
            all.map { CollectionMembership(it, it.id in inside) }
        }

    suspend fun setMembership(wallpaper: WallpaperEntity, collectionId: Long, shouldContain: Boolean) {
        if (shouldContain) {
            db.withTransaction {
                wallpapers.insertIgnore(wallpaper)
                items.insertIgnore(CollectionItem(collectionId, wallpaper.wallpaperId))
            }
        } else {
            items.remove(collectionId, wallpaper.wallpaperId)
        }
    }

    // Returns the new id, or null if the name is empty
    suspend fun createCollection(name: String): Long? {
        val clean = name.trim().take(40)
        if (clean.isEmpty()) return null
        return collections.insert(CollectionEntity(name = clean))
    }
}