package com.sonicsignature.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun BudgetSlider(
        value: Float,
        onValueChange: (Float) -> Unit,
        valueRange: ClosedFloatingPointRange<Float> = 1000f..100000f,
        modifier: Modifier = Modifier
) {
        val minBudget = valueRange.start.toInt()
        val maxBudget = valueRange.endInclusive.toInt()
        var budgetInput by remember { mutableStateOf(value.toInt().toString()) }
        var isEditingBudget by remember { mutableStateOf(false) }
        val typedBudget = budgetInput.toIntOrNull()
        val budgetBelowMinimum = budgetInput.isNotBlank() && typedBudget != null && typedBudget < minBudget

        LaunchedEffect(value.toInt(), isEditingBudget) {
                if (!isEditingBudget) {
                        budgetInput = value.toInt().toString()
                }
        }

        Column(modifier = modifier) {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        SonicSectionLabel(text = "Budget target")
                        androidx.compose.foundation.text.BasicTextField(
                                value = budgetInput,
                                onValueChange = { newValue ->
                                        val digitsOnly = newValue.filter(Char::isDigit)
                                        budgetInput = digitsOnly

                                        val asInt = digitsOnly.toIntOrNull()
                                        if (asInt != null && asInt in minBudget..maxBudget) {
                                                onValueChange(asInt.toFloat())
                                        }
                                },
                                textStyle =
                                        MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.onSurface
                                        ),
                                modifier =
                                        Modifier.onFocusChanged { state ->
                                                val wasEditing = isEditingBudget
                                                isEditingBudget = state.isFocused
                                                if (wasEditing && !state.isFocused) {
                                                        val committedBudget =
                                                                budgetInput.toIntOrNull()
                                                                        ?.coerceIn(
                                                                                minBudget,
                                                                                maxBudget
                                                                        )

                                                        if (committedBudget != null) {
                                                                budgetInput =
                                                                        committedBudget.toString()
                                                                onValueChange(
                                                                        committedBudget.toFloat()
                                                                )
                                                        } else {
                                                                budgetInput =
                                                                        value.toInt().toString()
                                                        }
                                                }
                                        },
                                keyboardOptions =
                                        androidx.compose.foundation.text.KeyboardOptions(
                                                keyboardType =
                                                        androidx.compose.ui.text.input.KeyboardType
                                                                .Number
                                        ),
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                        Row(
                                                modifier =
                                                        Modifier.background(
                                                                        MaterialTheme.colorScheme.primaryContainer.copy(
                                                                                alpha = 0.18f
                                                                        ),
                                                                        RoundedCornerShape(16.dp)
                                                                )
                                                                .padding(
                                                                        horizontal = 10.dp,
                                                                        vertical = 8.dp
                                                                ),
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                Text(
                                                        "₹",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .primaryContainer
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                innerTextField()
                                        }
                                }
                        )
                }

                Spacer(Modifier.height(8.dp))

                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                        BudgetReadout(
                                label = "Min",
                                value = "₹$minBudget",
                                modifier = Modifier.weight(1f)
                        )
                        BudgetReadout(
                                label = "Target",
                                value = "₹${value.toInt()}",
                                emphasized = true,
                                modifier = Modifier.weight(1f)
                        )
                        BudgetReadout(
                                label = "Max",
                                value = "₹$maxBudget",
                                modifier = Modifier.weight(1f)
                        )
                }

                if (budgetBelowMinimum) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                                text = "Budget cannot be less than ₹$minBudget.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                        )
                }

                Spacer(Modifier.height(12.dp))

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
                                        thumbColor = MaterialTheme.colorScheme.primaryContainer,
                                        activeTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                        inactiveTrackColor =
                                                MaterialTheme.colorScheme.outlineVariant.copy(
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

@Composable
private fun BudgetReadout(
        label: String,
        value: String,
        modifier: Modifier = Modifier,
        emphasized: Boolean = false
) {
        Column(
                modifier =
                        modifier.background(
                                        if (emphasized) {
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)
                                        } else {
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f)
                                        },
                                        RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
                Text(
                        text = label.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                )
                Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (emphasized) FontWeight.SemiBold else FontWeight.Medium,
                        color =
                                if (emphasized) {
                                        MaterialTheme.colorScheme.primary
                                } else {
                                        MaterialTheme.colorScheme.onSurface
                                },
                        textAlign = TextAlign.Center
                )
        }
}
