package com.enigmaticdevs.wallhaven.response

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