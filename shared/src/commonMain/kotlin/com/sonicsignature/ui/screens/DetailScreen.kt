package com.sonicsignature.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sonicsignature.model.IEMRecommendation

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(recommendation: IEMRecommendation, onBack: () -> Unit) {
        val uriHandler = LocalUriHandler.current

        Scaffold(
                topBar = {
                        TopAppBar(
                                title = {
                                        Text(
                                                "Details",
                                                style = MaterialTheme.typography.titleMedium
                                        )
                                },
                                navigationIcon = {
                                        IconButton(onClick = onBack) {
                                                Icon(
                                                        Icons.AutoMirrored.Filled.ArrowBack,
                                                        contentDescription = "Back"
                                                )
                                        }
                                }
                        )
                }
        ) { padding ->
                Column(
                        modifier =
                                Modifier.fillMaxSize()
                                        .padding(padding)
                                        .verticalScroll(rememberScrollState())
                                        .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                        // ── Hero Section ───────────────────────────────────────────────────────
                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                        Box(
                                                modifier =
                                                        Modifier.size(120.dp)
                                                                .background(
                                                                        MaterialTheme.colorScheme
                                                                                .surfaceVariant,
                                                                        RoundedCornerShape(60.dp)
                                                                ),
                                                contentAlignment = Alignment.Center
                                        ) {
                                                Text(
                                                        text =
                                                                recommendation
                                                                        .brand
                                                                        .take(1)
                                                                        .uppercase(),
                                                        style =
                                                                MaterialTheme.typography
                                                                        .displayLarge,
                                                        color = MaterialTheme.colorScheme.primary
                                                )
                                        }

                                        Spacer(Modifier.height(8.dp))

                                        Text(
                                                recommendation.name,
                                                style = MaterialTheme.typography.headlineLarge,
                                                textAlign = TextAlign.Center
                                        )
                                        Text(
                                                recommendation.brand,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Spacer(Modifier.height(8.dp))

                                        FlowRow(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                                recommendation.crinacleGrade?.let { grade ->
                                                        Surface(
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .tertiary.copy(
                                                                                alpha = 0.2f
                                                                        ),
                                                                shape = RoundedCornerShape(12.dp)
                                                        ) {
                                                                Text(
                                                                        "Grade: ${if (grade.equals("null", ignoreCase = true)) "Unranked" else grade}",
                                                                        modifier =
                                                                                Modifier.padding(
                                                                                        horizontal =
                                                                                                12.dp,
                                                                                        vertical =
                                                                                                6.dp
                                                                                ),
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .labelMedium,
                                                                        color =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .tertiary
                                                                )
                                                        }
                                                }

                                                // Tonality Badge
                                                val tonalityDisplay =
                                                        recommendation.tonalCategory.ifBlank {
                                                                recommendation.soundSignature
                                                                        .replace("_", " ")
                                                        }
                                                Surface(
                                                        color =
                                                                MaterialTheme.colorScheme.secondary
                                                                        .copy(alpha = 0.2f),
                                                        shape = RoundedCornerShape(12.dp)
                                                ) {
                                                        Text(
                                                                tonalityDisplay,
                                                                modifier =
                                                                        Modifier.padding(
                                                                                horizontal = 12.dp,
                                                                                vertical = 6.dp
                                                                        ),
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .labelMedium,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .secondary
                                                        )
                                                }
                                        }
                                }
                        }

                        // ── Compatibility Score ───────────────────────────────────────────────
                        if (recommendation.compatibilityScore > 0) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                Text(
                                                        "Sonic Compatibility",
                                                        style = MaterialTheme.typography.titleMedium
                                                )
                                                Text(
                                                        "${recommendation.compatibilityScore}%",
                                                        style = MaterialTheme.typography.titleLarge,
                                                        color =
                                                                if (recommendation
                                                                                .compatibilityScore >
                                                                                80
                                                                )
                                                                        MaterialTheme.colorScheme
                                                                                .primary
                                                                else
                                                                        MaterialTheme.colorScheme
                                                                                .tertiary,
                                                        fontWeight = FontWeight.Bold
                                                )
                                        }

                                        // Simple progress bar
                                        Box(
                                                modifier =
                                                        Modifier.fillMaxWidth()
                                                                .height(8.dp)
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(
                                                                        MaterialTheme.colorScheme
                                                                                .surfaceVariant
                                                                )
                                        ) {
                                                Box(
                                                        modifier =
                                                                Modifier.fillMaxWidth(
                                                                                recommendation
                                                                                        .compatibilityScore /
                                                                                        100f
                                                                        )
                                                                        .fillMaxHeight()
                                                                        .clip(
                                                                                RoundedCornerShape(
                                                                                        4.dp
                                                                                )
                                                                        )
                                                                        .background(
                                                                                if (recommendation
                                                                                                .compatibilityScore >
                                                                                                80
                                                                                )
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .primary
                                                                                else
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .tertiary
                                                                        )
                                                )
                                        }
                                }
                        }

                        // ── Specs ────────────────────────────────────────────────────────
                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                        SpecRow("Price", "₹${recommendation.priceINR}")
                                        SpecRow("Driver Type", recommendation.driverType)
                                }
                        }

                        // ── Why this matches you (LLM Context) ───────────────────────────
                        Text("Why this IEM?", style = MaterialTheme.typography.titleMedium)
                        Text(
                                recommendation.justification,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                        )

                        // ── Strengths & Trade-offs ───────────────────────────────────────
                        if (recommendation.strengths.isNotEmpty() ||
                                        recommendation.tradeOffs.isNotEmpty()
                        ) {
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                        if (recommendation.strengths.isNotEmpty()) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                                "Strengths",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .labelMedium,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .primary
                                                        )
                                                        Spacer(Modifier.height(8.dp))
                                                        recommendation.strengths.forEach {
                                                                Text(
                                                                        "• $it",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .bodyMedium,
                                                                        color =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .onSurfaceVariant
                                                                )
                                                                Spacer(Modifier.height(4.dp))
                                                        }
                                                }
                                        }
                                        if (recommendation.tradeOffs.isNotEmpty()) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                                "Trade-offs",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .labelMedium,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .error
                                                        )
                                                        Spacer(Modifier.height(8.dp))
                                                        recommendation.tradeOffs.forEach {
                                                                Text(
                                                                        "• $it",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .bodyMedium,
                                                                        color =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .onSurfaceVariant
                                                                )
                                                                Spacer(Modifier.height(4.dp))
                                                        }
                                                }
                                        }
                                }
                        }

                        Spacer(Modifier.height(8.dp))

                        // ── Crinacle Link ────────────────────────────────────────────────
                        Button(
                                onClick = {
                                        val query =
                                                "${recommendation.name} ${recommendation.brand}".replace(
                                                        " ",
                                                        "+"
                                                )
                                        uriHandler.openUri("https://crinacle.com/?s=$query")
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                colors =
                                        ButtonDefaults.buttonColors(
                                                containerColor =
                                                        MaterialTheme.colorScheme.surfaceVariant,
                                                contentColor = MaterialTheme.colorScheme.onSurface
                                        ),
                                shape = RoundedCornerShape(12.dp)
                        ) {
                                Icon(
                                        Icons.AutoMirrored.Filled.OpenInNew,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                        "View on Crinacle",
                                        style = MaterialTheme.typography.labelLarge
                                )
                        }
                }
        }
}

@Composable
private fun SpecRow(label: String, value: String) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                        label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                        value,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                )
        }
}
