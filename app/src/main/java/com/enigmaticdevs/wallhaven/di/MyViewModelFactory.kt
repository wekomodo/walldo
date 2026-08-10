package com.enigmaticdevs.wallhaven.di

import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.Provider
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import kotlin.reflect.KClass

class MyViewModelFactory(
    providers: Map<KClass<out ViewModel>, Provider<ViewModel>>,
    assistedProviders: Map<KClass<out ViewModel>, Provider<ViewModelAssistedFactory>>
) : MetroViewModelFactory() {
    override val viewModelProviders: Map<KClass<out ViewModel>, Provider<ViewModel>> = providers
    override val assistedFactoryProviders: Map<KClass<out ViewModel>, Provider<ViewModelAssistedFactory>> = assistedProviders
    override val manualAssistedFactoryProviders: Map<KClass<out ManualViewModelAssistedFactory>, Provider<ManualViewModelAssistedFactory>> = emptyMap()
}