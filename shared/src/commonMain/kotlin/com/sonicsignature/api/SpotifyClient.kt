package com.sonicsignature.api

import com.sonicsignature.model.AudioFeatures
import com.sonicsignature.model.SongMetadata
import com.sonicsignature.util.Result
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

/**
 * Spotify API client that routes all requests through the project backend. The backend holds the
 * Spotify credentials and handles authentication internally. Constitution §3.1: Spotify data used
 * as metadata only, never for ML training.
 */
class SpotifyClient(private val httpClient: HttpClient) {

    // ── Internal Spotify response shapes ──────────────────────────────────────

    @Serializable private data class SpotifySearchResponse(val tracks: SpotifyTrackPage)

    @Serializable private data class SpotifyTrackPage(val items: List<SpotifyTrack>)

    @Serializable
    private data class SpotifyTrack(
            val id: String,
            val name: String,
            val artists: List<SpotifyArtist>,
            val album: SpotifyAlbum
    )

    @Serializable private data class SpotifyArtist(val id: String, val name: String)

    @Serializable
    private data class SpotifyAlbum(val name: String, val images: List<SpotifyImage> = emptyList())

    @Serializable private data class SpotifyImage(val url: String, val width: Int, val height: Int)

    @Serializable
    private data class SpotifyAudioFeatures(
            val id: String,
            val tempo: Double,
            val energy: Double,
            val valence: Double,
            val acousticness: Double,
            val speechiness: Double,
            val danceability: Double,
            val instrumentalness: Double
    )

    // ── Public API ─────────────────────────────────────────────────────────────

    /**
     * Search for tracks matching [query]. Returns up to [limit] results. Calls are routed through
     * the backend proxy.
     */
    suspend fun searchTracks(query: String, limit: Int = 10): Result<List<SongMetadata>> {
        return try {
            val response =
                    httpClient.get("${BackendConfig.baseUrl}/api/spotify/search") {
                        parameter("q", query)
                        parameter("limit", limit)
                    }

            if (!response.status.isSuccess()) {
                return Result.Error("No tracks found. Try a different search.")
            }

            val searchResponse = response.body<SpotifySearchResponse>()
            val songs = searchResponse.tracks.items.map { it.toSongMetadata() }

            if (songs.isEmpty()) Result.Error("No tracks found. Try a different search.")
            else Result.Success(songs)
        } catch (e: Exception) {
            Result.Error("Couldn't search tracks. Check your network connection.", e)
        }
    }

    /**
     * Fetch audio features for a specific track by [trackId]. Calls are routed through the backend
     * proxy.
     */
    suspend fun getAudioFeatures(trackId: String): Result<AudioFeatures> {
        return try {
            val response =
                    httpClient.get("${BackendConfig.baseUrl}/api/spotify/audio-features/$trackId")

            if (!response.status.isSuccess()) {
                return Result.Error("Couldn't retrieve audio features for this track.")
            }

            val features = response.body<SpotifyAudioFeatures>()
            Result.Success(features.toAudioFeatures())
        } catch (e: Exception) {
            Result.Error("Couldn't retrieve audio features for this track.", e)
        }
    }

    // ── Mapping helpers ────────────────────────────────────────────────────────

    private fun SpotifyTrack.toSongMetadata() =
            SongMetadata(
                    id = id,
                    name = name,
                    artist = artists.firstOrNull()?.name ?: "Unknown Artist",
                    albumArtUrl = album.images.firstOrNull()?.url,
                    genres = emptyList()
            )

    private fun SpotifyAudioFeatures.toAudioFeatures() =
            AudioFeatures(
                    id = id,
                    tempo = tempo,
                    energy = energy,
                    valence = valence,
                    acousticness = acousticness,
                    speechiness = speechiness,
                    danceability = danceability,
                    instrumentalness = instrumentalness
            )
}
