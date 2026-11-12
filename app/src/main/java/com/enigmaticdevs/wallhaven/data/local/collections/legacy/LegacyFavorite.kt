package com.enigmaticdevs.wallhaven.data.local.collections.legacy

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionItemEntity
import com.enigmaticdevs.wallhaven.data.repository.GlobalFlagsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** One row of the old `images` table. Every column is nullable on purpose (see reader). */
data class LegacyFavorite(
    val imageId: String?,
    val thumbs: String?,
    val imageUrl: String?,
    val purity: String?,
    val dimensionX: Int?,
    val dimensionY: Int?,
)

/**
 * Reads the old Room favorites database ("FavoriteImages", table "images") with plain
 * SQLite, so none of the old entities have to live in the new app.
 *
 * Columns are looked up by name and treated as null when missing, because a user who
 * skipped app updates may still have an older schema (e.g. without imageId/thumbs/purity).
 */
class LegacyFavoritesReader(private val context: Context) {

    private val dbFile: File get() = context.getDatabasePath(DB_NAME)

    fun exists(): Boolean = dbFile.exists()

    /** Oldest first (ascending old id). Throws if the file can't be read. */
    fun readAll(): List<LegacyFavorite> {
        val db = SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
        db.use {
            it.rawQuery("SELECT * FROM $TABLE ORDER BY id ASC", null).use { c ->
                val rows = ArrayList<LegacyFavorite>(c.count)
                while (c.moveToNext()) {
                    rows += LegacyFavorite(
                        imageId = c.stringOrNull("imageId"),
                        thumbs = c.stringOrNull("thumbs"),
                        imageUrl = c.stringOrNull("imageUrl"),
                        purity = c.stringOrNull("purity"),
                        dimensionX = c.intOrNull("dimension_x"),
                        dimensionY = c.intOrNull("dimension_y"),
                    )
                }
                return rows
            }
        }
    }

    /** Also removes the -wal / -shm / -journal side files. */
    fun delete() {
        context.deleteDatabase(DB_NAME)
    }

    private fun Cursor.stringOrNull(column: String): String? {
        val i = getColumnIndex(column)
        return if (i < 0 || isNull(i)) null else getString(i)
    }

    private fun Cursor.intOrNull(column: String): Int? {
        val i = getColumnIndex(column)
        return if (i < 0 || isNull(i)) null else getInt(i)
    }

    companion object {
        const val DB_NAME = "FavoriteImages"
        const val TABLE = "images"
    }
}

/** One-time import into the default local collection. Safe to call on every app start. */
class LegacyFavoritesMigration(
    private val reader: LegacyFavoritesReader,
    private val dao: LegacyImportDao,
    private val flags: GlobalFlagsRepository,
) {
    suspend fun runIfNeeded() {
        if (flags.isLegacyFavoritesImported()) return

        if (!reader.exists()) {
            // Fresh install, or already cleaned up. Nothing to import.
            flags.markLegacyFavoritesImported()
            return
        }

        try {
            val old = withContext(Dispatchers.IO) { reader.readAll() }

            // Touching the DAO opens the DB, which runs the onCreate callback that
            // inserts the Favorites row, so it is guaranteed to exist after this line.
            val collectionId = dao.defaultCollectionId()
                ?: error("Default collection missing")

            // Old rows are oldest-first. The list shows newest first by addedAt, so give
            // later rows later timestamps. All of them stay older than anything added now.
            val base = System.currentTimeMillis() - old.size
            val items = old.mapIndexedNotNull { index, fav ->
                fav.toItem(collectionId, addedAt = base + index)
            }

            dao.insertItems(items) // single transaction, duplicates ignored
            flags.markLegacyFavoritesImported() // mark first, delete second
            reader.delete()
        } catch (e: Exception) {
            // Leave the file alone and don't mark done, so it retries next start.
            Log.w("LegacyFavorites", "Import failed, will retry", e)
        }
    }

    private fun LegacyFavorite.toItem(collectionId: Long, addedAt: Long): CollectionItemEntity? {
        val wallpaperId = resolveWallpaperId() ?: return null // can't identify it, skip
        return CollectionItemEntity(
            wallpaperId = wallpaperId,
            collectionId = collectionId,
            thumbUrl = thumbs,
            fullUrl = imageUrl,
            dimensionsX = dimensionX,
            dimensionsY = dimensionY,
            purity = purity,
            syncedSlot = null, // local collection
            addedAt = addedAt,
        )
    }

    /** Prefer the stored id; fall back to parsing it out of the image URLs. */
    private fun LegacyFavorite.resolveWallpaperId(): String? =
        imageId?.takeIf { it.isNotBlank() }?.let(::normalizeId)
            ?: imageUrl?.let { FULL_URL_ID.find(it)?.groupValues?.get(1) }
            ?: thumbs?.let { THUMB_URL_ID.find(it)?.groupValues?.get(1) }

    // "wallhaven-abc123.jpg" / "abc123" -> "abc123"
    private fun normalizeId(raw: String): String =
        raw.trim().removePrefix("wallhaven-").substringBefore('.')

    private companion object {
        // https://w.wallhaven.cc/full/ab/wallhaven-abc123.jpg
        val FULL_URL_ID = Regex("wallhaven-([a-z0-9]+)\\.")
        // https://th.wallhaven.cc/small/ab/abc123.jpg
        val THUMB_URL_ID = Regex("/([a-z0-9]+)\\.[a-z0-9]+$")
    }
}