package com.sonicsignature.backend.routes

import com.sonicsignature.backend.service.LastFmService
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

private const val LASTFM_API_BASE = "https://ws.audioscrobbler.com/2.0/"

@Serializable data class ErrorResponse(val error: String)

/**
 * Proxy routes for Last.fm API. The backend holds the API key so the client never sees it.
 *
 * GET /api/music/search?q=<query>&limit=<n> → Last.fm track.search GET
 * /api/music/tags?artist=<name>&track=<name> → Last.fm track.getTopTags
 */
fun Routing.lastFmProxyRoutes(lastFmService: LastFmService) {

    val lastFmHttpClient = HttpClient()

    route("/api/music") {
        get("/search") {
            val query = call.parameters["q"]
            if (query.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("q parameter is required"))
                return@get
            }

            val limit = call.parameters["limit"] ?: "10"

            try {
                val response =
                        lastFmHttpClient.get(LASTFM_API_BASE) {
                            parameter("method", "track.search")
                            parameter("track", query)
                            parameter("api_key", lastFmService.apiKey)
                            parameter("format", "json")
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
                        ErrorResponse("Last.fm search failed: ${e.message}")
                )
            }
        }

        get("/tags") {
            val artist = call.parameters["artist"]
            val track = call.parameters["track"]
            if (artist.isNullOrBlank() || track.isNullOrBlank()) {
                call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("artist and track parameters are required")
                )
                return@get
            }

            try {
                val response =
                        lastFmHttpClient.get(LASTFM_API_BASE) {
                            parameter("method", "track.getTopTags")
                            parameter("artist", artist)
                            parameter("track", track)
                            parameter("api_key", lastFmService.apiKey)
                            parameter("format", "json")
                        }

                call.respondText(
                        text = response.bodyAsText(),
                        contentType = ContentType.Application.Json,
                        status = response.status
                )
            } catch (e: Exception) {
                call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("Last.fm tags fetch failed: ${e.message}")
                )
            }
        }
    }
}
