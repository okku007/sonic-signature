package com.sonicsignature.api

import io.ktor.client.HttpClient

actual fun provideHttpClient(): HttpClient {
    // Wasm automatically falls back to Js engine in Ktor
    return HttpClient()
}
