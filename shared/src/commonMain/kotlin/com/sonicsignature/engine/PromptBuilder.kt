package com.sonicsignature.engine

import com.sonicsignature.model.BudgetTier
import com.sonicsignature.model.SongMetadata
import com.sonicsignature.model.TrackTags

/**
 * Builds structured LLM prompts for IEM recommendations. Supports three input modes: song-based,
 * artist-based, and genre/mood-based. Constitution §5.2: User input is wrapped in delimiters to
 * mitigate prompt injection.
 */
object PromptBuilder {

    private fun baseInstructions(budget: BudgetTier) =
            """
        BUDGET: ${budget.label} (${budget.range})
        
        INSTRUCTIONS:
        - Ground recommendations in Crinacle's IEM ranking database
        - For each IEM, provide: name, brand, approximate INR price (Indian Rupees), driver type, sound signature, Crinacle grade (if known), and a 2-sentence technical justification
        - Respond ONLY with a valid JSON array — no markdown, no prose, no code fences
        - Use exactly this schema:
          [{"name":"...","brand":"...","priceINR":0,"driverType":"DD|BA|PLANAR|HYBRID|TRIBRID","soundSignature":"NEUTRAL|V_SHAPED|WARM|BRIGHT|BALANCED","crinacleGrade":"A|B+|...|null","justification":"..."}]
        - Recommend exactly 3 IEMs
    """.trimIndent()

    /** Builds a prompt from one or more songs with their Last.fm tags. */
    fun buildSongPrompt(
            songsWithTags: List<Pair<SongMetadata, TrackTags>>,
            budget: BudgetTier
    ): String {
        val songEntries =
                songsWithTags
                        .mapIndexed { i, (song, tags) ->
                            val tagLine =
                                    if (tags.tags.isNotEmpty()) tags.tags.joinToString(", ")
                                    else "unknown"
                            "${i + 1}. ${song.name} by ${song.artist} [Tags: $tagLine]"
                        }
                        .joinToString("\n")

        return """
        You are an expert audiophile consultant specializing in In-Ear Monitors (IEMs).
        Analyze the following music taste profile and recommend exactly 3 IEMs.
        
        SONGS IN LISTENING PROFILE:
        [USER INPUT START]
        $songEntries
        [USER INPUT END]
        
        Use the tags and artists above to infer the sonic characteristics of this listener's
        taste (energy, mood, instrumental complexity, production style) and match IEMs whose
        sound signature and technical strengths complement this type of music.
        
        ${baseInstructions(budget)}
    """.trimIndent()
    }

    /** Builds a prompt from one or more artist names as a taste profile. */
    fun buildArtistPrompt(artists: List<String>, budget: BudgetTier): String {
        val artistList = artists.joinToString(", ")
        return """
            You are an expert audiophile consultant specializing in In-Ear Monitors (IEMs).
            The user's music taste is defined by the following artists:
            
            [USER INPUT START]
            Artists: $artistList
            [USER INPUT END]
            
            Based on the typical sonic characteristics, genres, and production styles associated with these artists,
            recommend exactly 3 IEMs that would best suit this listener's taste profile.
            
            ${baseInstructions(budget)}
        """.trimIndent()
    }

    /** Builds a prompt from a free-text genre or mood description. */
    fun buildGenrePrompt(genreDescription: String, budget: BudgetTier): String =
            """
        You are an expert audiophile consultant specializing in In-Ear Monitors (IEMs).
        The user listens primarily to:
        
        [USER INPUT START]
        $genreDescription
        [USER INPUT END]
        
        ${baseInstructions(budget)}
    """.trimIndent()
}
