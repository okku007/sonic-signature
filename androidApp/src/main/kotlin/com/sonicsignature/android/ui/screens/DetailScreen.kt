package com.sonicsignature.android.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sonicsignature.model.IEMRecommendation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(recommendation: IEMRecommendation, onBack: () -> Unit) {
        val context = LocalContext.current

        Scaffold(
                topBar = {
                        TopAppBar(
                                title = { Text(recommendation.name) },
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
                        // ── Header ───────────────────────────────────────────────────────
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                                Column {
                                        Text(
                                                recommendation.name,
                                                style = MaterialTheme.typography.headlineSmall
                                        )
                                        Text(
                                                recommendation.brand,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                }
                                recommendation.crinacleGrade?.let { CrinacleGradeBadge(it) }
                        }

                        // ── Specs ────────────────────────────────────────────────────────
                        Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                        SpecRow("Price", "₹${recommendation.priceINR}")
                                        SpecRow("Driver Type", recommendation.driverType)
                                        SpecRow(
                                                "Sound Signature",
                                                recommendation.soundSignature.replace("_", " ")
                                        )
                                        recommendation.crinacleGrade?.let {
                                                val displayGrade =
                                                        if (it.equals("null", ignoreCase = true))
                                                                "Unranked"
                                                        else it
                                                SpecRow("Crinacle Grade", displayGrade)
                                        }
                                }
                        }

                        // ── Justification ────────────────────────────────────────────────
                        Text("Why this IEM?", style = MaterialTheme.typography.titleMedium)
                        Text(
                                recommendation.justification,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // ── Crinacle Link ────────────────────────────────────────────────
                        OutlinedButton(
                                onClick = {
                                        val query =
                                                "${recommendation.name} ${recommendation.brand}".replace(
                                                        " ",
                                                        "+"
                                                )
                                        val intent =
                                                Intent(
                                                        Intent.ACTION_VIEW,
                                                        Uri.parse("https://crinacle.com/?s=$query")
                                                )
                                        context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth()
                        ) {
                                Icon(
                                        Icons.AutoMirrored.Filled.OpenInNew,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("View on Crinacle")
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
                Text(value, style = MaterialTheme.typography.bodyMedium)
        }
}
