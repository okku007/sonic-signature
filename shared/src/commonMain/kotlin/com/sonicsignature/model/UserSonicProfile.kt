package com.sonicsignature.model

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

@Serializable
@Stable
data class SonicAttributes(
        val bass: Float,
        val mids: Float,
        val treble: Float,
        val warmth: Float,
        val stage: Float,
        val detail: Float
)

@Serializable
@Stable
data class UserSonicProfile(
        val aggregatedTags: Map<String, Int>,
        val attributes: SonicAttributes,
        val dominantGenres: List<String>,
        val moodVector: MoodVector,
        val lastUpdatedEpoch: Long
)
