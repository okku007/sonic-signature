package com.sonicsignature.model

import kotlinx.serialization.Serializable

/**
 * Tags describing a track's genre, mood, and style. Sourced from Last.fm's community-driven tagging
 * system. Examples: "alternative rock", "melancholic", "electronic", "90s".
 */
@Serializable data class TrackTags(val tags: List<String> = emptyList())
