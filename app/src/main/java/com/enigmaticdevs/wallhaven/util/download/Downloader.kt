package com.enigmaticdevs.wallhaven.util.download

interface Downloader {
    fun downloadFile(url : String) : Long
}