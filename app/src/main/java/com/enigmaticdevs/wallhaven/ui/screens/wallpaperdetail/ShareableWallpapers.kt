package com.enigmaticdevs.wallhaven.ui.screens.wallpaperdetail

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import coil3.annotation.InternalCoilApi
import coil3.util.MimeTypeMap
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File



@Inject
@SingleIn(AppScope::class)
class ShareableWallpapers(
    private val context : Context,
    @Named("images") private val client : HttpClient
    ) {

    private val dir get() = File(context.cacheDir, "shared_wallpapers").apply { mkdirs() }

    suspend fun uriFor(id: String, url : String): Uri = withContext(Dispatchers.IO) {
        val file = File(dir,"$id.${extensionOf(url)}")
        if (!file.exists() || file.length() == 0L) {
            val response = client.get(url)
            check(response.status.isSuccess()) { "HTTP ${response.status}" }
            file.writeBytes(response.body<ByteArray>())
        }
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    // "https://w.wallhaven.cc/full/yq/wallhaven-yqg6r7.jpg" -> "jpg"
    // Anything that doesn't look like an extension falls back to "jpg"
    private fun extensionOf(url: String): String =
        url.substringBefore('?')
            .substringAfterLast('.', "")
            .lowercase()
            .takeIf { it.matches(Regex("[a-z0-9]{2,5}")) }
            ?: "jpg"
}