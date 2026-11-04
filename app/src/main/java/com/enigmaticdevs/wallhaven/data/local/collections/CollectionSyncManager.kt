package com.enigmaticdevs.wallhaven.data.local.collections

import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.milliseconds

/**
 * App-level singleton. Runs CollectionSyncer whenever the queue has work, and on request.
 * Screens never call the syncer directly.
 */
@Inject
class CollectionSyncManager(
    private val db: CollectionsDatabase,
    private val syncer: CollectionSyncer,
) {
    private companion object {
        const val DEBOUNCE_MS = 1_500L
    }

    private val dao = db.collectionDao()

    // Lives as long as the process, so a sync isn't cancelled when a screen closes.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val started = AtomicBoolean(false)
    private val requests = Channel<Unit>(Channel.CONFLATED)

    private val _lastResult = MutableStateFlow<SyncResult?>(null)

    /** Observe this to react to NeedsLogin, NeedsCloudflare and rejected adds. */
    val lastResult: StateFlow<SyncResult?> = _lastResult.asStateFlow()

    /** Safe to call more than once. */
    fun start() {
        if (!started.compareAndSet(false, true)) return

        // One sync at a time. Rapid taps collapse into a single run.
        scope.launch {
            requests.receiveAsFlow().collect {
                delay(DEBOUNCE_MS.milliseconds)
                requests.tryReceive()   // swallow requests that arrived during the wait
                _lastResult.value = syncer.sync()
            }
        }

        // Any write to a synced collection changes the pending count, which triggers a sync.
        scope.launch {
            dao.observePendingCount().collect { if (it > 0) requestSync() }
        }

        requestSync()   // app start: send leftovers and pull the site's state
    }

    /** Call after login succeeds, and when the collections screen opens. */
    fun requestSync() {
        requests.trySend(Unit)
    }
}