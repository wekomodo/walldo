package com.enigmaticdevs.wallhaven.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides

@DependencyGraph(AppScope::class)
interface AppGraph {
    @Provides
    fun provideKtorClient() {}
}