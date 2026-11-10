package com.enigmaticdevs.wallhaven.data.local.collections

import android.util.Log
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.milliseconds

/** What the sync indicator shows. */
data class SyncState(
    val pushing: Boolean,          // sending queued changes to the site right now
    val pulling: Boolean,          // downloading from the site right now (includes waiting out a rate-limit retry)
    val doneCollections: Int,      // synced collections fully downloaded
    val totalCollections: Int      // synced collections in total
) {
    /** Something is actively running. */
    val isSyncing get() = pushing || pulling

    /** Some synced collection isn't fully downloaded. True while isSyncing is false means retries gave up. */
    val isIncomplete get() = doneCollections < totalCollections
}

/**
 * App-level singleton. Decides WHEN the syncer runs; screens only call requestSync().
 *  - pushes run whenever the queue has work, one at a time, debounced
 *  - pulls run after a push finished, on their own coroutine, so they never delay a push
 */
@Inject
class CollectionSyncManager(
    private val db: CollectionsDatabase,
    private val syncer: CollectionSyncer,
) {
    private companion object {
        const val DEBOUNCE_MS = 1_500L
        const val MAX_PULL_ATTEMPTS = 5
        const val RETRY_BASE_MS = 30_000L
    }

    private val dao = db.collectionDao()

    // Lives as long as the process. The handler keeps a stray exception from killing the app.
    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO +
                CoroutineExceptionHandler { _, e -> Log.e("CollectionSyncManager", "uncaught", e) }
    )
    private val started = AtomicBoolean(false)

    /** false between logout and the next login: nothing syncs, requests are ignored. */
    private val accountActive = AtomicBoolean(true)

    // CONFLATED: any number of requests while busy collapse into one pending run.
    private val pushRequests = Channel<Unit>(Channel.CONFLATED)
    private val pullRequests = Channel<Unit>(Channel.CONFLATED)
    private val pullWanted = AtomicBoolean(false)

    /** The running pull, so logout can cancel it. */
    @Volatile private var pullJob: Job? = null

    private val pushing = MutableStateFlow(false)
    private val pulling = MutableStateFlow(false)

    private val _lastResult = MutableStateFlow<SyncResult?>(null)

    /** Observe this to react to NeedsLogin, NeedsCloudflare and rejected adds. */
    val lastResult: StateFlow<SyncResult?> = _lastResult.asStateFlow()

    /**
     * The sync indicator. pushing/pulling live in memory; the done/total counts come from Room,
     * so progress survives the app being killed.
     */
    val syncState: Flow<SyncState> = combine(
        pushing, pulling, dao.observeSyncedDone(), dao.observeSyncedTotal()
    ) { push, pull, done, total -> SyncState(push, pull, done, total) }
        .distinctUntilChanged()

    /** Safe to call more than once. */
    fun start() {
        if (!started.compareAndSet(false, true)) return

        // Pushes: one at a time. Rapid taps collapse into a single run.
        scope.launch {
            pushRequests.receiveAsFlow().collect {
                delay(DEBOUNCE_MS.milliseconds)
                pushRequests.tryReceive()   // swallow requests that arrived during the wait
                pushing.value = true
                val result = try {
                    syncer.sync()
                } finally {
                    pushing.value = false
                }
                _lastResult.value = result
                // Pull only once the queue is flushed, so a pull never races our own writes.
                if (result is SyncResult.Done && accountActive.get() && pullWanted.getAndSet(false)) {
                    pullRequests.trySend(Unit)
                }
            }
        }

        // Pulls: own collector, so a long first sync never delays a push.
        // Each pull is its own job, so logout can cancel it without killing this collector.
        scope.launch {
            pullRequests.receiveAsFlow().collect {
                val job = scope.launch { runPull() }
                pullJob = job
                job.join()
            }
        }

        // Any write to a synced collection changes the pending count, which triggers a push (not a pull).
        scope.launch {
            dao.observePendingCount().collect { if (it > 0) pushRequests.trySend(Unit) }
        }

        requestSync()   // app start: flush leftovers, then pull
    }

    /** Push, then pull. Call after login succeeds, and when the collections screen opens. */
    fun requestSync() {
        if (!accountActive.get()) return
        pullWanted.set(true)
        pushRequests.trySend(Unit)
    }

    /**
     * Call from your logout flow, BEFORE logging in as someone else. Stops the running pull, then removes every
     * synced collection and everything queued for the site. Local collections stay.
     */
    suspend fun onLoggedOut() {
        accountActive.set(false)
        pullWanted.set(false)
        pullJob?.cancelAndJoin()
        syncer.clearSyncedData()
        _lastResult.value = null
    }

    /** Call after a successful login. Starts the first sync for the new account. */
    fun onLoggedIn() {
        accountActive.set(true)
        requestSync()
    }

    /**
     * Downloads everything: the quick first pages, then the rest. On a failure (429, network) it waits and
     * tries again, resuming where it stopped. After MAX_PULL_ATTEMPTS it gives up until the next request.
     */
    private suspend fun runPull() {
        if (!accountActive.get()) return
        pulling.value = true
        try {
            for (attempt in 0 until MAX_PULL_ATTEMPTS) {
                if (!accountActive.get()) return
                val first = syncer.pullFirstPages()
                if (first is SyncResult.Skipped) return                               // not pro
                if (first is SyncResult.Done && syncer.pullRemainingPages()) return   // all complete
                delay((RETRY_BASE_MS * (attempt + 1)).milliseconds)                   // back off
            }
        } finally {
            pulling.value = false
        }
    }
}