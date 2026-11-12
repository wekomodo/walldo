package com.enigmaticdevs.wallhaven.di

import android.content.Context
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionsDatabase
import com.enigmaticdevs.wallhaven.data.local.collections.legacy.LegacyFavoritesMigration
import com.enigmaticdevs.wallhaven.data.local.collections.legacy.LegacyFavoritesReader
import com.enigmaticdevs.wallhaven.data.repository.GlobalFlagsRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer
@ContributesTo(AppScope::class)
object LegacyImportModule {

    @SingleIn(AppScope::class)
    @Provides
    fun provideLegacyFavoritesReader(context: Context): LegacyFavoritesReader =
        LegacyFavoritesReader(context)

    @SingleIn(AppScope::class)
    @Provides
    fun provideLegacyFavoritesMigration(
        reader: LegacyFavoritesReader,
        db: CollectionsDatabase,
        flags: GlobalFlagsRepository,
    ): LegacyFavoritesMigration =
        LegacyFavoritesMigration(reader, db.legacyImportDao(), flags)
}