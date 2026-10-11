package com.enigmaticdevs.wallhaven.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Uploader(
    val avatar: Avatar = Avatar(),
    val group: String = "",
    val username: String = ""
)