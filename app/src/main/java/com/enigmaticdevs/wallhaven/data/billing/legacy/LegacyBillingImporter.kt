package com.enigmaticdevs.wallhaven.data.billing.legacy

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.enigmaticdevs.wallhaven.data.billing.ProEntitlementStore
import dev.zacsweers.metro.Inject
import java.io.File

/**
 * Reads the old Room billing database ("purchase_db") with plain SQLite and
 * returns whether the user owned Walldo Pro. Only the `walldo_pro` table is
 * read. The donation table and everything else is ignored on purpose.
 *
 * Plain SQLite instead of Room means:
 *  - no old entities or Room migrations have to live in the new app
 *  - it works for both schema v1 and v2 (walldo_pro is identical in both)
 */
@Inject
class LegacyBillingReader(private val context: Context) {

    private val dbFile: File get() = context.getDatabasePath(LEGACY_DB_NAME)

    fun exists(): Boolean = dbFile.exists()

    /** true/false = what the old DB said. Throws if the file can't be read. */
    fun readWalldoPro(): Boolean {
        try {
            val db = SQLiteDatabase.openDatabase(
                dbFile.path, null, SQLiteDatabase.OPEN_READONLY
            )
            db.use {
                it.rawQuery(
                    "SELECT COUNT(*) FROM walldo_pro WHERE entitled != 0", null
                ).use { cursor ->
                    return cursor.moveToFirst() && cursor.getInt(0) > 0
                }
            }
        }
        catch (e : Exception){
            Log.e("LegacyProStatus","ReadFailed ${e.localizedMessage}")
        }
        return false

    }

    /** Also removes the -wal / -shm / -journal side files. */
    fun delete() {
        context.deleteDatabase(LEGACY_DB_NAME)
    }

    companion object {
        const val LEGACY_DB_NAME = "purchase_db"
    }
}

/** One-time import. Safe to call on every app start. */
class LegacyBillingMigration(
    private val reader: LegacyBillingReader,
    private val store: ProEntitlementStore,
) {
    suspend fun runIfNeeded() {
        if (store.isLegacyBillingImported()) return

        if (!reader.exists()) {
            // Fresh install, or already cleaned up. Nothing to import.
            store.markLegacyBillingImported()
            return
        }

        try {
            if (reader.readWalldoPro()) store.setPro(true)
            store.markLegacyBillingImported() // mark first, delete second
            reader.delete()
        } catch (e: Exception) {
            // Leave the file alone and don't mark done, so it retries next start.
            Log.w("LegacyBilling", "Import failed, will retry", e)
        }
    }
}