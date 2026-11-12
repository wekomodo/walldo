package com.enigmaticdevs.wallhaven.data.local.collections

import android.util.Log
import com.enigmaticdevs.wallhaven.data.repository.GlobalFlagsRepository
import com.enigmaticdevs.wallhaven.data.repository.SettingsRepository
import com.enigmaticdevs.wallhaven.data.repository.WallpaperRepository
import com.enigmaticdevs.wallhaven.ui.screens.login.AccountOutcome
import com.enigmaticdevs.wallhaven.ui.screens.login.WallhavenAccountApi
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed interface SyncResult {
    /** Everything queued was sent. rejectedWallpaperIds are adds the site silently ignored. */
    data class Done(val rejectedWallpaperIds: List<String> = emptyList()) : SyncResult

    data object Skipped : SyncResult          // not pro
    data object NeedsLogin : SyncResult       // show the login screen
    data object NeedsCloudflare : SyncResult  // show the solver screen
    data object SessionExpired : SyncResult   // still 419 after the retry; try again later
    data class Failed(val message: String?) : SyncResult  // network error, 429, 5xx: ops stay queued
}

/**
 * Two jobs, kept apart so a long download never delays the user's changes:
 *  1. PUSH (sync): send the queued ops to the site in order, then check the adds landed.
 *  2. PULL (pullFirstPages / pullRemainingPages): download the site's collections into the DB.
 * Run it through CollectionSyncManager; screens never call this directly.
 */
@Inject
class CollectionSyncer(
    private val db: CollectionsDatabase,
    private val account: WallhavenAccountApi,
    private val remote: WallpaperRepository,
    private val globalFlagsRepository: GlobalFlagsRepository,
    private val settingsRepository: SettingsRepository
) {
    private companion object {
        const val TAG = "CollectionSyncer"
        const val PAGE_SIZE = 24                          // wallhaven's fixed API page size
        const val STALE_MS = 24L * 60 * 60 * 1000         // re-pull a collection at least this often
    }

    private val dao = db.collectionDao()

    /** Read on every call (not once at construction), so upgrading to pro takes effect immediately. */
    private val isPro get() = globalFlagsRepository.proStatus.value

    private val pushMutex = Mutex()
    private val pullMutex = Mutex()   // separate from push, so a minutes-long pull never blocks a push

    // ============================================================ push

    /** Sends the queued ops in order, then verifies the adds. */
    suspend fun sync(): SyncResult = pushMutex.withLock {
        if (!isPro) return@withLock SyncResult.Skipped
        try {
            // wallpaperId -> remote collection it should now be in (to spot ignored adds)
            val expected = mutableMapOf<String, Long>()

            for (op in dao.pendingOps()) {
                // A non-null result is a blocker (login, Cloudflare, expiry, network): stop here.
                // Everything not sent yet stays queued.
                runOp(op, expected)?.let { return@withLock it }
            }
            SyncResult.Done(rejectedWallpaperIds = verifyAdds(expected))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "sync failed", e)
            SyncResult.Failed(e.message)
        }
    }

    private suspend fun runOp(op: PendingOpEntity, expected: MutableMap<String, Long>): SyncResult? =
        when (op.type) {
            OpType.CREATE_COLLECTION -> createRemote(op)
            OpType.DELETE_COLLECTION -> deleteRemote(op)
            OpType.ADD -> addRemote(op, expected)
            OpType.REMOVE -> removeRemote(op, expected)
        }

    /** Creates the collection on the site, then links the local row to the new remote id. */
    private suspend fun createRemote(op: PendingOpEntity): SyncResult? {
        val collection = op.collectionId?.let { dao.collection(it) }
        if (collection == null || collection.remoteId != null) {
            dao.deleteOp(op.id)   // deleted meanwhile, or already linked
            return null
        }

        // The site doesn't return the new ID, so snapshot the IDs, create, and diff.
        val before = remote.fetchCollections().data.map { it.id }.toSet()

        when (val outcome = account.createCollection(collection.name, op.isPublic)) {
            is AccountOutcome.Success -> Unit
            AccountOutcome.NeedsLogin -> return SyncResult.NeedsLogin
            AccountOutcome.NeedsCloudflare -> return SyncResult.NeedsCloudflare
            AccountOutcome.SessionExpired -> return SyncResult.SessionExpired
            is AccountOutcome.Failed -> {
                if (isTransient(outcome.code)) {
                    return SyncResult.Failed(outcome.message ?: "HTTP ${outcome.code}")
                }
                rejectCreate(op, collection.id, "HTTP ${outcome.code}")
                return null
            }
        }

        val created = remote.fetchCollections().data.filter { it.id !in before }
        val match = created.filter { it.label == collection.name }.maxByOrNull { it.id }
        if (match == null) {
            rejectCreate(op, collection.id, "Couldn't find the new collection on wallhaven")
            return null
        }
        dao.setRemoteId(collection.id, match.id)
        dao.deleteOp(op.id)
        return null
    }

    /** The site refused the create: keep the user's collection and wallpapers, but as a local collection. */
    private suspend fun rejectCreate(op: PendingOpEntity, collectionId: Long, error: String) {
        dao.rejectOp(op.id, error)
        dao.demoteToLocal(collectionId)
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
        return finish(op, account.removeWallpaper(wallpaperId)) { expected.remove(wallpaperId) }
    }

    /**
     * Success deletes the op. A definitive refusal rejects it (kept for the UI).
     * Blockers and transient errors (network, 429, 5xx) leave it queued so it's retried.
     */
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
            if (isTransient(outcome.code)) {
                SyncResult.Failed(outcome.message ?: "HTTP ${outcome.code}")
            } else {
                dao.rejectOp(op.id, "HTTP ${outcome.code}")
                null
            }
    }

    /** -1 = network error. These must never permanently reject a user's change. */
    private fun isTransient(code: Int) = code == -1 || code == 408 || code == 429 || code >= 500

    /**
     * Checks that the adds we just sent really landed. New adds go to the front of a collection, so a batch of
     * N adds sits within the first ceil(N / 24) pages. Only if something is still missing after that do we scan
     * the whole collection, before calling it a rejection.
     */
    private suspend fun verifyAdds(expected: Map<String, Long>): List<String> {
        if (expected.isEmpty()) return emptyList()
        val username = settingsRepository.usernameFlow.first()
        val rejected = mutableListOf<String>()

        for ((remoteId, ids) in expected.entries.groupBy({ it.value }, { it.key })) {
            val needed = (ids.size + PAGE_SIZE - 1) / PAGE_SIZE
            val seen = mutableSetOf<String>()
            var page = 1
            var last = 1
            while (page <= minOf(needed, last)) {
                val res = remote.fetchCollectionWallpapers(remoteId, username, page)
                res.wallpaperList.forEach { seen += it.id }
                last = maxOf(res.meta.last_page, 1)
                page++
            }
            var missing = ids.filterNot { it in seen }
            if (missing.isNotEmpty() && last > needed) {
                val all = fetchAllIds(remoteId, username)
                missing = ids.filterNot { it in all }
            }
            rejected += missing
        }
        return rejected
    }

    private suspend fun fetchAllIds(remoteId: Long, username: String): Set<String> {
        val ids = mutableSetOf<String>()
        var page = 1
        var last = 1
        do {
            val res = remote.fetchCollectionWallpapers(remoteId, username, page)
            res.wallpaperList.forEach { ids += it.id }
            last = maxOf(res.meta.last_page, 1)
            page++
        } while (page <= last)
        return ids
    }

    // ============================================================ pull

    /**
     * Phase 1, quick: fetch the collection list, decide which collections changed, and download page 1 of each
     * collection that has nothing yet. That gives the UI thumbnails and real counts right away.
     * When nothing changed on the site this costs a single request.
     */
    suspend fun pullFirstPages(): SyncResult = pullMutex.withLock {
        if (!isPro) return@withLock SyncResult.Skipped
        try {
            val username = settingsRepository.usernameFlow.first()
            val gen = System.currentTimeMillis()
            val collections = remote.fetchCollections().data
            dao.upsertRemoteCollections(collections, gen, staleBefore = gen - STALE_MS)
            for (c in dao.collectionsNeedingSync()) {
                if (c.syncedPages == 0) pullPage(c, 1, username)
            }
            SyncResult.Done()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "first pages failed", e)
            SyncResult.Failed(e.message)
        }
    }

    /**
     * Phase 2, slow: download the remaining pages of every incomplete collection. Each page is saved on its own,
     * so progress survives a crash or a 429 (the next run resumes from syncedPages).
     * Returns true when everything is complete, false when the caller should retry later.
     */
    suspend fun pullRemainingPages(): Boolean = pullMutex.withLock {
        if (!isPro) return@withLock true
        try {
            val username = settingsRepository.usernameFlow.first()
            for (c in dao.collectionsNeedingSync()) {
                var page = c.syncedPages + 1
                var last = maxOf(c.lastPage, 1)
                while (page <= last) {
                    last = pullPage(c, page, username)
                    page++
                }
            }
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "background pull failed", e)
            false
        }
    }

    /** Fetches one page and saves it. Returns the collection's page count as the site just reported it. */
    private suspend fun pullPage(c: CollectionEntity, page: Int, username: String): Int {
        val res = remote.fetchCollectionWallpapers(c.remoteId!!, username, page)
        val lastPage = maxOf(res.meta.last_page, 1)
        dao.applyPage(c.id, res.wallpaperList, page, lastPage, res.meta.total, c.pullGen)
        return lastPage
    }

    // ============================================================ logout

    /**
     * Logout / account switch: waits for any in-flight push or pull to stop touching the DB, then removes every
     * synced collection and everything queued. Local collections stay. The manager cancels the running pull first.
     */
    suspend fun clearSyncedData() {
        pushMutex.withLock {
            pullMutex.withLock {
                dao.removeAllSynced()
            }
        }
    }
}