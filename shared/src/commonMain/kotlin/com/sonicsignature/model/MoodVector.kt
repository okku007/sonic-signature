package com.sonicsignature.model

import kotlinx.serialization.Serializable

@Serializable
enum class MoodVector {
    RELAXED,
    BALANCED,
    ENERGETIC,
    DARK,
    BRIGHT
}

val MoodVector.displayName: String
    get() =
            when (this) {
                MoodVector.RELAXED -> "Relaxed & Chill"
                MoodVector.BALANCED -> "Balanced / Neutral"
                MoodVector.ENERGETIC -> "Energetic & Aggressive"
                MoodVector.DARK -> "Dark & Atmospheric"
                MoodVector.BRIGHT -> "Bright & Airy"
            }
