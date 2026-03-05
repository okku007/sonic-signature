package com.sonicsignature.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO

actual fun provideHttpClient(): HttpClient {
    return HttpClient(CIO)
}
