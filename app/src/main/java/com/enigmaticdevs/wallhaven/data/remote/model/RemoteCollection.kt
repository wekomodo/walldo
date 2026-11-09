package com.enigmaticdevs.wallhaven.data.remote.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable


@Immutable
@Serializable
data class RemoteCollection(
    val count: Int,
    val id: Long,
    val label: String,
    val `public`: Int,
    val views: Int
)