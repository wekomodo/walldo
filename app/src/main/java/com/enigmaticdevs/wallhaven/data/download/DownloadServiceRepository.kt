package com.enigmaticdevs.wallhaven.data.download

import okhttp3.ResponseBody
import javax.inject.Inject

class DownloadServiceRepository @Inject constructor(
    private val downloadService: DownloadService
){

    suspend fun downloadFile(url: String): ResponseBody? {

      return try {
            downloadService.downloadFile(url)
        }
       catch (e : Exception){
            null
       }
    }
}