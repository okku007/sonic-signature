package com.sonicsignature.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sonicsignature.model.IEMRecommendation
import com.sonicsignature.ui.components.SonicBadge
import com.sonicsignature.ui.components.SonicContentColumn
import com.sonicsignature.ui.components.SonicPanel
import com.sonicsignature.ui.components.SonicPrimaryButton
import com.sonicsignature.ui.components.SonicScreenHeader
import com.sonicsignature.ui.components.SonicTone

@Composable
fun DetailScreen(recommendation: IEMRecommendation, onBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val tonalLabel =
            recommendation.tonalCategory.ifBlank {
                recommendation.soundSignature.replace('_', ' ')
            }
    val gradeText = safeGrade(recommendation.crinacleGrade)

    Box(
            modifier =
                    Modifier.fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(horizontal = 24.dp, vertical = 20.dp),
            contentAlignment = Alignment.TopCenter
    ) {
        Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SonicContentColumn {
                SonicScreenHeader(
                        title = recommendation.name.uppercase(),
                        subtitle = "Detailed view for the currently selected recommendation.",
                        leading = {
                            androidx.compose.material3.IconButton(onClick = onBack) {
                                Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                )

                SonicPanel(title = recommendation.brand, badge = "Detail") {
                    Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                                text = recommendation.name,
                                style = MaterialTheme.typography.displayMedium
                        )
                        Text(
                                text = "${recommendation.brand}  •  ${recommendation.driverType}",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SonicBadge(text = gradeText, tone = gradeTone(gradeText))
                            SonicBadge(text = tonalLabel, tone = SonicTone.Accent)
                            SonicBadge(
                                    text = "₹${recommendation.priceINR}",
                                    tone = SonicTone.Neutral
                            )
                        }
                    }
                }

                if (recommendation.compatibilityScore > 0) {
                    SonicPanel(title = "Compatibility meter", badge = "Live score", emphasized = true) {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                        text = "Sonic compatibility",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                        text = "${recommendation.compatibilityScore}%",
                                        style = MaterialTheme.typography.displayLarge
                                )
                            }
                            SonicBadge(
                                    text =
                                            when {
                                                recommendation.compatibilityScore >= 85 -> "High confidence"
                                                recommendation.compatibilityScore >= 70 -> "Good alignment"
                                                else -> "Review trade-offs"
                                            },
                                    tone =
                                            when {
                                                recommendation.compatibilityScore >= 85 -> SonicTone.Success
                                                recommendation.compatibilityScore >= 70 -> SonicTone.Accent
                                                else -> SonicTone.Warning
                                            }
                            )
                        }

                        Box(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .height(10.dp)
                                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Box(
                            modifier =
                                            Modifier.fillMaxWidth(
                                                            recommendation.compatibilityScore /
                                                                    100f
                                                    )
                                                    .fillMaxHeight()
                                                    .background(
                                                            MaterialTheme.colorScheme.primaryContainer
                                                    )
                            )
                        }
                    }
                }

                SonicPanel(title = "Spec sheet", badge = "Current fields") {
                    DetailSpecRow(label = "Price", value = "₹${recommendation.priceINR}")
                    DetailSpecRow(label = "Driver type", value = recommendation.driverType)
                    DetailSpecRow(
                            label = "Sound signature",
                            value = recommendation.soundSignature.replace('_', ' ')
                    )
                    DetailSpecRow(label = "Tonal category", value = tonalLabel)
                    DetailSpecRow(label = "Crinacle grade", value = gradeText)
                }

                SonicPanel(title = "Why this matches you", badge = "Justification") {
                    Text(
                            text = recommendation.justification,
                            style = MaterialTheme.typography.bodyLarge
                    )
                }

                if (recommendation.strengths.isNotEmpty()) {
                    SonicPanel(title = "Strengths", badge = "Upside") {
                        recommendation.strengths.forEach { strength ->
                            DetailBullet(strength, tone = SonicTone.Success)
                        }
                    }
                }

                if (recommendation.tradeOffs.isNotEmpty()) {
                    SonicPanel(title = "Trade-offs", badge = "Caution") {
                        recommendation.tradeOffs.forEach { tradeOff ->
                            DetailBullet(tradeOff, tone = SonicTone.Warning)
                        }
                    }
                }

                SonicPrimaryButton(
                        text = "Open Crinacle search",
                        onClick = {
                            val query =
                                    "${recommendation.brand} ${recommendation.name}".replace(
                                            " ",
                                            "+"
                                    )
                            uriHandler.openUri("https://crinacle.com/?s=$query")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        leading = {
                            Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.size(8.dp))
                        }
                )
            }
        }
    }
}

@Composable
private fun DetailSpecRow(label: String, value: String) {
    Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun DetailBullet(text: String, tone: SonicTone) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
        Box(
                modifier =
                        Modifier.padding(top = 6.dp)
                                .size(8.dp)
                                .background(
                                        when (tone) {
                                            SonicTone.Success ->
                                                    MaterialTheme.colorScheme.primaryContainer
                                            SonicTone.Warning -> Color(0xFFC28B45)
                                            SonicTone.Danger -> MaterialTheme.colorScheme.error
                                            SonicTone.Accent -> MaterialTheme.colorScheme.primary
                                            SonicTone.Neutral ->
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                )
        )
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
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
