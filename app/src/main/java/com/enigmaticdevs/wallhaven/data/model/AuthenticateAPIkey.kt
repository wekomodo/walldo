package com.enigmaticdevs.wallhaven.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthenticateAPIkey(
    @SerialName("data")
    val data: DataX
)