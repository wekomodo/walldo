package com.enigmaticdevs.wallhaven.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.enigmaticdevs.wallhaven.data.local.SecureStorage
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionsDatabase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "com.enigmaticdevs.wallhaven_dataStore",
    produceMigrations = { _ ->
        listOf(LegacyPreferencesMigration())
    }
)

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
            //.fallbackToDestructiveMigration(true)
            .addCallback(object : RoomDatabase.Callback() {
                // runs once, when the file is first created: guarantees exactly one "Favorites"
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "INSERT INTO collection (name, isSynced, createdAt, sortOrder, lastPage, syncedPages, pullGen, lastSyncedAt,isAutoWallpaper) " +
                                "VALUES ('Favorites', 0, ${System.currentTimeMillis()}, 0, 0, 0, 0, 0,1)"
                    )
                }
            })
            .build()
}



/** can be deleted later when bumping the version */
private class LegacyPreferencesMigration : DataMigration<Preferences> {

    // Old keys
    private val oldApiKey = stringPreferencesKey("api_key")
    private val oldPurity = stringPreferencesKey("purity")
    private val oldCategory = stringPreferencesKey("category")
    private val oldRatio = stringPreferencesKey("ratio")
    private val oldResolution = stringPreferencesKey("resolution")


    // New keys
    private val newApiKey = stringPreferencesKey("wallhaven_api_key")
    private val newPurity = stringPreferencesKey("filter_purity")
    private val newCategory = stringPreferencesKey("filter_category")
    private val newRatio = stringPreferencesKey("filter_ratio")
    private val newResolution = stringPreferencesKey("filter_resolution")

    private val oldKeys = listOf(oldApiKey, oldPurity, oldCategory, oldRatio, oldResolution)

    override suspend fun shouldMigrate(currentData: Preferences): Boolean {
        // Trigger if ANY old key (including api_key) is still in the file
        return oldKeys.any { it in currentData }
    }

    override suspend fun migrate(currentData: Preferences): Preferences {
        val mutablePrefs = currentData.toMutablePreferences()

        fun move(oldKey: Preferences.Key<String>, newKey: Preferences.Key<String>) {
            currentData[oldKey]?.let { value ->
                mutablePrefs[newKey] = value
                mutablePrefs.remove(oldKey)
            }
        }

        move(oldApiKey, newApiKey)
        move(oldPurity, newPurity)
        move(oldCategory, newCategory)
        move(oldRatio, newRatio)
        move(oldResolution, newResolution)

        return mutablePrefs.toPreferences()
    }

    override suspend fun cleanUp() = Unit
}