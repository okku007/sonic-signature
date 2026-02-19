package com.sonicsignature.android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sonicsignature.android.ui.theme.GradeA
import com.sonicsignature.android.ui.theme.GradeB
import com.sonicsignature.android.ui.theme.GradeC
import com.sonicsignature.android.ui.theme.GradeD
import com.sonicsignature.model.IEMRecommendation
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
        recommendations: List<IEMRecommendation>,
        isLoading: Boolean,
        error: String?,
        onBack: () -> Unit,
        onSearchAgain: () -> Unit,
        onRecommendationClick: (Int) -> Unit
) {
    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Your IEM Matches") },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = onSearchAgain) {
                                Icon(Icons.Default.Refresh, contentDescription = "Search again")
                            }
                        }
                )
            }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading -> {
                    Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))

                        // Cycle through loading messages
                        var loadingPhase by remember { mutableStateOf(0) }
                        val loadingMessages =
                                listOf(
                                        "Analyzing audio profile…",
                                        "Consulting the audiophile oracle…",
                                        "Filtering by your budget…",
                                        "Scanning 1,000+ driver configurations…",
                                        "Evaluating sound signatures…",
                                        "Checking Crinacle's ranking database…",
                                        "Synthesizing technical reviews…",
                                        "Calibrating final matches…",
                                        "Finding your perfect sonic match…",
                                        "Almost there…"
                                )

                        LaunchedEffect(Unit) {
                            while (true) {
                                delay(2500)
                                if (loadingPhase < loadingMessages.lastIndex) {
                                    loadingPhase++
                                }
                            }
                        }

                        Text(
                                loadingMessages[loadingPhase],
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                error != null -> {
                    Column(
                            modifier = Modifier.align(Alignment.Center).padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                                error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(onClick = onSearchAgain) { Text("Try Again") }
                    }
                }
                recommendations.isEmpty() -> {
                    Text(
                            "No recommendations yet. Go back and search!",
                            modifier = Modifier.align(Alignment.Center).padding(32.dp),
                            style = MaterialTheme.typography.bodyMedium
                    )
                }
                else -> {
                    LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(recommendations) { index, rec ->
                            IEMRecommendationCard(
                                    recommendation = rec,
                                    onClick = { onRecommendationClick(index) }
                            )
                        }
                        item {
                            OutlinedButton(
                                    onClick = onSearchAgain,
                                    modifier = Modifier.fillMaxWidth()
                            ) { Text("Search Again") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IEMRecommendationCard(recommendation: IEMRecommendation, onClick: () -> Unit) {
    Card(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(recommendation.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                            recommendation.brand,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                recommendation.crinacleGrade?.let { grade -> CrinacleGradeBadge(grade) }
            }

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                    recommendation.driverType,
                                    style = MaterialTheme.typography.labelSmall
                            )
                        }
                )
                AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                    recommendation.soundSignature,
                                    style = MaterialTheme.typography.labelSmall
                            )
                        }
                )
                AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                    "₹${recommendation.priceINR}",
                                    style = MaterialTheme.typography.labelSmall
                            )
                        }
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                    recommendation.justification,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CrinacleGradeBadge(grade: String) {
    // Handle explicitly "null" string from LLM or actual null/blank
    val safeGrade =
            if (grade.equals("null", ignoreCase = true) || grade.isBlank()) "Unranked" else grade

    val color =
            when {
                safeGrade.startsWith("A") -> GradeA
                safeGrade.startsWith("B") -> GradeB
                safeGrade.startsWith("C") -> GradeC
                safeGrade == "Unranked" -> MaterialTheme.colorScheme.onSurfaceVariant
                else -> GradeD
            }

    Surface(color = color.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small) {
        Text(
                text = safeGrade,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = color
        )
    }
}
