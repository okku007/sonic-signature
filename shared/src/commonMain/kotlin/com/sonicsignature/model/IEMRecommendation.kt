package com.sonicsignature.model

import kotlinx.serialization.Serializable

enum class DriverType {
    DD, // Dynamic Driver — natural bass, cohesive sound
    BA, // Balanced Armature — fast, precise mids/highs
    PLANAR, // Planar Magnetic — exceptional speed, linear bass
    HYBRID, // DD + BA combination
    TRIBRID // DD + BA + EST combination
}

enum class SoundSignature {
    NEUTRAL, // Linear, accurate reproduction
    V_SHAPED, // Boosted bass + treble, recessed mids
    WARM, // Emphasized lower-mids and bass
    BRIGHT, // Boosted high frequencies
    BALANCED // Slight liveliness boost, all-rounder
}

@Serializable
data class IEMRecommendation(
        val name: String,
        val brand: String,
        val priceINR: Int,
        val driverType: String, // String for LLM JSON compat; validated on parse
        val soundSignature: String, // String for LLM JSON compat; validated on parse
        val crinacleGrade: String? = null,
        val justification: String
) {
    fun driverTypeEnum(): DriverType? = runCatching { DriverType.valueOf(driverType) }.getOrNull()
    fun soundSignatureEnum(): SoundSignature? =
            runCatching { SoundSignature.valueOf(soundSignature) }.getOrNull()
}
