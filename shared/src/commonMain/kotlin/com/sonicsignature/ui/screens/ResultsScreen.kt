package com.sonicsignature.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sonicsignature.model.IEMRecommendation
import com.sonicsignature.ui.components.SonicBadge
import com.sonicsignature.ui.components.SonicContentColumn
import com.sonicsignature.ui.components.SonicLoadingState
import com.sonicsignature.ui.components.SonicPanel
import com.sonicsignature.ui.components.SonicScreenHeader
import com.sonicsignature.ui.components.SonicSecondaryButton
import com.sonicsignature.ui.components.SonicTone

@Composable
fun ResultsScreen(
        recommendations: List<IEMRecommendation>,
        isLoading: Boolean,
        error: String?,
        onBack: () -> Unit,
        onSearchAgain: () -> Unit,
        onRecommendationClick: (Int) -> Unit
) {
    Box(
            modifier =
                    Modifier.fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(horizontal = 24.dp, vertical = 20.dp),
            contentAlignment = Alignment.TopCenter
    ) {
        SonicContentColumn(modifier = Modifier.fillMaxSize()) {
            SonicScreenHeader(
                    title = "RECOMMENDED SET",
                    subtitle =
                            "Structured recommendation output from the current Sonic Signature engine.",
                    leading = {
                        androidx.compose.material3.IconButton(onClick = onBack) {
                            Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    trailing = {
                        SonicSecondaryButton(
                                text = "Refresh",
                                onClick = onSearchAgain,
                                leading = {
                                    Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                }
                        )
                    }
            )

            when {
                isLoading -> {
                    Box(
                            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                            contentAlignment = Alignment.TopCenter
                    ) {
                        SonicLoadingState(modifier = Modifier.fillMaxWidth())
                    }
                }
                error != null -> {
                    SonicPanel(title = "Run failed", badge = "Retry", emphasized = true) {
                        Text(
                                text = error,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                        )
                        SonicSecondaryButton(
                                text = "Search again",
                                onClick = onSearchAgain,
                                modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                recommendations.isEmpty() -> {
                    SonicPanel(title = "No output", badge = "Idle") {
                        Text(
                                text =
                                        "No recommendations are loaded yet. Return to Discover and initiate a run.",
                                style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                else -> {
                    LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(recommendations) { index, recommendation ->
                            RecommendationResultCard(
                                    recommendation = recommendation,
                                    onOpen = { onRecommendationClick(index) }
                            )
                        }
                        item {
                            SonicSecondaryButton(
                                    text = "Search again",
                                    onClick = onSearchAgain,
                                    modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            Text(
                                    text =
                                            "Price, grade, and justification are model-derived summaries. Verify current listings before purchase.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendationResultCard(recommendation: IEMRecommendation, onOpen: () -> Unit) {
    val gradeText = safeGrade(recommendation.crinacleGrade)
    val scoreTone =
            when {
                recommendation.compatibilityScore >= 85 -> SonicTone.Success
                recommendation.compatibilityScore >= 70 -> SonicTone.Accent
                recommendation.compatibilityScore > 0 -> SonicTone.Warning
                else -> SonicTone.Neutral
            }

    SonicPanel(title = recommendation.brand, badge = recommendationCardLabel(recommendation), onClick = onOpen) {
        Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
            ) {
                Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                            text = recommendation.name,
                            style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                            text = "${recommendation.brand}  •  ${recommendation.driverType}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SonicBadge(
                            text =
                                    if (recommendation.compatibilityScore > 0) {
                                        "Match ${recommendation.compatibilityScore}%"
                                    } else {
                                        "Match pending"
                                    },
                            tone = scoreTone
                    )
                    SonicBadge(text = gradeText, tone = gradeTone(gradeText))
                }
            }

            Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                SonicBadge(text = "₹${recommendation.priceINR}", tone = SonicTone.Neutral)
                SonicBadge(
                        text = recommendation.soundSignature.replace('_', ' '),
                        tone = SonicTone.Accent
                )
                SonicBadge(text = recommendation.driverType, tone = SonicTone.Neutral)
            }

            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                MetricColumn(label = "Driver", value = recommendation.driverType)
                MetricColumn(
                        label = "Signature",
                        value = recommendation.soundSignature.replace('_', ' ')
                )
                MetricColumn(label = "Price", value = "₹${recommendation.priceINR}")
            }

            Text(
                    text = recommendation.justification,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
            )

            SonicSecondaryButton(
                    text = "Inspect specs",
                    onClick = onOpen,
                    modifier = Modifier.fillMaxWidth(),
                    leading = {
                        Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                    }
            )
        }
    }
}

@Composable
private fun MetricColumn(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
        )
    }
}

private fun safeGrade(grade: String?): String =
        if (grade.isNullOrBlank() || grade.equals("null", ignoreCase = true)) {
            "Unranked"
        } else {
            grade
        }

private fun gradeTone(grade: String): SonicTone =
        when {
            grade.startsWith("A") -> SonicTone.Success
            grade.startsWith("B") -> SonicTone.Accent
            grade.startsWith("C") -> SonicTone.Warning
            grade == "Unranked" -> SonicTone.Neutral
            else -> SonicTone.Danger
        }

private fun recommendationCardLabel(recommendation: IEMRecommendation): String =
        buildString {
            append("₹")
            append(recommendation.priceINR)
            if (recommendation.compatibilityScore > 0) {
                append(" // ")
                append(recommendation.compatibilityScore)
                append("%")
            }
        }
