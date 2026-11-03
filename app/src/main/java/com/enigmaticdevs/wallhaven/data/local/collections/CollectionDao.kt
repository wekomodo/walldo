package com.enigmaticdevs.wallhaven.data.local.collections

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CollectionDao {

    // ------------------------------------------------------------ reads for the UI

    @Query("""SELECT c.*, (SELECT COUNT(*) FROM collection_item i WHERE i.collectionId = c.id) AS itemCount FROM collection AS c ORDER BY c.isDefault DESC, c.createdAt""")
    abstract fun observeCollections(): Flow<List<CollectionWithCount>>

    @Query("SELECT wallpaperId FROM collection_item WHERE collectionId = :collectionId ORDER BY addedAt DESC")
    abstract fun observeWallpaperIds(collectionId: Long): Flow<List<String>>

    /** For "is this wallpaper saved anywhere" state on a detail screen. */
    @Query("SELECT collectionId FROM collection_item WHERE wallpaperId = :wallpaperId")
    abstract fun observeCollectionIdsOf(wallpaperId: String): Flow<List<Long>>

    @Query("SELECT COUNT(*) FROM pending_op WHERE state = :state")
    abstract fun observePendingCount(state: OpState = OpState.PENDING): Flow<Int>

    @Query("SELECT * FROM pending_op WHERE state = :state ORDER BY id")
    abstract fun observeRejectedOps(state: OpState = OpState.REJECTED): Flow<List<PendingOpEntity>>

    // ------------------------------------------------------------ lookups

    @Query("SELECT * FROM collection WHERE id = :id")
    abstract suspend fun collection(id: Long): CollectionEntity?

    @Query("SELECT * FROM collection WHERE isSynced = 1")
    abstract suspend fun syncedCollections(): List<CollectionEntity>

    /** The one synced collection this wallpaper is in, or null. */
    @Query("SELECT collectionId FROM collection_item WHERE wallpaperId = :wallpaperId AND syncedSlot = 1 LIMIT 1")
    abstract suspend fun syncedCollectionOf(wallpaperId: String): Long?

    @Query("SELECT COUNT(*) FROM collection WHERE isDefault = 1 AND isSynced = 0")
    abstract suspend fun localDefaultCount(): Int

    @Query("SELECT * FROM pending_op WHERE state = 'PENDING' ORDER BY id")
    abstract suspend fun pendingOps(): List<PendingOpEntity>

    @Query("SELECT COUNT(*) FROM pending_op WHERE state = 'PENDING'")
    abstract suspend fun pendingCount(): Int

    // ------------------------------------------------------------ plain writes

    @Insert
    abstract suspend fun insertCollection(collection: CollectionEntity): Long

    /** Returns -1 if the row was ignored (already there, or the wallpaper is in another synced collection). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertItem(item: CollectionItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertItems(items: List<CollectionItemEntity>)

    @Insert
    abstract suspend fun insertOp(op: PendingOpEntity): Long

    @Query("UPDATE collection SET remoteId = :remoteId WHERE id = :id")
    abstract suspend fun setRemoteId(id: Long, remoteId: Long)

    @Query("UPDATE collection SET name = :name, isDefault = :isDefault WHERE id = :id")
    abstract suspend fun updateRemoteInfo(id: Long, name: String, isDefault: Boolean)

    @Query("DELETE FROM collection WHERE id = :id")
    abstract suspend fun deleteCollectionRow(id: Long)

    @Query("DELETE FROM collection_item WHERE wallpaperId = :wallpaperId AND collectionId = :collectionId")
    abstract suspend fun deleteItem(wallpaperId: String, collectionId: Long)

    @Query("DELETE FROM collection_item WHERE syncedSlot = 1")
    abstract suspend fun deleteAllSyncedItems()

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

    @Query("UPDATE collection SET isSynced = 0, remoteId = NULL, isDefault = 0 WHERE id = :id")
    abstract suspend fun markLocal(id: Long)

    @Query("UPDATE collection_item SET syncedSlot = NULL WHERE collectionId = :collectionId")
    abstract suspend fun clearSlotsOf(collectionId: Long)

    @Query("UPDATE collection SET isSynced = 0, remoteId = NULL, isDefault = 0 WHERE isSynced = 1")
    abstract suspend fun markAllLocal()

    @Query("UPDATE collection_item SET syncedSlot = NULL WHERE syncedSlot IS NOT NULL")
    abstract suspend fun clearAllSlots()

    // ------------------------------------------------------------ transactions

    @Transaction
    open suspend fun ensureLocalDefault(name: String) {
        if (localDefaultCount() == 0) {
            insertCollection(CollectionEntity(name = name, isSynced = false, isDefault = true))
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
    open suspend fun addItem(wallpaperId: String, collection: CollectionEntity): Boolean {
        val inserted = insertItem(
            CollectionItemEntity(
                wallpaperId = wallpaperId,
                collectionId = collection.id,
                syncedSlot = if (collection.isSynced) 1 else null
            )
        )
        if (inserted == -1L) return false
        if (collection.isSynced) {
            insertOp(PendingOpEntity(type = OpType.ADD, collectionId = collection.id, wallpaperId = wallpaperId))
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

    /** A synced collection whose create was refused becomes a local one, so nothing is lost. */
    @Transaction
    open suspend fun demoteToLocal(collectionId: Long) {
        clearSlotsOf(collectionId)
        markLocal(collectionId)
        deleteAddOpsForCollection(collectionId)
    }

    /** On logout: everything becomes local and nothing is left in the queue. */
    @Transaction
    open suspend fun demoteAllToLocal() {
        clearAllSlots()
        markAllLocal()
        deleteAllOps()
    }

    /**
     * Makes the synced collections match the site. Returns false (and changes nothing) if
     * ops were queued in the meantime, because the pull would overwrite those local changes.
     */
    @Transaction
    open suspend fun applyRemote(
        remote: List<RemoteCollection>,
        memberships: Map<Long, List<String>>
    ): Boolean {
        if (pendingCount() > 0) return false

        val local = syncedCollections()
        val remoteIds = remote.map { it.id }.toSet()

        // Collections deleted on the site disappear here too (their items cascade).
        local.forEach { c ->
            val rid = c.remoteId
            if (rid != null && rid !in remoteIds) deleteCollectionRow(c.id)
        }

        // Clear every synced item first, then re-insert, so a wallpaper moved between
        // collections on the site can't hit the unique index while the old row still exists.
        deleteAllSyncedItems()

        val byRemoteId = local.mapNotNull { c -> c.remoteId?.let { it to c } }.toMap()
        val allItemsToInsert = mutableListOf<CollectionItemEntity>() // Accumulator
        for ((id, label, isDefault) in remote) {
            val existing = byRemoteId[id]
            val localId = if (existing != null) {
                updateRemoteInfo(existing.id, label, isDefault)
                existing.id
            } else {
                insertCollection(
                    CollectionEntity(name = label, isSynced = true, remoteId = id, isDefault = isDefault)
                )
            }

            // Add to the list instead of hitting the DB
            allItemsToInsert.addAll(
                memberships[id].orEmpty().map {
                    CollectionItemEntity(wallpaperId = it, collectionId = localId, syncedSlot = 1)
                }
            )
        }

        // Execute a single batched database write
        if (allItemsToInsert.isNotEmpty()) {
            insertItems(allItemsToInsert)
        }
        return true
    }
}