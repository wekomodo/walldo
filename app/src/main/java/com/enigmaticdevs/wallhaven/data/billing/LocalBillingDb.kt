package com.enigmaticdevs.wallhaven.data.billing


import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteTable
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import com.enigmaticdevs.wallhaven.data.billing.models.Donation
import com.enigmaticdevs.wallhaven.data.billing.models.WalldoPro

@Database(
        entities = [
            Donation::class,
            WalldoPro::class
        ],
        version = 2,
    autoMigrations = [
            AutoMigration (from = 1, to = 2, spec = LocalBillingDb.BillingV5AutoMigration::class)
        ]
)


abstract class LocalBillingDb : RoomDatabase() {

    abstract fun entitlementsDao(): EntitlementsDao
    @DeleteTable(tableName = "AugmentedSkuDetails")
    @DeleteTable(tableName = "purchase_table")
    class BillingV5AutoMigration : AutoMigrationSpec
    companion object {
        @Volatile
        private var INSTANCE: LocalBillingDb? = null
        private const val DATABASE_NAME = "purchase_db"

        fun getInstance(context: Context): LocalBillingDb =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: buildDatabase(context.applicationContext).also {
                        INSTANCE = it
                    }
                }

        private fun buildDatabase(appContext: Context): LocalBillingDb {
            return Room.databaseBuilder(appContext, LocalBillingDb::class.java, DATABASE_NAME)
                    .fallbackToDestructiveMigration() // Data is cache, so it is OK to delete
                    .build()
        }
    }
}
