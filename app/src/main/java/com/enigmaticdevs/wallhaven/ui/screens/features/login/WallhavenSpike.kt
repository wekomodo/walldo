package com.enigmaticdevs.wallhaven.ui.screens.features.login

import android.util.Log
import com.enigmaticdevs.wallhaven.ui.util.WALLHAVEN_UA
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.parameters
import org.jsoup.Jsoup

class WallhavenSpike {

    private val TAG = "WallhavenSpike"
    private val http = HttpClient(OkHttp) {
        install(UserAgent) { agent = WALLHAVEN_UA }
        install(HttpCookies) { storage = WebViewCookiesStorage() }
    }


    suspend fun run(label: String) {
        //read the page for csrf token
        val page = http.get("https://wallhaven.cc/favorites")
        Log.d(
            TAG,
            "GET /favorites -> ${page.status}, cf-mitigated: ${page.headers["cf-mitigated"]}"
        )
        if (page.status != HttpStatusCode.OK) {
            Log.d(TAG, "Body start: ${page.bodyAsText().take(200)}")
            return
        }
        val token = Jsoup.parse(page.bodyAsText())
            .selectFirst("meta[name=csrf-token]")?.attr("content")
        Log.d(TAG, "Got token: ${token != null}")
        if (token == null) return

        // real test : writing a collection
        val resp = http.submitForm(
            url = "https://wallhaven.cc/collection/new",
            formParameters = parameters {
                append("_token", token)
                append("label", label)
                append("public", "on")
            }

        )
        Log.d(TAG,"POST /collection/new -> ${resp.status}, cf-mitigated: ${resp.headers["cf-mitigated"]}")
        Log.d(TAG,"Location= ${resp.headers["Location"]}")
        Log.d(TAG,"Body start: ${resp.bodyAsText().take(200)}")
    }
}
