package com.enigmaticdevs.wallhaven.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.enigmaticdevs.wallhaven.data.local.SecureStorage
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionsDatabase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn


@BindingContainer
@ContributesTo(AppScope::class)
object StorageModule {

    @SingleIn(AppScope::class)
    fun provideSecureStorage(application: Application): SecureStorage {
        return SecureStorage(application)
    }

    @SingleIn(AppScope::class)
    @Provides
    fun provideDatastore(context: Context): DataStore<Preferences> = context.dataStore

    // not used yet
    @Named("filters")
    @SingleIn(AppScope::class)
    @Provides
    fun provideFilterDataStore(context: Context): DataStore<Preferences> = context.filterDataStore

    // Provide Room Db
    @SingleIn(AppScope::class)
    @Provides
    fun provideCollectionsDatabase(context: Context): CollectionsDatabase =
        Room.databaseBuilder(context, CollectionsDatabase::class.java, "walldo_collections.db")
            .fallbackToDestructiveMigration(true)
            .addCallback(object : RoomDatabase.Callback() {
                // runs once, when the file is first created: guarantees exactly one "Favorites"
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "INSERT INTO collection (name, isSynced, createdAt, sortOrder, lastPage, syncedPages, pullGen, lastSyncedAt) " +
                                "VALUES ('Favorites', 0, ${System.currentTimeMillis()}, 0, 0, 0, 0, 0)"
                    )
                }
            })
            .build()
}