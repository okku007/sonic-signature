package com.sonicsignature.backend.service

import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Manages Spotify access using Client Credentials flow. Single backend-wide token, auto-refreshed
 * when expired. Reads credentials from .env file (local dev) or environment variables (production).
 */
class SpotifyAuthService {

    private val dotenv = dotenv { ignoreIfMissing = true }

    private val clientId: String =
            dotenv["SPOTIFY_CLIENT_ID"]
                    ?: error("SPOTIFY_CLIENT_ID not found in .env or environment")
    private val clientSecret: String =
            dotenv["SPOTIFY_CLIENT_SECRET"]
                    ?: error("SPOTIFY_CLIENT_SECRET not found in .env or environment")

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    }
            )
        }
    }

    @Serializable
    private data class TokenResponse(
            @SerialName("access_token") val accessToken: String,
            @SerialName("expires_in") val expiresIn: Int,
            @SerialName("token_type") val tokenType: String
    )

    private var cachedToken: String? = null
    private var tokenExpiryMs: Long = 0L
    private val mutex = Mutex()

    /** Returns a valid Spotify access token, refreshing if needed. */
    suspend fun getAccessToken(): String =
            mutex.withLock {
                val nowMs = System.currentTimeMillis()

                // Return cached token if still valid (60s buffer)
                cachedToken?.let { token -> if (nowMs < tokenExpiryMs - 60_000) return token }

                val response =
                        httpClient.submitForm(
                                url = "https://accounts.spotify.com/api/token",
                                formParameters =
                                        parameters {
                                            append("grant_type", "client_credentials")
                                            append("client_id", clientId)
                                            append("client_secret", clientSecret)
                                        }
                        )

                if (!response.status.isSuccess()) {
                    throw IllegalStateException("Spotify auth failed: ${response.status}")
                }

                val tokenResponse = response.body<TokenResponse>()
                cachedToken = tokenResponse.accessToken
                tokenExpiryMs = nowMs + (tokenResponse.expiresIn * 1000L)

                return tokenResponse.accessToken
            }
}
