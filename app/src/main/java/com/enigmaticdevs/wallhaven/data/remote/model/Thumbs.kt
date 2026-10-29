package com.enigmaticdevs.wallhaven.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class Thumbs(
    val large: String,
    val original: String,
    val small: String
)