package com.enigmaticdevs.wallhaven.di.flag

import android.os.SystemClock
import com.enigmaticdevs.wallhaven.data.remote.ApiKeyProvider
import dev.zacsweers.metro.Inject
import io.ktor.client.call.HttpClientCall
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.util.AttributeKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration.Companion.milliseconds


val SkipSavedApiKey = AttributeKey<Boolean>("SkipSavedApiKey")

fun savedApiKeyPlugin(provider: ApiKeyProvider) = createClientPlugin("SavedApiKey") {
    onRequest { request, _ ->
        if (!request.attributes.contains(SkipSavedApiKey)) {
            provider.currentKey?.let { request.header("X-API-Key", it) }
        }
    }
}


// RateLimiter.kt
@Inject
class RateLimiter(
    private val minIntervalMs: Long = 1_400,
    private val backgroundIntervalMs: Long = 2_000
) {
    private val mutex = Mutex()
    private var next = 0L
    @Volatile private var blockedUntil = 0L

    suspend fun acquire(background: Boolean = false) = mutex.withLock {
        while (true) {
            val wait = maxOf(next, blockedUntil) - SystemClock.elapsedRealtime()
            if (wait <= 0) break
            delay(wait.milliseconds)   // re-check, so a cooldown set while waiting is honored
        }
        next = SystemClock.elapsedRealtime() + if (background) backgroundIntervalMs else minIntervalMs
    }

    fun cooldown(ms: Long) {
        blockedUntil = maxOf(blockedUntil, SystemClock.elapsedRealtime() + ms)
    }
}

val BackgroundRequest = AttributeKey<Boolean>("BackgroundRequest")

class RateLimitConfig {
    lateinit var limiter: RateLimiter
    var maxRetries = 3
}

val RateLimit = createClientPlugin("RateLimit", ::RateLimitConfig) {
    val limiter = pluginConfig.limiter
    val maxRetries = pluginConfig.maxRetries

    fun cooldownMs(response: HttpResponse, attempt: Int): Long =
        response.headers[HttpHeaders.RetryAfter]?.toLongOrNull()?.times(1_000)
            ?: (30_000L * (attempt + 1))   // 30s, 60s, 90s when there's no header

    on(Send) { request ->
        val background = request.attributes.getOrNull(BackgroundRequest) == true
        var attempt = 0
        var call: HttpClientCall
        do {
            limiter.acquire(background)
            call = try {
                proceed(request)
            } catch (e: ResponseException) {
                if (e.response.status.value != 429 || attempt >= maxRetries) throw e
                limiter.cooldown(cooldownMs(e.response, attempt))
                attempt++
                continue
            }
            val status = call.response.status.value
            if (status != 429 || attempt >= maxRetries) break
            limiter.cooldown(cooldownMs(call.response, attempt))
            attempt++
        } while (true)
        call
    }
}