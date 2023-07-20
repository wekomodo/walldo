package com.enigmaticdevs.wallhaven.util

import kotlinx.coroutines.CoroutineDispatcher

interface DispatcherProvider {
    val main : CoroutineDispatcher
    val io : CoroutineDispatcher
    val default : CoroutineDispatcher
    val unconfined : CoroutineDispatcher
}
