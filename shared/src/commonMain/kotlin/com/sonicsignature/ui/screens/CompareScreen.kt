package com.sonicsignature.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sonicsignature.model.IEMComparisonItem

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CompareScreen(
        comparisonItems: List<IEMComparisonItem>,
        onAddIemClick: () -> Unit,
        onRemoveIemClick: (IEMComparisonItem) -> Unit
) {
    Column(
            modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Compare", style = MaterialTheme.typography.headlineLarge)

            if (comparisonItems.size < 3) {
                IconButton(onClick = onAddIemClick, modifier = Modifier.size(48.dp)) {
                    Icon(
                            Icons.Default.Add,
                            contentDescription = "Add IEM",
                            tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        if (comparisonItems.isEmpty()) {
            Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
            ) {
                Text(
                        text = "No IEMs selected for comparison.\nClick the + icon to add some.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            return
        }

        // Horizontal scrolling comparison items
        LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(comparisonItems) { item ->
                ComparisonCard(
                        item = item,
                        onRemove = { onRemoveIemClick(item) },
                        modifier = Modifier.width(280.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Tonal Comparison Overlay", style = MaterialTheme.typography.titleMedium)

        OutlinedCard(modifier = Modifier.fillMaxWidth().height(250.dp)) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                TonalOverlayGraph(items = comparisonItems, modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun ComparisonCard(
        item: IEMComparisonItem,
        onRemove: () -> Unit,
        modifier: Modifier = Modifier
) {
    OutlinedCard(modifier = modifier) {
        Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                            item.recommendation.name,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                    )
                    Text(
                            item.recommendation.brand,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove",
                            modifier = Modifier.size(16.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // Compatibility Score
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                        "Match Score: ",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                        "${item.compatibilityScore.toInt()}%",
                        style = MaterialTheme.typography.bodyLarge,
                        color =
                                if (item.compatibilityScore > 80)
                                        MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold
                )
            }

            // Specs
            SpecRow("Price", "₹${item.recommendation.priceINR}")
            SpecRow("Driver", item.recommendation.driverType)
            SpecRow("Signature", item.recommendation.soundSignature.replace("_", " "))

            Spacer(Modifier.height(4.dp))
            Text(
                    "Strengths",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
            )
            item.matchingStrengths.take(2).forEach { strength ->
                Text("• $strength", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(4.dp))
            Text(
                    "Trade-offs",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
            )
            item.tradeOffs.take(2).forEach { tradeoff ->
                Text("• $tradeoff", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

// A mock tonal overlay graph. In a real application this would map actual frequencies
// using Compose Charts or precise Canvas paths.
@Composable
private fun TonalOverlayGraph(items: List<IEMComparisonItem>, modifier: Modifier = Modifier) {
    val colors =
            listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.secondary,
                    MaterialTheme.colorScheme.tertiary
            )

    val gridColor = MaterialTheme.colorScheme.surfaceVariant

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Draw grid
        for (i in 1..4) {
            val y = height * (i / 5f)
            drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
            )
        }

        // Draw mock frequency response lines for each item
        items.forEachIndexed { index, item ->
            val color = colors[index % colors.size]
            val path = Path()

            // Generate a fake curve based on sound signature heuristics
            val isVShaped = item.recommendation.soundSignature.contains("V_SHAPED")
            val isNeutral = item.recommendation.soundSignature.contains("NEUTRAL")

            path.moveTo(0f, height * 0.5f)

            val points = 20
            for (i in 1..points) {
                val x = width * (i.toFloat() / points)
                var yOffset = 0f
                if (isVShaped) {
                    yOffset =
                            if (i < points / 3 || i > 2 * points / 3) -height * 0.2f
                            else height * 0.1f
                } else if (!isNeutral) {
                    yOffset = if (i < points / 2) -height * 0.15f else height * 0.1f
                }

                // Add some random noise to differentiate them slightly based on index
                val noise = ((index + i) % 3 - 1) * height * 0.05f
                val y = (height * 0.5f) + yOffset + noise

                path.lineTo(x, y.coerceIn(0f, height))
            }

            drawPath(path = path, color = color, style = Stroke(width = 3f))
        }

        // Draw Legend
        items.forEachIndexed { index, item ->
            val color = colors[index % colors.size]
            drawCircle(
                    color = color,
                    radius = 4.dp.toPx(),
                    center = Offset(16.dp.toPx() + (index * 100.dp.toPx()), 16.dp.toPx())
            )
        }
    }
}
