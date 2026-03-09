package com.sonicsignature.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

actual fun provideHttpClient(): HttpClient {
    return HttpClient(Darwin)
}
