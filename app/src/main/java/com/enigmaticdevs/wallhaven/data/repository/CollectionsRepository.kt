package com.enigmaticdevs.wallhaven.data.repository

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionCover
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionDao
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionEntity
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionItemEntity
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionRemoteMediator
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionWithCount
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionsDatabase
import com.enigmaticdevs.wallhaven.data.local.collections.PendingOpEntity
import com.enigmaticdevs.wallhaven.data.remote.WallhavenAPI
import com.enigmaticdevs.wallhaven.data.remote.model.Wallpaper
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

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
    private val database: CollectionsDatabase,
    private val globalFlagsRepository: GlobalFlagsRepository,
    private val api : WallhavenAPI,
    private val scope : CoroutineScope,
    private val settingsRepository: SettingsRepository,
) {

    private val dao = database.collectionDao()

/*

    @OptIn(ExperimentalPagingApi::class)
    val wallpapersFlow = Pager(
        config = PagingConfig(pageSize = 24),
        remoteMediator = CollectionRemoteMediator(
            collectionId = localCollectionId,
            wallhavenId = api,
            api = api,
            dao = dao,
            username = settingsRepository // Wire it through
        ),
        pagingSourceFactory = {
            dao.getPagedItemsForCollection(localCollectionId)
        }
    ).flow.cachedIn(scope)
*/


    val isPro = globalFlagsRepository.proStatus
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
    fun observeWallpaperIds(collectionId: Long): Flow<List<CollectionItemEntity>> = dao.observeWallpaperIds(collectionId)
    fun observeCollectionIdsOf(wallpaperId: String): Flow<List<Long>> = dao.observeCollectionIdsOf(wallpaperId)
    fun observePendingCount(): Flow<Int> = dao.observePendingCount()
    fun observeRejectedOps(): Flow<List<PendingOpEntity>> = dao.observeRejectedOps()

    suspend fun clearRejectedOps() = dao.clearRejectedOps()

    /** Call once at startup. Everyone gets one local Default collection. */
    suspend fun ensureDefault() = dao.ensureLocalDefault("Default")

    /** Creating collections is pro-only. */
    suspend fun createCollection(name: String, synced: Boolean, isPublic: Boolean = false): Long {
        require(isPro.value) { "Creating collections is a pro feature" }
        val clean = name.trim()
        require(clean.isNotEmpty()) { "Name can't be empty" }
        return if (synced) dao.createSynced(clean, isPublic) else dao.createLocal(clean)
    }

    suspend fun deleteCollection(collectionId: Long) {
        val collection = dao.collection(collectionId) ?: return
        dao.deleteCollectionAndQueue(collection)
    }

    suspend fun addWallpaper(wallpaper: Wallpaper, collectionId: Long): AddResult {
        val target = dao.collection(collectionId) ?: error("No such collection")
        if (target.isSynced) {
            val current = dao.syncedCollectionOf(wallpaper.id)
            if (current == collectionId) return AddResult.AlreadyThere
            if (current != null) return AddResult.NeedsMove(current)
        }
        return if (dao.addItem(wallpaper, target)) AddResult.Added else AddResult.AlreadyThere
    }

    /** After the user confirms a NeedsMove. */
    suspend fun moveWallpaper(wallpaper: Wallpaper, toCollectionId: Long) {
        val to = dao.collection(toCollectionId) ?: return
        require(to.isSynced) { "Moves are only for synced collections" }
        val fromId = dao.syncedCollectionOf(wallpaper.id)
        when {
            fromId == null -> dao.addItem(wallpaper, to)
            fromId == toCollectionId -> Unit
            else -> dao.collection(fromId)?.let { dao.moveSynced(wallpaper.id, it, to) }
        }
    }

    suspend fun removeWallpaper(wallpaperId: String, collectionId: Long) {
        val collection = dao.collection(collectionId) ?: return
        dao.removeItem(wallpaperId, collection)
    }

    fun getCollectionInfo() : Flow<List<CollectionCover>> = dao.getCollectionsInfo()

    /** Call on logout, and whenever the logged-in account changes. Synced collections become local. */
    suspend fun onLoggedOut() = dao.deleteAllSyncedCollections()
}