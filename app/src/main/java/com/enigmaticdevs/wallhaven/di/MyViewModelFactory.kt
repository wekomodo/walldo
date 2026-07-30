package com.enigmaticdevs.wallhaven.di

import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.Provider
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import kotlin.reflect.KClass

class MyViewModelFactory(
    providers: Map<KClass<out ViewModel>, Provider<ViewModel>>
) : MetroViewModelFactory()