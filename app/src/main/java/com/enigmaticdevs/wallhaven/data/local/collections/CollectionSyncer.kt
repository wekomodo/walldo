package com.enigmaticdevs.wallhaven.data.local.collections

import android.util.Log
import com.enigmaticdevs.wallhaven.ui.screens.features.login.AccountOutcome
import com.enigmaticdevs.wallhaven.ui.screens.features.login.WallhavenAccountApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.text.contains

sealed interface SyncResult {
    /**
     * Everything queued was sent. rejectedWallpaperIds are adds the site silently ignored.
     * pulled = false means ops were queued during the sync, so the pull waits for the next one.
     */
    data class Done(
        val rejectedWallpaperIds: List<String> = emptyList(),
        val pulled: Boolean = true
    ) : SyncResult

    data object Skipped : SyncResult          // not pro
    data object NeedsLogin : SyncResult       // show the login screen
    data object NeedsCloudflare : SyncResult  // show the solver screen
    data object SessionExpired : SyncResult   // still 419 after the retry; try again later
    data class Failed(val message: String?) : SyncResult  // network error; ops stay queued
}

/**
 * Sends the queued ops in order, then pulls the site's state and overwrites the synced
 * collections with it (the site wins). Run it from an app-level scope or WorkManager.
 */
class CollectionSyncer(
    private val dao: CollectionDao,
    private val account: WallhavenAccountApi,
    private val remote: WallhavenCollectionsRemote,
    private val isPro: () -> Boolean,
) {
    private companion object {
        const val TAG = "CollectionSyncer"
    }

    private val mutex = Mutex()

    suspend fun sync(): SyncResult = mutex.withLock {
        if (!isPro()) return@withLock SyncResult.Skipped
        try {
            // wallpaperId -> remote collection it should now be in (to spot ignored adds)
            val expected = mutableMapOf<String, Long>()

            for (op in dao.pendingOps()) {
                // A non-null result is a blocker (login, Cloudflare, expiry, network): stop here.
                // Everything not sent yet stays queued and the pull is skipped.
                runOp(op, expected)?.let { return@withLock it }
            }
            pull(expected)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "sync failed", e)
            SyncResult.Failed(e.message)
        }
    }

    // ------------------------------------------------------------ sending ops

    private suspend fun runOp(op: PendingOpEntity, expected: MutableMap<String, Long>): SyncResult? =
        when (op.type) {
            OpType.CREATE_COLLECTION -> createRemote(op)
            OpType.DELETE_COLLECTION -> deleteRemote(op)
            OpType.ADD -> addRemote(op, expected)
            OpType.REMOVE -> removeRemote(op, expected)
        }

    private suspend fun createRemote(op: PendingOpEntity): SyncResult? {
        val collection = op.collectionId?.let { dao.collection(it) }
        if (collection == null || collection.remoteId != null) {
            dao.deleteOp(op.id)   // deleted meanwhile, or already linked
            return null
        }

        // The site doesn't return the new ID, so snapshot the IDs, create, and diff.
        val before = remote.fetchCollections().map { it.id }.toSet()

        when (val outcome = account.createCollection(collection.name, op.isPublic)) {
            is AccountOutcome.Success -> Unit
            AccountOutcome.NeedsLogin -> return SyncResult.NeedsLogin
            AccountOutcome.NeedsCloudflare -> return SyncResult.NeedsCloudflare
            AccountOutcome.SessionExpired -> return SyncResult.SessionExpired
            is AccountOutcome.Failed -> {
                if (outcome.code == -1) return SyncResult.Failed(outcome.message)
                rejectCreate(op, collection.id, "HTTP ${outcome.code}")
                return null
            }
        }

        val created = remote.fetchCollections().filter { it.id !in before }
        val match = created.filter { it.label == collection.name }.maxByOrNull { it.id }
        if (match == null) {
            rejectCreate(op, collection.id, "Couldn't find the new collection on wallhaven")
            return null
        }
        dao.setRemoteId(collection.id, match.id)
        dao.deleteOp(op.id)
        return null
    }

    private suspend fun rejectCreate(op: PendingOpEntity, collectionId: Long, error: String) {
        dao.rejectOp(op.id, error)
        dao.demoteToLocal(collectionId)   // keep the user's collection and wallpapers, as local
    }

    private suspend fun deleteRemote(op: PendingOpEntity): SyncResult? {
        val remoteId = op.remoteCollectionId ?: run { dao.deleteOp(op.id); return null }
        return finish(op, account.deleteCollection(remoteId))
    }

    private suspend fun addRemote(op: PendingOpEntity, expected: MutableMap<String, Long>): SyncResult? {
        val wallpaperId = op.wallpaperId ?: run { dao.deleteOp(op.id); return null }
        val collection = op.collectionId?.let { dao.collection(it) }
        if (collection == null) {
            dao.deleteOp(op.id)   // collection deleted meanwhile
            return null
        }
        val remoteId = collection.remoteId
        if (remoteId == null) {
            dao.rejectOp(op.id, "Collection isn't on wallhaven")
            return null
        }
        return finish(op, account.addToCollection(wallpaperId, remoteId)) {
            expected[wallpaperId] = remoteId
        }
    }

    private suspend fun removeRemote(op: PendingOpEntity, expected: MutableMap<String, Long>): SyncResult? {
        val wallpaperId = op.wallpaperId ?: run { dao.deleteOp(op.id); return null }
        return finish(op, account.removeWallpaper(wallpaperId)) {
            expected.remove(wallpaperId)
        }
    }

    /** Success deletes the op; a refusal rejects it; a blocker is returned and the op stays queued. */
    private suspend fun finish(
        op: PendingOpEntity,
        outcome: AccountOutcome<Unit>,
        onSuccess: () -> Unit = {}
    ): SyncResult? = when (outcome) {
        is AccountOutcome.Success -> {
            dao.deleteOp(op.id)
            onSuccess()
            null
        }
        AccountOutcome.NeedsLogin -> SyncResult.NeedsLogin
        AccountOutcome.NeedsCloudflare -> SyncResult.NeedsCloudflare
        AccountOutcome.SessionExpired -> SyncResult.SessionExpired
        is AccountOutcome.Failed ->
            if (outcome.code == -1) {
                SyncResult.Failed(outcome.message)
            } else {
                dao.rejectOp(op.id, "HTTP ${outcome.code}")
                null
            }
    }

    // ------------------------------------------------------------ pulling

    private suspend fun pull(expected: Map<String, Long>): SyncResult {
        val collections = remote.fetchCollections()
        val memberships = collections.associate { it.id to remote.fetchWallpaperIds(it.id) }

        if (!dao.applyRemote(collections, memberships)) {
            return SyncResult.Done(pulled = false)
        }

        // An add the site accepted (302) but silently ignored shows up as a wallpaper
        // that isn't in the collection after the pull.
        val rejected = expected
            .filter { (wallpaperId, remoteId) -> wallpaperId !in memberships[remoteId].orEmpty() }
            .keys
            .toList()
        return SyncResult.Done(rejectedWallpaperIds = rejected)
    }
}