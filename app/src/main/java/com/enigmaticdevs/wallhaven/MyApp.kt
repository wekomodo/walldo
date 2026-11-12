package com.enigmaticdevs.wallhaven

import android.app.Application
import coil3.SingletonImageLoader
import com.enigmaticdevs.wallhaven.data.local.collections.CollectionSyncer
import com.enigmaticdevs.wallhaven.di.AppGraph
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.createGraphFactory
import kotlinx.coroutines.launch

class MyApp(): Application() {


    lateinit var appGraph: AppGraph

    override fun onCreate() {
        super.onCreate()
        appGraph = createGraphFactory<AppGraph.Factory>().create(this,this)
        // Initialize your Metro Dependency Graph

        appGraph.appScope.launch {

            /** Map Metro's ImageLoader instance to Coil's global instance */
            SingletonImageLoader.setSafe {
                appGraph.provideImageLoaderInstance()
            }

            /** sync collections with wallhaven.cc */
            appGraph.collectionSyncManager.start()

            /** legacy billing migrations */
            appGraph.legacyBillingMigration.runIfNeeded()
            appGraph.legacyFavoritesMigration.runIfNeeded()
            appGraph.billingRepository.refresh()
        }
    }

}