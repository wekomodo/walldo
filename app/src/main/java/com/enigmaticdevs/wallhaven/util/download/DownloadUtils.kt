package com.enigmaticdevs.wallhaven.util.download

import com.enigmaticdevs.wallhaven.BuildConfig

const val packageName = BuildConfig.APPLICATION_ID

const val ACTION_DOWNLOAD_COMPLETE = "$packageName.ACTION_DOWNLOAD_COMPLETE"
const val DATA_ACTION = "$packageName.DATA_ACTION"
const val DATA_URI = "$packageName.DATA_URI"
const val DOWNLOAD_STATUS = "$packageName.DOWNLOAD_STATUS"
const val STATUS_SUCCESSFUL = 1
const val STATUS_FAILED = 2
const val STATUS_CANCELLED = 3


enum class DownloadAction { DOWNLOAD, WALLPAPER }