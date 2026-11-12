package com.enigmaticdevs.wallhaven.data.local.collections

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.enigmaticdevs.wallhaven.data.remote.model.RemoteCollection
import com.enigmaticdevs.wallhaven.data.remote.model.Wallpaper
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

@Serializable
data class CollectionCover(
    val id: Long,
    val name: String,
    val coverThumbUrl: String?,
    val isSynced: Boolean,
    val count: Int,
    val isAutoWallpaper : Boolean = false,
)

@Dao
abstract class CollectionDao {

    // ============================================================ reads for the UI

    /**
     * All collections with an item count. While a synced collection is still being downloaded, the count is
     * the larger of the site's count and what we have, so the number doesn't jump around or show 0.
     */
    @Query("""
        SELECT c.*,
               CASE WHEN c.isSynced = 1 AND c.remoteId IS NOT NULL AND (c.lastPage = 0 OR c.syncedPages < c.lastPage)
                    THEN MAX(c.remoteCount, (SELECT COUNT(*) FROM collection_item i WHERE i.collectionId = c.id))
                    ELSE (SELECT COUNT(*) FROM collection_item i WHERE i.collectionId = c.id) END AS itemCount
        FROM collection AS c
        ORDER BY c.createdAt
    """)
    abstract fun observeCollections(): Flow<List<CollectionWithCount>>

    /** Items of one collection, newest first. */
    @Query("SELECT * FROM collection_item WHERE collectionId = :collectionId ORDER BY addedAt DESC")
    abstract fun observeWallpaperIds(collectionId: Long): Flow<List<CollectionItemEntity>>

    /** For "is this wallpaper saved anywhere" state on a detail screen. */
    @Query("SELECT collectionId FROM collection_item WHERE wallpaperId = :wallpaperId")
    abstract fun observeCollectionIdsOf(wallpaperId: String): Flow<List<Long>>

    @Query("SELECT COUNT(*) FROM pending_op WHERE state = :state")
    abstract fun observePendingCount(state: OpState = OpState.PENDING): Flow<Int>

    @Query("SELECT * FROM pending_op WHERE state = :state ORDER BY id")
    abstract fun observeRejectedOps(state: OpState = OpState.REJECTED): Flow<List<PendingOpEntity>>

    /** The collections list screen: name, cover (newest item) and count (same rule as observeCollections). */
    @Query("""
    SELECT c.id, c.name, c.isSynced,
           (SELECT thumbUrl FROM collection_item 
            WHERE collectionId = c.id 
              AND (
                  (SUBSTR(:purity, 1, 1) = '1' AND purity = 'sfw') OR
                  (SUBSTR(:purity, 2, 1) = '1' AND purity = 'sketchy') OR
                  (SUBSTR(:purity, 3, 1) = '1' AND purity = 'nsfw') OR
                  purity IS NULL
              )
            ORDER BY addedAt DESC LIMIT 1) AS coverThumbUrl,
           CASE WHEN c.isSynced = 1 AND c.remoteId IS NOT NULL AND (c.lastPage = 0 OR c.syncedPages < c.lastPage)
                THEN MAX(c.remoteCount, (SELECT COUNT(*) FROM collection_item WHERE collectionId = c.id))
                ELSE (SELECT COUNT(*) FROM collection_item WHERE collectionId = c.id) END AS count,
                c.isAutoWallpaper
    FROM collection c
    ORDER BY c.sortOrder ASC, c.createdAt ASC
""")
    abstract fun getCollectionsInfo(purity: String): Flow<List<CollectionCover>>


    // for AutoWallpaper use

    // 1. Set a single collection as the active Auto-Wallpaper source
    @Query("UPDATE collection SET isAutoWallpaper = CASE WHEN id = :collectionId THEN 1 ELSE 0 END")
    abstract suspend fun setAutoWallpaperCollection(collectionId: Long)

    // 2. Worker query: picks 1 random wallpaper across any active Auto-Wallpaper collection
    @Query("""
    SELECT i.fullUrl FROM collection_item i
    INNER JOIN collection c ON i.collectionId = c.id
    WHERE c.isAutoWallpaper = 1
      AND i.fullUrl IS NOT NULL
      AND (
          (SUBSTR(:purityMask, 1, 1) = '1' AND i.purity = 'sfw') OR
          (SUBSTR(:purityMask, 2, 1) = '1' AND i.purity = 'sketchy') OR
          (SUBSTR(:purityMask, 3, 1) = '1' AND i.purity = 'nsfw') OR
          i.purity IS NULL
      )
    ORDER BY RANDOM() LIMIT 1
""")
    abstract suspend fun getNextAutoWallpaperUrl(purityMask: String): String?

    // ---- sync progress, used by the manager's indicator (synced collections only; local ones never count)

    @Query("SELECT COUNT(*) FROM collection WHERE isSynced = 1 AND remoteId IS NOT NULL")
    abstract fun observeSyncedTotal(): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM collection
        WHERE isSynced = 1 AND remoteId IS NOT NULL AND lastPage > 0 AND syncedPages >= lastPage
    """)
    abstract fun observeSyncedDone(): Flow<Int>

    // ============================================================ lookups

    /** looking up a collection items based on collectionID and using paging3 */
    @Query("""SELECT * FROM collection_item WHERE collectionId = :collectionId 
        AND (
          (SUBSTR(:purity, 1, 1) = '1' AND purity = 'sfw') OR
          (SUBSTR(:purity, 2, 1) = '1' AND purity = 'sketchy') OR
          (SUBSTR(:purity, 3, 1) = '1' AND purity = 'nsfw') OR
          purity IS NULL
      )ORDER BY addedAt DESC""")
    abstract fun getPagedWallpapers(collectionId: Long, purity : String): PagingSource<Int, CollectionItemEntity>
    @Query("SELECT * FROM collection WHERE id = :id")
    abstract suspend fun collection(id: Long): CollectionEntity?

    @Query("SELECT * FROM collection WHERE isSynced = 1")
    abstract suspend fun syncedCollections(): List<CollectionEntity>

    /** Synced collections that still have pages to download (or none yet), in creation order. */
    @Query("""
        SELECT * FROM collection
        WHERE isSynced = 1 AND remoteId IS NOT NULL AND (lastPage = 0 OR syncedPages < lastPage)
        ORDER BY id
    """)
    abstract suspend fun collectionsNeedingSync(): List<CollectionEntity>

    /** returns the count of wallpapers in a collection */
    @Query("SELECT COUNT(*) FROM collection_item WHERE collectionId = :collectionId")
    abstract suspend fun getItemCount(collectionId: Long): Int

    /** The one synced collection this wallpaper is in, or null. */
    @Query("SELECT collectionId FROM collection_item WHERE wallpaperId = :wallpaperId AND syncedSlot = 1 LIMIT 1")
    abstract suspend fun syncedCollectionOf(wallpaperId: String): Long?

    @Query("SELECT COUNT(*) FROM collection WHERE isSynced = 0")
    abstract suspend fun localDefaultCount(): Int

    @Query("SELECT * FROM pending_op WHERE state = 'PENDING' ORDER BY id")
    abstract suspend fun pendingOps(): List<PendingOpEntity>

    @Query("SELECT COUNT(*) FROM pending_op WHERE state = 'PENDING'")
    abstract suspend fun pendingCount(): Int

    /** Wallpapers with an unsent add/remove. A pull must not overwrite or delete these. */
    @Query("SELECT wallpaperId FROM pending_op WHERE state = 'PENDING' AND wallpaperId IS NOT NULL")
    abstract suspend fun pendingWallpaperIds(): List<String>

    /** Site collections the user deleted here but whose delete hasn't been sent yet. A pull must not bring them back. */
    @Query("""
        SELECT remoteCollectionId FROM pending_op
        WHERE state = 'PENDING' AND type = 'DELETE_COLLECTION' AND remoteCollectionId IS NOT NULL
    """)
    abstract suspend fun pendingDeleteRemoteIds(): List<Long>

    /** Collections created here whose create hasn't been sent yet. */
    @Query("SELECT COUNT(*) FROM pending_op WHERE state = 'PENDING' AND type = 'CREATE_COLLECTION'")
    abstract suspend fun pendingCreateCount(): Int

    /** How many items the current pull (gen) has saved for this collection. */
    @Query("SELECT COUNT(*) FROM collection_item WHERE collectionId = :cid AND syncGen = :gen")
    abstract suspend fun countGen(cid: Long, gen: Long): Int

    // ============================================================ plain writes

    @Insert
    abstract suspend fun insertCollection(collection: CollectionEntity): Long

    /** Returns -1 if the row was ignored (already there, or the wallpaper is in another synced collection). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertItem(item: CollectionItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertItems(items: List<CollectionItemEntity>)

    /** Used by pulls: replaces an existing row, which also evicts the same wallpaper from another synced collection. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertItems(items: List<CollectionItemEntity>)

    @Insert
    abstract suspend fun insertOp(op: PendingOpEntity): Long

    @Query("UPDATE collection SET remoteId = :remoteId WHERE id = :id")
    abstract suspend fun setRemoteId(id: Long, remoteId: Long)

    /** Name and item count as the site reports them. */
    @Query("UPDATE collection SET name = :name, remoteCount = :count WHERE id = :id")
    abstract suspend fun updateRemoteInfo(id: Long, name: String, count: Int)

    /** Marks a collection as "pull me from page 1" under a new generation id. */
    @Query("UPDATE collection SET lastPage = 0, syncedPages = 0, pullGen = :gen, lastSyncedAt = 0 WHERE id = :id")
    abstract suspend fun startPull(id: Long, gen: Long)

    @Query("UPDATE collection SET lastPage = :lastPage, syncedPages = :page WHERE id = :id")
    abstract suspend fun setPageDone(id: Long, page: Int, lastPage: Int)

    @Query("UPDATE collection SET lastSyncedAt = :at WHERE id = :id")
    abstract suspend fun setSyncedAt(id: Long, at: Long)

    @Query("DELETE FROM collection WHERE id = :id")
    abstract suspend fun deleteCollectionRow(id: Long)

    /** Deletes every synced collection; their items go with them (cascade). Local collections stay. */
    @Query("DELETE FROM collection WHERE isSynced = 1")
    abstract suspend fun deleteAllSyncedCollections()

    @Query("DELETE FROM collection_item WHERE wallpaperId = :wallpaperId AND collectionId = :collectionId")
    abstract suspend fun deleteItem(wallpaperId: String, collectionId: Long)

    @Query("UPDATE collection SET sortOrder = :sortOrder WHERE id = :id")
    abstract suspend fun updateSortOrder(id: Long, sortOrder: Int)

    @Transaction
    open suspend fun updateCollectionOrder(orderedIds: List<Long>) {
        orderedIds.forEachIndexed { index, id ->
            updateSortOrder(id, index)
        }
    }

    /**
     * End of a pull: drops synced items the site no longer has (they carry an older pull id).
     * Never touches items with a queued op, or items added here after this pull started.
     */
    @Query("""
        DELETE FROM collection_item
        WHERE collectionId = :cid AND syncedSlot = 1 AND syncGen != :gen AND addedAt <= :gen
          AND wallpaperId NOT IN (SELECT wallpaperId FROM pending_op
                                  WHERE state = 'PENDING' AND wallpaperId IS NOT NULL)
    """)
    abstract suspend fun sweep(cid: Long, gen: Long)

    @Query("DELETE FROM pending_op WHERE id = :id")
    abstract suspend fun deleteOp(id: Long)

    @Query("UPDATE pending_op SET state = 'REJECTED', lastError = :error WHERE id = :id")
    abstract suspend fun rejectOp(id: Long, error: String?)

    @Query("DELETE FROM pending_op WHERE state = 'REJECTED'")
    abstract suspend fun clearRejectedOps()

    @Query("DELETE FROM pending_op WHERE collectionId = :collectionId")
    abstract suspend fun deleteOpsForCollection(collectionId: Long)

    @Query("DELETE FROM pending_op WHERE collectionId = :collectionId AND type = 'ADD'")
    abstract suspend fun deleteAddOpsForCollection(collectionId: Long)

    @Query("DELETE FROM pending_op")
    abstract suspend fun deleteAllOps()

    @Query("UPDATE collection SET isSynced = 0, remoteId = NULL WHERE id = :id")
    abstract suspend fun markLocal(id: Long)

    @Query("UPDATE collection_item SET syncedSlot = NULL WHERE collectionId = :collectionId")
    abstract suspend fun clearSlotsOf(collectionId: Long)

    // ============================================================ transactions

    /** Makes sure there's at least one local collection (the "Default"/favorites one). */
    @Transaction
    open suspend fun ensureLocalDefault(name: String) {
        if (localDefaultCount() == 0) {
            insertCollection(CollectionEntity(name = name, isSynced = false))
        }
    }

    @Transaction
    open suspend fun createLocal(name: String): Long =
        insertCollection(CollectionEntity(name = name, isSynced = false))

    /** Creates the collection locally right away and queues the create on the site. */
    @Transaction
    open suspend fun createSynced(name: String, isPublic: Boolean): Long {
        val id = insertCollection(CollectionEntity(name = name, isSynced = true))
        insertOp(
            PendingOpEntity(
                type = OpType.CREATE_COLLECTION,
                collectionId = id,
                label = name,
                isPublic = isPublic
            )
        )
        return id
    }

    /**
     * Deletes locally (items cascade) and, for a collection that exists on the site, queues the
     * remote delete. Anything still queued for the collection is dropped.
     */
    @Transaction
    open suspend fun deleteCollectionAndQueue(collection: CollectionEntity) {
        deleteOpsForCollection(collection.id)
        val remoteId = collection.remoteId
        if (collection.isSynced && remoteId != null) {
            insertOp(PendingOpEntity(type = OpType.DELETE_COLLECTION, remoteCollectionId = remoteId))
        }
        deleteCollectionRow(collection.id)
    }

    /** Returns false if nothing was added (already there, or in another synced collection). */
    @Transaction
    open suspend fun addItem(wallpaper: Wallpaper, collection: CollectionEntity): Boolean {
        val inserted = insertItem(
            CollectionItemEntity(
                wallpaperId = wallpaper.id,
                collectionId = collection.id,
                thumbUrl = wallpaper.thumbs.large,
                fullUrl = wallpaper.url,
                dimensionsX = wallpaper.dimension_x,
                dimensionsY = wallpaper.dimension_y,
                purity = wallpaper.purity,
                syncedSlot = if (collection.isSynced) 1 else null
            )
        )
        if (inserted == -1L) return false
        if (collection.isSynced) {
            insertOp(PendingOpEntity(type = OpType.ADD, collectionId = collection.id, wallpaperId = wallpaper.id))
        }
        return true
    }

    @Transaction
    open suspend fun removeItem(wallpaperId: String, collection: CollectionEntity) {
        deleteItem(wallpaperId, collection.id)
        if (collection.isSynced) {
            insertOp(PendingOpEntity(type = OpType.REMOVE, wallpaperId = wallpaperId))
        }
    }

    /** Moves a wallpaper between two synced collections: remove, then add (the site ignores a plain add). */
    @Transaction
    open suspend fun moveSynced(wallpaperId: String, from: CollectionEntity, to: CollectionEntity) {
        deleteItem(wallpaperId, from.id)
        insertItem(CollectionItemEntity(wallpaperId = wallpaperId, collectionId = to.id, syncedSlot = 1))
        insertOp(PendingOpEntity(type = OpType.REMOVE, wallpaperId = wallpaperId))
        insertOp(PendingOpEntity(type = OpType.ADD, collectionId = to.id, wallpaperId = wallpaperId))
    }

    /** A synced collection whose create was refused becomes a local one, so the user loses nothing. */
    @Transaction
    open suspend fun demoteToLocal(collectionId: Long) {
        clearSlotsOf(collectionId)
        markLocal(collectionId)
        deleteAddOpsForCollection(collectionId)
    }

    /**
     * Logout / account switch: removes every synced collection and everything queued for the site.
     * Local collections and their wallpapers are untouched.
     */
    @Transaction
    open suspend fun removeAllSynced() {
        deleteAllOps()
        deleteAllSyncedCollections()
    }

    /**
     * Step 1 of a pull: bring the list of synced collections in line with the site's list, and decide
     * which ones need pulling (new, never pulled, changed, stale, or half-downloaded and changed since).
     * Respects queued deletes and creates so a pull can't undo the user's unsent changes.
     */
    @Transaction
    open suspend fun upsertRemoteCollections(remote: List<RemoteCollection>, gen: Long, staleBefore: Long) {
        val local = syncedCollections()

        // An empty list is far more likely a failed fetch than "the user deleted every collection".
        if (remote.isEmpty() && local.any { it.remoteId != null }) return

        val deleting = pendingDeleteRemoteIds().toSet()
        val creating = pendingCreateCount() > 0
        val remoteIds = remote.mapTo(mutableSetOf()) { it.id }

        // Collections deleted on the site disappear here too (their items cascade).
        local.forEach { c -> c.remoteId?.let { if (it !in remoteIds) deleteCollectionRow(c.id) } }

        val byRemoteId = local.mapNotNull { c -> c.remoteId?.let { it to c } }.toMap()

        for (r in remote) {
            if (r.id in deleting) continue          // our delete hasn't been sent yet, so don't resurrect it

            val existing = byRemoteId[r.id]
            if (existing == null) {
                // Could be the collection we're in the middle of creating; the next pull will adopt it.
                if (creating) continue
                val localId = insertCollection(
                    CollectionEntity(name = r.label, isSynced = true, remoteId = r.id, remoteCount = r.count)
                )
                startPull(localId, gen)
                continue
            }

            updateRemoteInfo(existing.id, r.label, r.count)   // `existing` below is the snapshot from before this

            val complete = existing.lastPage > 0 && existing.syncedPages >= existing.lastPage
            val needsPull = when {
                existing.lastPage == 0 -> true                          // never pulled (e.g. just created)
                !complete -> existing.remoteCount != r.count            // half-downloaded: resume, unless the site changed
                existing.lastSyncedAt <= staleBefore -> true            // stale, or the last sweep was skipped
                getItemCount(existing.id) != r.count -> true            // count differs from the site's
                else -> false                                           // nothing to do: zero extra requests
            }
            if (needsPull) startPull(existing.id, gen)
        }
    }

    /**
     * Step 2 of a pull, once per page: saves the page's wallpapers and records progress.
     * Items with a queued op are neither written nor swept, so local edits survive a running pull.
     * On the last page, stale items are swept, but only if we saw everything the site says it has.
     */
    @Transaction
    open suspend fun applyPage(
        collectionId: Long,
        wallpapers: List<Wallpaper>,
        page: Int,
        lastPage: Int,
        total: Int,
        gen: Long
    ) {
        // A blank page for a non-empty collection is a bad response. Throw before writing anything.
        if (wallpapers.isEmpty() && total > 0) {
            error("empty page $page for collection $collectionId (total=$total)")
        }

        val busy = pendingWallpaperIds().toSet()

        // REPLACE also evicts the same wallpaper from another synced collection (the unique index),
        // which is right: the site says it lives here now.
        upsertItems(
            wallpapers.filter { it.id !in busy }.mapIndexed { i, w ->
                CollectionItemEntity(
                    wallpaperId = w.id,
                    collectionId = collectionId,
                    fullUrl = w.path,
                    dimensionsX = w.dimension_x,
                    dimensionsY = w.dimension_y,
                    purity = w.purity,
                    thumbUrl = w.thumbs.original,
                    syncedSlot = 1,
                    syncGen = gen,
                    addedAt = gen - ((page - 1) * 24L + i)   // keeps the site's newest-first order
                )
            }
        )
        setPageDone(collectionId, page, lastPage)

        if (page >= lastPage) {
            // Slack = items with queued ops, which we skipped on purpose.
            if (countGen(collectionId, gen) + busy.size >= total) {
                sweep(collectionId, gen)
                setSyncedAt(collectionId, System.currentTimeMillis())
            }
            // Otherwise lastSyncedAt stays 0, so the next refresh pulls this collection again.
        }
    }
}