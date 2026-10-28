/*
package com.enigmaticdevs.wallhaven.dbimporter

import android.content.Context
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import dev.zacsweers.metro.Inject

private const val FAVORITES_DB = "FavoriteImages"      // old database file name
private const val FAVORITES_TABLE = "images"   // value of Constant.FavoriteTableName

private data class OldFavorite(
    val imageId: String?, val thumbs: String?, val imageUrl: String?,
    val purity: String?, val x: Int?, val y: Int?
)

@Inject
class LegacyFavoritesImporter(
    private val context: Context,
    private val db: CollectionsDatabase
) {
    suspend fun import() {
        val rows = readOldRows() ?: return
        if (rows.isEmpty()) return

        db.withTransaction {
            val collectionId = db.collectionDao().getDefaultId()
                ?: db.collectionDao().insert(CollectionEntity(name = "Favorites", isDefault = true))

            var addedAt = System.currentTimeMillis() - rows.size   // keeps the old order
            for (old in rows) {
                val wallpaper = old.toWallpaper() ?: continue
                db.wallpaperDao().insertIgnore(wallpaper)
                db.itemDao().insertIgnore(CollectionItem(collectionId, wallpaper.wallpaperId, addedAt++))
            }
        }
    }

    private fun readOldRows(): List<OldFavorite>? = context.readLegacyDb(FAVORITES_DB) { old ->
        old.rawQuery(
            "SELECT imageId, thumbs, imageUrl, purity, dimension_x, dimension_y " +
                    "FROM $FAVORITES_TABLE ORDER BY id ASC",
            null
        ).use { c ->
            buildList {
                while (c.moveToNext()) add(
                    OldFavorite(
                        c.getStringOrNull(0), c.getStringOrNull(1), c.getStringOrNull(2),
                        c.getStringOrNull(3), c.getIntOrNull(4), c.getIntOrNull(5)
                    )
                )
            }
        }
    }

    private fun OldFavorite.toWallpaper(): WallpaperEntity? {
        // imageId is normally set; the URL is only a fallback
        val id = imageId?.takeIf { it.isNotBlank() }
            ?: (imageUrl ?: thumbs)?.substringAfterLast('/')?.substringBeforeLast('.')
                ?.removePrefix("wallhaven-")?.takeIf { it.isNotBlank() }
            ?: return null

        return WallpaperEntity(
            wallpaperId = id,
            thumbUrl = thumbs ?: "https://th.wallhaven.cc/lg/${id.take(2)}/$id.jpg",
            fullUrl = imageUrl,
            resolution = if (x != null && y != null) "${x}x$y" else null,
            purity = purity
        )
    }
}*/
