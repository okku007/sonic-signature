package com.sonicsignature.api

import com.sonicsignature.model.SongMetadata
import com.sonicsignature.model.TrackTags
import com.sonicsignature.util.Result
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Music metadata client that routes all requests through the project backend (which proxies
 * Last.fm). Constitution §3.1: Music data used as metadata only, never for ML training.
 */
class MusicClient(private val httpClient: HttpClient) {

    // ── Internal Last.fm response shapes ──────────────────────────────────────

    @Serializable private data class LastFmSearchResponse(val results: SearchResults)

    @Serializable private data class SearchResults(val trackmatches: TrackMatches)

    @Serializable private data class TrackMatches(val track: List<LastFmTrack>)

    @Serializable
    private data class LastFmTrack(
            val name: String,
            val artist: String,
            val url: String = "",
            val listeners: String = "0",
            val image: List<LastFmImage> = emptyList(),
            val mbid: String = ""
    )

    @Serializable
    private data class LastFmImage(@SerialName("#text") val url: String, val size: String = "")

    @Serializable private data class LastFmTopTagsResponse(val toptags: TopTags)

    @Serializable private data class TopTags(val tag: List<LastFmTag>)

    @Serializable private data class LastFmTag(val name: String, val count: Int = 0)

    // ── Public API ─────────────────────────────────────────────────────────────

    /**
     * Search for tracks matching [query]. Returns up to [limit] results. Calls are routed through
     * the backend proxy.
     */
    suspend fun searchTracks(query: String, limit: Int = 10): Result<List<SongMetadata>> {
        return try {
            val response =
                    httpClient.get("${BackendConfig.baseUrl}/api/music/search") {
                        parameter("q", query)
                        parameter("limit", limit)
                    }

            if (!response.status.isSuccess()) {
                return Result.Error("No tracks found. Try a different search.")
            }

            val searchResponse = response.body<LastFmSearchResponse>()
            val songs = searchResponse.results.trackmatches.track.map { it.toSongMetadata() }

            if (songs.isEmpty()) Result.Error("No tracks found. Try a different search.")
            else Result.Success(songs)
        } catch (e: Exception) {
            Result.Error("Couldn't search tracks. Check your network connection.", e)
        }
    }

    /**
     * Fetch tags for a specific track by artist name and track name. Tags describe genre, mood, and
     * style (e.g. "alternative rock", "melancholic", "90s").
     */
    suspend fun getTrackTags(artist: String, track: String): Result<TrackTags> {
        return try {
            val response =
                    httpClient.get("${BackendConfig.baseUrl}/api/music/tags") {
                        parameter("artist", artist)
                        parameter("track", track)
                    }

            if (!response.status.isSuccess()) {
                // Tags are optional — if unavailable, return empty list
                return Result.Success(TrackTags(tags = emptyList()))
            }

            val tagsResponse = response.body<LastFmTopTagsResponse>()
            val tags =
                    tagsResponse.toptags.tag.sortedByDescending { it.count }.take(10).map {
                        it.name
                    }

            Result.Success(TrackTags(tags = tags))
        } catch (e: Exception) {
            // Tags are nice-to-have, not critical — return empty on failure
            Result.Success(TrackTags(tags = emptyList()))
        }
    }

    // ── Mapping helpers ────────────────────────────────────────────────────────

    private fun LastFmTrack.toSongMetadata(): SongMetadata {
        val artUrl =
                image.firstOrNull { it.size == "large" || it.size == "extralarge" }?.url?.ifBlank {
                    null
                }
                        ?: image.lastOrNull()?.url?.ifBlank { null }

        return SongMetadata(
                id = "${artist}::${name}", // Last.fm identifies tracks by artist+name
                name = name,
                artist = artist,
                albumArtUrl = artUrl,
                genres = emptyList()
        )
    }
}
