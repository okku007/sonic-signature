package com.sonicsignature.engine

import com.sonicsignature.model.MoodVector
import com.sonicsignature.model.SonicAttributes
import com.sonicsignature.model.UserSonicProfile
import kotlin.time.Clock

object ProfileAggregator {

        // Tag heuristics dictionary: mapping common Last.fm tags to our 6 axes
        private val tagWeights =
                mapOf(
                        "bass" to
                                SonicAttributes(
                                        bass = 0.8f,
                                        mids = 0f,
                                        treble = 0f,
                                        warmth = 0.4f,
                                        stage = 0f,
                                        detail = 0f
                                ),
                        "electronic" to
                                SonicAttributes(
                                        bass = 0.7f,
                                        mids = 0.2f,
                                        treble = 0.6f,
                                        warmth = 0.1f,
                                        stage = 0.3f,
                                        detail = 0.5f
                                ),
                        "acoustic" to
                                SonicAttributes(
                                        bass = 0.1f,
                                        mids = 0.8f,
                                        treble = 0.4f,
                                        warmth = 0.7f,
                                        stage = 0.5f,
                                        detail = 0.6f
                                ),
                        "classical" to
                                SonicAttributes(
                                        bass = 0.1f,
                                        mids = 0.5f,
                                        treble = 0.8f,
                                        warmth = 0.2f,
                                        stage = 0.9f,
                                        detail = 0.9f
                                ),
                        "metal" to
                                SonicAttributes(
                                        bass = 0.6f,
                                        mids = 0.4f,
                                        treble = 0.7f,
                                        warmth = 0.1f,
                                        stage = 0.4f,
                                        detail = 0.6f
                                ),
                        "jazz" to
                                SonicAttributes(
                                        bass = 0.4f,
                                        mids = 0.7f,
                                        treble = 0.6f,
                                        warmth = 0.8f,
                                        stage = 0.6f,
                                        detail = 0.7f
                                ),
                        "vocal" to
                                SonicAttributes(
                                        bass = 0.1f,
                                        mids = 0.9f,
                                        treble = 0.3f,
                                        warmth = 0.6f,
                                        stage = 0.4f,
                                        detail = 0.5f
                                ),
                        "ambient" to
                                SonicAttributes(
                                        bass = 0.3f,
                                        mids = 0.2f,
                                        treble = 0.4f,
                                        warmth = 0.5f,
                                        stage = 0.9f,
                                        detail = 0.4f
                                )
                )

        fun aggregateProfile(tags: Map<String, Int>, genres: List<String>): UserSonicProfile {
                if (tags.isEmpty()) {
                        return UserSonicProfile(
                                aggregatedTags = emptyMap(),
                                attributes = SonicAttributes(0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f),
                                dominantGenres = genres,
                                moodVector = MoodVector.BALANCED,
                                lastUpdatedEpoch = Clock.System.now().toEpochMilliseconds()
                        )
                }

                var totalBass = 0f
                var totalMids = 0f
                var totalTreble = 0f
                var totalWarmth = 0f
                var totalStage = 0f
                var totalDetail = 0f
                var weightSum = 0f

                tags.forEach { (tag, count) ->
                        val weight = tagWeights[tag.lowercase()]
                        if (weight != null) {
                                totalBass += weight.bass * count
                                totalMids += weight.mids * count
                                totalTreble += weight.treble * count
                                totalWarmth += weight.warmth * count
                                totalStage += weight.stage * count
                                totalDetail += weight.detail * count
                                weightSum += count
                        }
                }

                // Normalize if we found weighted tags, otherwise default to 0.5 (neutral)
                val attributes =
                        if (weightSum > 0) {
                                SonicAttributes(
                                        bass = (totalBass / weightSum).coerceIn(0f, 1f),
                                        mids = (totalMids / weightSum).coerceIn(0f, 1f),
                                        treble = (totalTreble / weightSum).coerceIn(0f, 1f),
                                        warmth = (totalWarmth / weightSum).coerceIn(0f, 1f),
                                        stage = (totalStage / weightSum).coerceIn(0f, 1f),
                                        detail = (totalDetail / weightSum).coerceIn(0f, 1f)
                                )
                        } else {
                                SonicAttributes(0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f)
                        }

                // Simple heuristic for mood
                val mood =
                        when {
                                attributes.warmth > 0.6f && attributes.bass > 0.6f ->
                                        MoodVector.DARK
                                attributes.treble > 0.7f && attributes.detail > 0.7f ->
                                        MoodVector.BRIGHT
                                attributes.warmth > 0.7f && attributes.stage > 0.6f ->
                                        MoodVector.RELAXED
                                attributes.bass > 0.6f && attributes.treble > 0.6f ->
                                        MoodVector.ENERGETIC
                                else -> MoodVector.BALANCED
                        }

                return UserSonicProfile(
                        aggregatedTags = tags,
                        attributes = attributes,
                        dominantGenres = genres,
                        moodVector = mood,
                        lastUpdatedEpoch = Clock.System.now().toEpochMilliseconds()
                )
        }
}
