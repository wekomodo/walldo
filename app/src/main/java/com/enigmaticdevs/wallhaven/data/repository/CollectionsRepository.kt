package com.enigmaticdevs.wallhaven.data.repository

import com.enigmaticdevs.wallhaven.data.local.collections.CollectionDao
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionEntity
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionWithCount
import com.enigmaticdevs.wallhaven.data.local.collections.PendingOpEntity
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

sealed interface AddResult {
    data object Added : AddResult
    data object AlreadyThere : AddResult

    /** The wallpaper is in another synced collection. Ask "Move from X to Y?", then call moveWallpaper(). */
    data class NeedsMove(val fromCollectionId: Long) : AddResult
}

data class CollectionMembership(
    val collection: CollectionEntity,
    val contains: Boolean
)

/**
 * What the UI talks to. Everything writes to Room first; synced collections also queue an op
 * for CollectionSyncer to send later.
 */

@Inject
class CollectionsRepository(
    private val dao: CollectionDao,
    private val isPro: () -> Boolean,
) {

    fun observeMemberships(wallpaperId: String): Flow<List<CollectionMembership>> =
        combine(dao.observeCollections(), dao.observeCollectionIdsOf(wallpaperId)) { allWithCount, activeIds ->
            val activeSet = activeIds.toSet()
            allWithCount.map { withCount ->
                CollectionMembership(
                    collection = withCount.collection,
                    contains = withCount.collection.id in activeSet
                )
            }
        }


    fun observeCollections(): Flow<List<CollectionWithCount>> = dao.observeCollections()
    fun observeWallpaperIds(collectionId: Long): Flow<List<String>> = dao.observeWallpaperIds(collectionId)
    fun observeCollectionIdsOf(wallpaperId: String): Flow<List<Long>> = dao.observeCollectionIdsOf(wallpaperId)
    fun observePendingCount(): Flow<Int> = dao.observePendingCount()
    fun observeRejectedOps(): Flow<List<PendingOpEntity>> = dao.observeRejectedOps()

    suspend fun clearRejectedOps() = dao.clearRejectedOps()

    /** Call once at startup. Everyone gets one local Default collection. */
    suspend fun ensureDefault() = dao.ensureLocalDefault("Default")

    /** Creating collections is pro-only. */
    suspend fun createCollection(name: String, synced: Boolean, isPublic: Boolean = false): Long {
        require(isPro()) { "Creating collections is a pro feature" }
        val clean = name.trim()
        require(clean.isNotEmpty()) { "Name can't be empty" }
        return if (synced) dao.createSynced(clean, isPublic) else dao.createLocal(clean)
    }

    suspend fun deleteCollection(collectionId: Long) {
        val collection = dao.collection(collectionId) ?: return
        require(!collection.isDefault) { "The Default collection can't be deleted" }
        dao.deleteCollectionAndQueue(collection)
    }

    suspend fun addWallpaper(wallpaperId: String, collectionId: Long): AddResult {
        val target = dao.collection(collectionId) ?: error("No such collection")
        if (target.isSynced) {
            val current = dao.syncedCollectionOf(wallpaperId)
            if (current == collectionId) return AddResult.AlreadyThere
            if (current != null) return AddResult.NeedsMove(current)
        }
        return if (dao.addItem(wallpaperId, target)) AddResult.Added else AddResult.AlreadyThere
    }

    /** After the user confirms a NeedsMove. */
    suspend fun moveWallpaper(wallpaperId: String, toCollectionId: Long) {
        val to = dao.collection(toCollectionId) ?: return
        require(to.isSynced) { "Moves are only for synced collections" }
        val fromId = dao.syncedCollectionOf(wallpaperId)
        when {
            fromId == null -> dao.addItem(wallpaperId, to)
            fromId == toCollectionId -> Unit
            else -> dao.collection(fromId)?.let { dao.moveSynced(wallpaperId, it, to) }
        }
    }

    suspend fun removeWallpaper(wallpaperId: String, collectionId: Long) {
        val collection = dao.collection(collectionId) ?: return
        dao.removeItem(wallpaperId, collection)
    }

    /** Call on logout, and whenever the logged-in account changes. Synced collections become local. */
    suspend fun onLoggedOut() = dao.demoteAllToLocal()
}