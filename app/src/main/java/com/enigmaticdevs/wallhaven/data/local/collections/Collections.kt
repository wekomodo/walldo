package com.enigmaticdevs.wallhaven.data.local.collections

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "collection", indices = [Index("remoteId")])
data class CollectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** true = mirrors a wallhaven collection (pro only). false = lives only on this device. */
    val isSynced: Boolean,
    /** wallhaven's collection ID. Null for local collections, and for synced ones not created on the site yet. */
    val remoteId: Long? = null,
    /** when was this collection created */
    val createdAt: Long = System.currentTimeMillis(),

    /** sort order of the collection stays local */
    val sortOrder : Int = 0,

    // ---- pull bookkeeping (only meaningful for synced collections) ----
    /** The item count the site last reported. Shown while the collection is still partly downloaded. */
    val remoteCount: Int? = null,
    /** Number of pages the site has for this collection. 0 = no page fetched yet. */
    val lastPage: Int = 0,
    /** How many pages have been downloaded and saved. The collection is complete when syncedPages >= lastPage. */
    val syncedPages: Int = 0,
    /** Id of the pull in progress / last pull (a timestamp). Items saved by that pull carry the same value. */
    val pullGen: Long = 0,
    /** When the last complete pull finished. 0 = never, or the last one was skipped, so pull again. */
    val lastSyncedAt: Long = 0,
    /** what collection to use for autowallpaper */
    val isAutoWallpaper : Boolean = false
)

// Which wallpaper is in which collection.
// Local collections: a wallpaper can be in any number of them (syncedSlot is NULL, and NULLs never collide).
// Synced collections: a wallpaper can be in at most one (syncedSlot is always 1, so the unique index rejects a second).
@Entity(
    tableName = "collection_item",
    primaryKeys = ["wallpaperId", "collectionId"],
    foreignKeys = [
        ForeignKey(
            entity = CollectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE // deleting a collection deletes its items
        )
    ],
    indices = [
        Index("collectionId"),
        Index(value = ["wallpaperId", "syncedSlot"], unique = true)
    ]
)
data class CollectionItemEntity(
    val wallpaperId: String,
    val collectionId: Long,

    //offline caching of wallpaper data
    val thumbUrl : String? = null,
    val fullUrl : String? = null,
    val dimensionsX : Int? = null,
    val dimensionsY : Int? = null,
    val purity : String? = null,
    /** 1 for items in a synced collection, NULL for items in a local one. See the note above. */
    val syncedSlot: Int? = null,
    /** Order in the list: newest first. For pulled items it follows the site's order. */
    val addedAt: Long = System.currentTimeMillis(),
    /** Id of the pull that saved this item. Items from older pulls get swept at the end of a new pull. */
    val syncGen: Long = 0
)

enum class OpType { CREATE_COLLECTION, DELETE_COLLECTION, ADD, REMOVE }

/** PENDING ops are sent in order. REJECTED ops were refused by the site and are kept for the UI. */
enum class OpState { PENDING, REJECTED }

@Entity(tableName = "pending_op")
data class PendingOpEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: OpType,
    val state: OpState = OpState.PENDING,
    /** Local collection ID (CREATE_COLLECTION, ADD). */
    val collectionId: Long? = null,
    /** DELETE_COLLECTION only: the local row is already gone by the time this runs. */
    val remoteCollectionId: Long? = null,
    /** ADD, REMOVE. */
    val wallpaperId: String? = null,
    /** CREATE_COLLECTION only. */
    val label: String? = null,
    val isPublic: Boolean = false,
    val lastError: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

data class CollectionWithCount(
    @Embedded val collection: CollectionEntity,
    val itemCount: Int
)