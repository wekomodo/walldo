package com.enigmaticdevs.wallhaven.util.livedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import com.enigmaticdevs.wallhaven.utils.livedata.Event

inline fun <T> LiveData<Event<T>>.observeEvent(
    owner: LifecycleOwner,
    crossinline onEventUnhandledContent: (T) -> Unit
) {
    observe(owner) { it?.getContentIfNotHandled()?.let(onEventUnhandledContent) }
}

inline fun <T> LiveData<T>.observeOnce(
    owner: LifecycleOwner,
    crossinline onChanged: (T) -> Unit
): Observer<T> {
    val wrappedObserver = object : Observer<T> {
        override fun onChanged(value: T) {
            onChanged.invoke(value)
            removeObserver(this)
        }
    }
    observe(owner, wrappedObserver)
    return wrappedObserver
}

fun <K, V> lazyMap(initializer: (K) -> V): Map<K, V> {
    val map = mutableMapOf<K, V>()
    return map.withDefault { key ->
        val newValue = initializer(key)
        map[key] = newValue
        return@withDefault newValue
    }
}
fun <T, S> LiveData<T?>.combineWith(other: LiveData<S?>): LiveData<Pair<T?, S?>> =
    MediatorLiveData<Pair<T?, S?>>().apply {
        addSource(this@combineWith) { value = Pair(it, other.value) }
        addSource(other) { value = Pair(this@combineWith.value, it) }
    }
