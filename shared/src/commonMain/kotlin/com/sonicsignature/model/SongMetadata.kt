package com.sonicsignature.model

import kotlinx.serialization.Serializable

@Serializable
data class SongMetadata(
    val id: String,
    val name: String,
    val artist: String,
    val albumArtUrl: String? = null,
    val genres: List<String> = emptyList()
)
