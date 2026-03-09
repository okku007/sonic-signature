package com.sonicsignature.model

import kotlinx.serialization.Serializable

@Serializable
data class IEMComparisonItem(
        val recommendation: IEMRecommendation,
        val compatibilityScore: Float,
        val matchingStrengths: List<String>,
        val tradeOffs: List<String>
)
