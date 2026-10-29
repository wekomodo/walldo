package com.enigmaticdevs.wallhaven.data.remote.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable


@Immutable
@Serializable
data class DataX(
    val aspect_ratios: List<String>,
    val categories: List<String>,
    val per_page: String,
    val purity: List<String>,
    val resolutions: List<String>,
    val tag_blacklist: List<String>,
    val thumb_size: String,
    val toplist_range: String,
    val user_blacklist: List<String>
)