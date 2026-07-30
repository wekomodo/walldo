package com.enigmaticdevs.wallhaven

import android.app.Application
import com.enigmaticdevs.wallhaven.di.AppGraph
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.createGraphFactory

class MyApp(): Application() {


    lateinit var appGraph: AppGraph

    override fun onCreate() {
        super.onCreate()
        appGraph = createGraphFactory<AppGraph.Factory>().create(this)
    }

}