package com.sonicsignature.api

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

// Platform-provided HttpClientEngine
expect fun provideHttpClient(): HttpClient

object ConfiguredHttpClient {
    val client =
            provideHttpClient().config {
                install(ContentNegotiation) {
                    json(
                            Json {
                                prettyPrint = true
                                isLenient = true
                                ignoreUnknownKeys = true
                            }
                    )
                }

                install(HttpRequestRetry) {
                    retryOnServerErrors(maxRetries = 3)
                    exponentialDelay()
                }
            }
}
