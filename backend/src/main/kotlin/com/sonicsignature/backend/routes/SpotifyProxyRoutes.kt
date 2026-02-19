package com.sonicsignature.backend.routes

import com.sonicsignature.backend.service.SpotifyAuthService
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val SPOTIFY_API_BASE = "https://api.spotify.com/v1"

@Serializable data class ErrorResponse(val error: String)

/**
 * Proxy routes that forward Spotify API requests using the backend's access token. No client auth
 * required — the backend manages the Spotify token internally.
 *
 * GET /api/spotify/search?q=<query>&limit=<n> → proxies Spotify search GET
 * /api/spotify/audio-features/<trackId> → proxies audio features
 */
fun Routing.spotifyProxyRoutes(authService: SpotifyAuthService) {

    val spotifyHttpClient = HttpClient {
        install(ContentNegotiation) {
            json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    }
            )
        }
    }

    route("/api/spotify") {
        get("/search") {
            val query = call.parameters["q"]
            if (query.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("q parameter is required"))
                return@get
            }

            val limit = call.parameters["limit"] ?: "10"

            try {
                val token = authService.getAccessToken()
                val response =
                        spotifyHttpClient.get("$SPOTIFY_API_BASE/search") {
                            header(HttpHeaders.Authorization, "Bearer $token")
                            parameter("q", query)
                            parameter("type", "track")
                            parameter("limit", limit)
                        }

                call.respondText(
                        text = response.bodyAsText(),
                        contentType = ContentType.Application.Json,
                        status = response.status
                )
            } catch (e: Exception) {
                call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("Spotify search failed: ${e.message}")
                )
            }
        }

        get("/audio-features/{trackId}") {
            val trackId = call.parameters["trackId"]
            if (trackId.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("trackId is required"))
                return@get
            }

            try {
                val token = authService.getAccessToken()
                val response =
                        spotifyHttpClient.get("$SPOTIFY_API_BASE/audio-features/$trackId") {
                            header(HttpHeaders.Authorization, "Bearer $token")
                        }

                call.respondText(
                        text = response.bodyAsText(),
                        contentType = ContentType.Application.Json,
                        status = response.status
                )
            } catch (e: Exception) {
                call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("Audio features fetch failed: ${e.message}")
                )
            }
        }
    }
}
