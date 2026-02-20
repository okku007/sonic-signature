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

private const val LASTFM_API_BASE = "https://ws.audioscrobbler.com/2.0/"

/**
 * Music metadata client that calls the Last.fm API directly using the user's own API key. No
 * backend proxy — all requests go straight from the device to Last.fm.
 */
class MusicClient(private val httpClient: HttpClient, private val lastFmApiKey: String) {

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
     * Search for tracks matching [query]. Returns up to [limit] results. Calls the Last.fm API
     * directly with the user's API key.
     */
    suspend fun searchTracks(query: String, limit: Int = 10): Result<List<SongMetadata>> {
        if (lastFmApiKey.isBlank()) {
            return Result.Error("Set up your Last.fm API key in Settings to search songs.")
        }

        return try {
            val response =
                    httpClient.get(LASTFM_API_BASE) {
                        parameter("method", "track.search")
                        parameter("track", query)
                        parameter("api_key", lastFmApiKey)
                        parameter("format", "json")
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
        if (lastFmApiKey.isBlank()) {
            return Result.Success(TrackTags(tags = emptyList()))
        }

        return try {
            val response =
                    httpClient.get(LASTFM_API_BASE) {
                        parameter("method", "track.getTopTags")
                        parameter("artist", artist)
                        parameter("track", track)
                        parameter("api_key", lastFmApiKey)
                        parameter("format", "json")
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
