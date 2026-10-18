package com.enigmaticdevs.wallhaven.ui.util

val ratioOptions = listOf(
    "9x16 (portrait)" to "9x16",
    "10x16 (portrait)" to "10x16",
    "9x18 (portrait)" to "9x18",
    "16x9 (Wide)" to "16x9",
    "16x10 (Wide)" to "16x10",
    "21x9 (Ultra wide)" to "21x9",
    "32x9 (Ultra wide)" to "32x9",
    "48x9 (Ultra wide)" to "48x9",
    "None" to ""
)

val resolutionOptions = listOf(
    "3840x1600 (Ultra wide)" to "3840x1600",
    "3440x1440 (Ultra wide)" to "3440x1440",
    "2560x1080 (Ultra wide)" to "2560x1080",
    "3840x2400 (16:10)" to "3840x2400",
    "2560x1600 (16:10)" to "2560x1600",
    "1920x1200 (16:10)" to "1920x1200",
    "1600x1000 (16:10)" to "1600x1000",
    "1280x800 (16:10)" to "1280x800",
    "3840x2160 (16:9)" to "3840x2160",
    "2560x1440 (16:9)" to "2560x1440",
    "1920x1080 (16:9)" to "1920x1080",
    "1600x900 (16:9)" to "1600x900",
    "1280x720 (16:9)" to "1280x720",
    "3840x2880 (4:3)" to "3840x2880",
    "2560x1920 (4:3)" to "2560x1920",
    "1920x1440 (4:3)" to "1920x1440",
    "1600x1200 (4:3)" to "1600x1200",
    "1280x960 (4:3)" to "1280x960",
    "3840x3072 (5:4)" to "3840x3072",
    "2560x2048 (5:4)" to "2560x2048",
    "1920x1536 (5:4)" to "1920x1536",
    "1600x1280 (5:4)" to "1600x1280",
    "1280x1024 (5:4)" to "1280x1024",
    "None" to ""
)

val searchInfoMessage =  "• tagname (Search fuzzily for a tag) \n" +
        "• -tagname (exludes a tag/keyword)\n" +
        "• +tag1+tag2 (must have tag1 and tag2)\n" +
        "• @username (useruploads)\n" +
        "• id:123 (Exact tag search(not ImageID))\n" +
        "• like:wallpaper ID (Find wallpapers with similar tags)"