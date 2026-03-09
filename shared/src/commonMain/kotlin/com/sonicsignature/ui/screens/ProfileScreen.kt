package com.sonicsignature.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonicsignature.model.SonicAttributes
import com.sonicsignature.model.UserSonicProfile
import com.sonicsignature.model.displayName
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(profile: UserSonicProfile?, onShareProfile: () -> Unit) {
        Column(
                modifier =
                        Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                Text("Your Sonic Signature", style = MaterialTheme.typography.headlineLarge)

                if (profile == null) {
                        Box(
                                modifier = Modifier.fillMaxSize().padding(32.dp),
                                contentAlignment = Alignment.Center
                        ) {
                                Text(
                                        text =
                                                "No profile generated yet.\nSearch for music in Discover to build your DNA.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                        }
                        return
                }

                // Exportable Card
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                                Text(
                                        "SONIC DNA",
                                        style =
                                                MaterialTheme.typography.labelMedium.copy(
                                                        letterSpacing = 2.sp
                                                ),
                                        color = MaterialTheme.colorScheme.primary
                                )

                                // Radar Chart
                                Box(
                                        modifier = Modifier.size(220.dp),
                                        contentAlignment = Alignment.Center
                                ) { RadarChart(attributes = profile.attributes) }

                                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                                // High-level descriptors
                                Text(
                                        profile.moodVector.displayName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                )

                                FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                        profile.dominantGenres.take(3).forEach { genre ->
                                                Surface(
                                                        shape = RoundedCornerShape(16.dp),
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .primaryContainer,
                                                        contentColor =
                                                                MaterialTheme.colorScheme
                                                                        .onPrimaryContainer
                                                ) {
                                                        Text(
                                                                genre.uppercase(),
                                                                modifier =
                                                                        Modifier.padding(
                                                                                horizontal = 12.dp,
                                                                                vertical = 6.dp
                                                                        ),
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .labelSmall
                                                        )
                                                }
                                        }
                                }
                        }
                }

                // Tag Heatmap representation
                Text("Top Identified Tags", style = MaterialTheme.typography.titleMedium)
                FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                ) {
                        val maxCount = profile.aggregatedTags.values.maxOrNull()?.toFloat() ?: 1f
                        profile.aggregatedTags
                                .entries
                                .sortedByDescending { it.value }
                                .take(10)
                                .forEach { (tag, count) ->
                                        val intensity = (count / maxCount).coerceIn(0.2f, 1f)
                                        Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color =
                                                        MaterialTheme.colorScheme.secondary.copy(
                                                                alpha = intensity * 0.5f
                                                        ),
                                                contentColor = Color.White
                                        ) {
                                                Text(
                                                        "${tag.lowercase()} ($count)",
                                                        modifier =
                                                                Modifier.padding(
                                                                        horizontal = 8.dp,
                                                                        vertical = 4.dp
                                                                ),
                                                        style = MaterialTheme.typography.labelSmall
                                                )
                                        }
                                }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                        onClick = onShareProfile,
                ) { Text("Share Profile") }
        }
}

@Composable
fun RadarChart(attributes: SonicAttributes, modifier: Modifier = Modifier) {
        val labels = listOf("Bass", "Mids", "Treble", "Detail", "Stage", "Warmth")
        val values =
                listOf(
                        attributes.bass,
                        attributes.mids,
                        attributes.treble,
                        attributes.detail,
                        attributes.stage,
                        attributes.warmth
                )

        val primaryColor = MaterialTheme.colorScheme.primary
        val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

        Canvas(modifier = modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2 * 0.8f
                val angleStep = (2 * PI / labels.size).toFloat()

                // Draw web
                for (step in 1..5) {
                        val stepRadius = radius * (step / 5f)
                        val webPath = Path()
                        for (i in labels.indices) {
                                val angle = i * angleStep - PI / 2
                                val x = center.x + stepRadius * cos(angle).toFloat()
                                val y = center.y + stepRadius * sin(angle).toFloat()
                                if (i == 0) webPath.moveTo(x, y) else webPath.lineTo(x, y)
                        }
                        webPath.close()
                        drawPath(
                                path = webPath,
                                color = surfaceVariantColor,
                                style = Stroke(width = 1f)
                        )
                }

                // Draw axes & labels
                for (i in labels.indices) {
                        val angle = i * angleStep - PI / 2
                        val x = center.x + radius * cos(angle).toFloat()
                        val y = center.y + radius * sin(angle).toFloat()
                        drawLine(
                                color = surfaceVariantColor,
                                start = center,
                                end = Offset(x, y),
                                strokeWidth = 1f
                        )
                }

                // Draw data polygon
                val dataPath = Path()
                for (i in values.indices) {
                        val angle = i * angleStep - PI / 2
                        val valueRadius = radius * values[i].coerceIn(0f, 1f)
                        val x = center.x + valueRadius * cos(angle).toFloat()
                        val y = center.y + valueRadius * sin(angle).toFloat()

                        if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
                }
                dataPath.close()

                drawPath(path = dataPath, color = primaryColor.copy(alpha = 0.4f))
                drawPath(path = dataPath, color = primaryColor, style = Stroke(width = 3f))
        }
}
