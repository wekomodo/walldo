package com.enigmaticdevs.wallhaven.data.model

enum class Purity(val purity : String) {
    SFW("100"),
    SFW_SKETCHY("110"),
    NSFW("111")
}

enum class Category(val category : String) {
    GENERAL("100"),
    ANIME("110"),
    PEOPLE("111")
}


enum class Sorting(val sort : String) {
    DATE_ADDED("date_added"),
    RELEVANCE("relevance"),
    RANDOM("random"),
    TOP_LIST("toplist")
}
