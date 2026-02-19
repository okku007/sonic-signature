package com.sonicsignature.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AudioFeatures(
    val id: String,
    val tempo: Double,           // BPM
    val energy: Double,          // 0.0–1.0: calm → intense
    val valence: Double,         // 0.0–1.0: dark/sad → happy/euphoric
    val acousticness: Double,    // 0.0–1.0: synthetic → organic/acoustic
    val speechiness: Double,     // 0.0–1.0: instrumental → spoken word
    val danceability: Double,    // 0.0–1.0: bonus signal
    val instrumentalness: Double // 0.0–1.0: vocal → instrumental
)
