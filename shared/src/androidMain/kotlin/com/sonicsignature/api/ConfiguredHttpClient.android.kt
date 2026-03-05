package com.sonicsignature.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

actual fun provideHttpClient(): HttpClient {
    return HttpClient(OkHttp)
}
