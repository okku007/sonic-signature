package com.sonicsignature.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BudgetSlider(
        value: Float,
        onValueChange: (Float) -> Unit,
        valueRange: ClosedFloatingPointRange<Float> = 1000f..100000f,
        modifier: Modifier = Modifier
) {
        Column(modifier = modifier) {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        Text(
                                "Budget",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                        )
                        androidx.compose.foundation.text.BasicTextField(
                                value = value.toInt().toString(),
                                onValueChange = { newValue ->
                                        val asInt = newValue.toIntOrNull()
                                        if (asInt != null) {
                                                // constrain to range if needed, or let them type
                                                // beyond
                                                onValueChange(asInt.toFloat().coerceIn(valueRange))
                                        } else if (newValue.isEmpty()) {
                                                onValueChange(valueRange.start)
                                        }
                                },
                                textStyle =
                                        MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.secondary
                                        ),
                                keyboardOptions =
                                        androidx.compose.foundation.text.KeyboardOptions(
                                                keyboardType =
                                                        androidx.compose.ui.text.input.KeyboardType
                                                                .Number
                                        ),
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                        "₹",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.secondary
                                                )
                                                innerTextField()
                                        }
                                }
                        )
                }

                Spacer(Modifier.height(8.dp))

                val stepsBetween =
                        ((valueRange.endInclusive - valueRange.start) / 1000f).toInt() - 1

                Slider(
                        value = value,
                        onValueChange = { newValue ->
                                // Enforce the 1000 step increment mathematically
                                val steppedValue = (newValue / 1000f).toInt() * 1000f
                                onValueChange(steppedValue)
                        },
                        valueRange = valueRange,
                        // steps are intentionally omitted to avoid drawing annoying dots
                        colors =
                                SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.secondary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary,
                                        inactiveTrackColor =
                                                MaterialTheme.colorScheme.primary.copy(
                                                        alpha = 0.24f
                                                ),
                                        // Hide tick marks if any are drawn by default by setting to
                                        // transparent
                                        activeTickColor =
                                                androidx.compose.ui.graphics.Color.Transparent,
                                        inactiveTickColor =
                                                androidx.compose.ui.graphics.Color.Transparent
                                ),
                        modifier = Modifier.fillMaxWidth()
                )
        }
}
