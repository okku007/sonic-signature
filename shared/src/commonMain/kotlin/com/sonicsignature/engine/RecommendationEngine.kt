package com.sonicsignature.engine

import com.sonicsignature.api.LLMClientFactory
import com.sonicsignature.api.MusicClient
import com.sonicsignature.model.BudgetTier
import com.sonicsignature.model.IEMRecommendation
import com.sonicsignature.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Orchestrates the full IEM recommendation pipeline. Constitution §4.4: All operations run on
 * background dispatchers. Constitution §4.5: Dependencies are injected, not singletons.
 */
class RecommendationEngine(
        private val musicClient: MusicClient,
        private val llmFactory: LLMClientFactory
) {

        /**
         * Recommend IEMs based on one or more tracks. Pipeline: fetch tags for each → build
         * combined prompt → call LLM → parse response.
         */
        suspend fun recommendFromSongs(
                songs: List<com.sonicsignature.model.SongMetadata>,
                budget: BudgetTier
        ): Result<List<IEMRecommendation>> =
                withContext(Dispatchers.Default) {
                        val provider =
                                llmFactory.create()
                                        ?: return@withContext Result.Error(
                                                "Set up your LLM provider in Settings to get started."
                                        )

                        // Fetch tags for each song (best-effort — empty tags on failure)
                        val songsWithTags =
                                songs.map { song ->
                                        val tagsResult =
                                                musicClient.getTrackTags(song.artist, song.name)
                                        val tags =
                                                when (tagsResult) {
                                                        is Result.Success -> tagsResult.data
                                                        is Result.Error ->
                                                                com.sonicsignature.model.TrackTags(
                                                                        emptyList()
                                                                )
                                                        Result.Loading ->
                                                                com.sonicsignature.model.TrackTags(
                                                                        emptyList()
                                                                )
                                                }
                                        song to tags
                                }

                        val prompt = PromptBuilder.buildSongPrompt(songsWithTags, budget)

                        val llmResponse =
                                try {
                                        provider.complete(prompt)
                                } catch (e: Exception) {
                                        return@withContext Result.Error(
                                                e.message
                                                        ?: "The AI service is temporarily unavailable. Try again.",
                                                e
                                        )
                                }

                        RecommendationParser.parse(llmResponse)
                }

        /** Recommend IEMs based on a list of artist names as a taste profile. */
        suspend fun recommendFromArtists(
                artists: List<String>,
                budget: BudgetTier
        ): Result<List<IEMRecommendation>> =
                withContext(Dispatchers.Default) {
                        val provider =
                                llmFactory.create()
                                        ?: return@withContext Result.Error(
                                                "Set up your LLM provider in Settings to get started."
                                        )

                        val prompt = PromptBuilder.buildArtistPrompt(artists, budget)

                        val llmResponse =
                                try {
                                        provider.complete(prompt)
                                } catch (e: Exception) {
                                        return@withContext Result.Error(
                                                e.message
                                                        ?: "The AI service is temporarily unavailable. Try again.",
                                                e
                                        )
                                }

                        RecommendationParser.parse(llmResponse)
                }

        /** Recommend IEMs based on a free-text genre or mood description. */
        suspend fun recommendFromGenre(
                description: String,
                budget: BudgetTier
        ): Result<List<IEMRecommendation>> =
                withContext(Dispatchers.Default) {
                        val provider =
                                llmFactory.create()
                                        ?: return@withContext Result.Error(
                                                "Set up your LLM provider in Settings to get started."
                                        )

                        val prompt = PromptBuilder.buildGenrePrompt(description, budget)

                        val llmResponse =
                                try {
                                        provider.complete(prompt)
                                } catch (e: Exception) {
                                        return@withContext Result.Error(
                                                e.message
                                                        ?: "The AI service is temporarily unavailable. Try again.",
                                                e
                                        )
                                }

                        RecommendationParser.parse(llmResponse)
                }
}
