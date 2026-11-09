package com.enigmaticdevs.wallhaven.data.remote.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable


@Immutable
@Serializable
data class RemoteCollections(
    val data : List<RemoteCollection>
)