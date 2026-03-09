package com.sonicsignature.engine

import com.sonicsignature.model.SongMetadata
import com.sonicsignature.model.TrackTags
import com.sonicsignature.model.TuningPreference
import com.sonicsignature.model.UserSonicProfile
import com.sonicsignature.model.displayName

object PromptBuilder {

    private fun baseInstructions(
            budget: Int,
            tuningPreference: TuningPreference,
            customTuning: String
    ) =
            """
        BUDGET: Up to ₹$budget INR
        ${if (tuningPreference == TuningPreference.CUSTOM && customTuning.isNotBlank()) "TUNING PREFERENCE BIAS: $customTuning" else if (tuningPreference != TuningPreference.NONE) "TUNING PREFERENCE BIAS: ${tuningPreference.displayName}" else ""}
        
        INSTRUCTIONS:
        - Ground recommendations in Crinacle's IEM ranking database and recognized audiophile consensus.
        - For each IEM, provide: name, brand, approximate INR price (Indian Rupees), driver type, sound signature, Crinacle grade (if known).
        - NEW IN V2: You MUST provide a `compatibility_score` (0-100), `strengths` (list of 2-3 brief points), `tradeOffs` (list of 1-2 brief points), and `tonalCategory` (e.g. "Warm-Neutral").
        - Provide a 2-sentence technical justification (`justification`) explaining EXACTLY why this matches the user's sonic profile and attributes.
        - Respond ONLY with a valid JSON array — no markdown, no prose, no code fences.
        - Use exactly this schema:
          [
            {
              "name":"...",
              "brand":"...",
              "priceINR":0,
              "driverType":"DD|BA|PLANAR|HYBRID|TRIBRID",
              "soundSignature":"NEUTRAL|V_SHAPED|WARM|BRIGHT|BALANCED",
              "crinacleGrade":"A|B+|...|null",
              "justification":"...",
              "compatibilityScore": 85,
              "strengths": ["vocal clarity", "rich sub-bass"],
              "tradeOffs": ["narrow soundstage"],
              "tonalCategory": "Warm-Neutral"
            }
          ]
        - Recommend exactly 3 IEMs.
    """.trimIndent()

    fun buildSongProfilePrompt(
            songsWithTags: List<Pair<SongMetadata, TrackTags>>,
            budget: Int,
            tuningPreference: TuningPreference,
            customTuning: String,
            profile: UserSonicProfile
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
        
        SONIC PROFILE DNA:
        - Dominant Mood: ${profile.moodVector.displayName}
        - Top Genres: ${profile.dominantGenres.take(3).joinToString(", ")}
        - Bass Preference: ${profile.attributes.bass}
        - Detail Sensitivity: ${profile.attributes.detail}
        
        SONGS IN LISTENING PROFILE:
        [USER INPUT START]
        $songEntries
        [USER INPUT END]
        
        Use the tags, attributes, and artists above to infer the sonic characteristics of this listener's
        taste (energy, mood, instrumental complexity, production style) and match IEMs whose
        sound signature and technical strengths complement this type of music.
        
        ${baseInstructions(budget, tuningPreference, customTuning)}
    """.trimIndent()
    }

    fun buildArtistPrompt(
            artists: List<String>,
            budget: Int,
            tuningPreference: TuningPreference,
            customTuning: String
    ): String {
        val artistList = artists.joinToString(", ")
        return """
            You are an expert audiophile consultant specializing in In-Ear Monitors (IEMs).
            The user's music taste is defined by the following artists:
            
            [USER INPUT START]
            Artists: $artistList
            [USER INPUT END]
            
            Based on the typical sonic characteristics, genres, and production styles associated with these artists,
            recommend exactly 3 IEMs that would best suit this listener's taste profile.
            
            ${baseInstructions(budget, tuningPreference, customTuning)}
        """.trimIndent()
    }

    fun buildGenrePrompt(
            genreDescription: String,
            budget: Int,
            tuningPreference: TuningPreference,
            customTuning: String
    ): String =
            """
        You are an expert audiophile consultant specializing in In-Ear Monitors (IEMs).
        The user listens primarily to:
        
        [USER INPUT START]
        $genreDescription
        [USER INPUT END]
        
        ${baseInstructions(budget, tuningPreference, customTuning)}
    """.trimIndent()
}
