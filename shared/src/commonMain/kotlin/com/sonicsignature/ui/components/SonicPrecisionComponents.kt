package com.sonicsignature.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

val SonicScreenMaxWidth = 980.dp
val SonicSectionGap = 24.dp
val SonicBlockGap = 16.dp
private val SonicChipShape = RoundedCornerShape(999.dp)

private val LoadingMessages =
        listOf(
                "Analyzing audio profile...",
                "Resolving tuning bias...",
                "Scanning driver and price bands...",
                "Cross-checking justification notes...",
                "Calibrating final recommendations..."
        )

enum class SonicTone {
    Neutral,
    Accent,
    Success,
    Warning,
    Danger,
}

enum class SonicTopLevelDestination {
    Discover,
    Settings,
}

@Composable
fun SonicContentColumn(
        modifier: Modifier = Modifier,
        verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(SonicSectionGap),
        content: @Composable ColumnScope.() -> Unit
) {
    Column(
            modifier = modifier.fillMaxWidth().widthIn(max = SonicScreenMaxWidth),
            verticalArrangement = verticalArrangement,
            content = content
    )
}

@Composable
fun SonicScreenHeader(
        title: String,
        subtitle: String,
        modifier: Modifier = Modifier,
        leading: (@Composable () -> Unit)? = null,
        trailing: (@Composable RowScope.() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                leading?.invoke()
            }
            Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    content = trailing ?: {}
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, style = MaterialTheme.typography.displayMedium)
            Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SonicSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier
    )
}

@Composable
fun SonicPanel(
        modifier: Modifier = Modifier,
        title: String? = null,
        badge: String? = null,
        badgeTone: SonicTone? = null,
        emphasized: Boolean = false,
        onClick: (() -> Unit)? = null,
        content: @Composable ColumnScope.() -> Unit
) {
    val panelModifier =
            modifier.fillMaxWidth()
                    .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)

    Column(
            modifier = panelModifier.padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (title != null || badge != null) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                title?.let { SonicSectionLabel(it) }
                badge?.let {
                    SonicBadge(
                            text = it,
                            tone = badgeTone ?: if (emphasized) SonicTone.Accent else SonicTone.Neutral
                    )
                }
            }
            SonicDivider()
        }
        content()
    }
}

@Composable
fun SonicBadge(
        text: String,
        tone: SonicTone = SonicTone.Neutral,
        modifier: Modifier = Modifier
) {
    val (container, content) =
            when (tone) {
                SonicTone.Accent ->
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f) to
                                MaterialTheme.colorScheme.primary
                SonicTone.Success -> Color(0xFF203228) to Color(0xFF8BD8A7)
                SonicTone.Warning -> Color(0xFF322A1F) to Color(0xFFFFD29D)
                SonicTone.Danger -> Color(0xFF4A1111) to Color(0xFFFF8A80)
                SonicTone.Neutral ->
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.14f) to
                                MaterialTheme.colorScheme.onSurface
            }

    Box(
            modifier =
                    modifier.background(container, SonicChipShape)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = content
        )
    }
}

@Composable
fun SonicPrimaryButton(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        leading: (@Composable RowScope.() -> Unit)? = null
) {
    Button(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.height(52.dp),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
            colors =
                    ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor =
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
                            disabledContentColor =
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                    )
    ) {
        leading?.invoke(this)
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun SonicSecondaryButton(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        leading: (@Composable RowScope.() -> Unit)? = null
) {
    Button(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.height(52.dp),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
            colors =
                    ButtonDefaults.buttonColors(
                            containerColor =
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f),
                            contentColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor =
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
                            disabledContentColor =
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                    )
    ) {
        leading?.invoke(this)
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun SonicIconFrame(
        modifier: Modifier = Modifier,
        onClick: () -> Unit,
        content: @Composable BoxScope.() -> Unit
) {
    Box(
            modifier =
                    modifier.size(40.dp)
                            .background(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f),
                                    RoundedCornerShape(14.dp)
                            )
                            .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun SonicField(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        modifier: Modifier = Modifier,
        placeholder: String = "",
        enabled: Boolean = true,
        readOnly: Boolean = false,
        singleLine: Boolean = true,
        maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
        trailing: (@Composable () -> Unit)? = null,
        leading: (@Composable () -> Unit)? = null,
        supportingText: String? = null,
        visualTransformation: VisualTransformation = VisualTransformation.None,
        keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
        keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.fillMaxWidth(),
            enabled = enabled,
            readOnly = readOnly,
            singleLine = singleLine,
            maxLines = maxLines,
            label = { Text(label) },
            placeholder =
                    if (placeholder.isNotBlank()) {
                        { Text(placeholder, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    } else {
                        null
                    },
            leadingIcon = leading,
            trailingIcon = trailing,
            supportingText =
                    if (supportingText != null) {
                        {
                            Text(
                                    text = supportingText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        null
                    },
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            colors =
                    OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent,
                            focusedContainerColor =
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f),
                            unfocusedContainerColor =
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f),
                            disabledContainerColor =
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f),
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
    )
}

@Composable
fun SonicStatusMessage(
        title: String,
        body: String,
        tone: SonicTone,
        modifier: Modifier = Modifier
) {
    val accentColor =
            when (tone) {
                SonicTone.Accent -> MaterialTheme.colorScheme.primaryContainer
                SonicTone.Success -> Color(0xFF4E8E66)
                SonicTone.Warning -> Color(0xFFC28B45)
                SonicTone.Danger -> MaterialTheme.colorScheme.error
                SonicTone.Neutral -> MaterialTheme.colorScheme.outlineVariant
            }
    val backgroundColor =
            when (tone) {
                SonicTone.Accent -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)
                SonicTone.Success -> Color(0xFF16221A)
                SonicTone.Warning -> Color(0xFF241D14)
                SonicTone.Danger -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.34f)
                SonicTone.Neutral -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.10f)
            }

    Column(
            modifier =
                    modifier.fillMaxWidth()
                            .background(backgroundColor, RoundedCornerShape(20.dp))
                            .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.labelLarge, color = accentColor)
        Text(text = body, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun SonicLoadingState(
        modifier: Modifier = Modifier,
        compact: Boolean = false,
        title: String = "Calibrating recommendation engine",
        messages: List<String> = LoadingMessages
) {
    var loadingPhase by remember { mutableStateOf(0) }

    LaunchedEffect(messages) {
        while (true) {
            delay(1800)
            loadingPhase = (loadingPhase + 1) % messages.size
        }
    }

    if (compact) {
        Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                    text = messages[loadingPhase],
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary
            )
        }
    } else {
        SonicPanel(modifier = modifier.widthIn(max = 440.dp), title = "Engine status") {
            Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 2.2.dp,
                        color = MaterialTheme.colorScheme.primary
                )
                Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                )
                Text(
                        text = messages[loadingPhase],
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SonicMonogramBlock(
        label: String,
        sublabel: String,
        modifier: Modifier = Modifier,
        size: Dp = 88.dp
) {
    Box(
                    modifier =
                            modifier.size(size)
                            .background(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.16f),
                                    RoundedCornerShape(24.dp)
                            )
                            .padding(12.dp)
    ) {
        Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                    modifier =
                            Modifier.size(8.dp)
                                    .background(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            CircleShape
                                    )
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = label, style = MaterialTheme.typography.headlineMedium)
                Text(
                        text = sublabel.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SonicDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
            modifier = modifier,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.85f)
    )
}

@Composable
fun SonicBottomNav(
        selected: SonicTopLevelDestination,
        onDiscoverClick: () -> Unit,
        onSettingsClick: () -> Unit,
        modifier: Modifier = Modifier
) {
    Row(
            modifier =
                    modifier.fillMaxWidth()
                            .background(
                                    MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                            )
                            .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SonicBottomNavItem(
                label = "Discover",
                selected = selected == SonicTopLevelDestination.Discover,
                onClick = onDiscoverClick,
                icon = {
                    Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.widthIn(min = 132.dp)
        )
        SonicBottomNavItem(
                label = "Settings",
                selected = selected == SonicTopLevelDestination.Settings,
                onClick = onSettingsClick,
                icon = {
                    Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.widthIn(min = 132.dp)
        )
    }
}

@Composable
private fun SonicBottomNavItem(
        label: String,
        selected: Boolean,
        onClick: () -> Unit,
        icon: @Composable () -> Unit,
        modifier: Modifier = Modifier
) {
    val containerColor =
            if (selected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f)
            }
    val contentColor =
            if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }

    Row(
            modifier =
                    modifier.background(containerColor, RoundedCornerShape(18.dp))
                            .clickable(onClick = onClick)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
    ) {
        CompositionLocalProvider(androidx.compose.material3.LocalContentColor provides contentColor) {
            icon()
            Spacer(Modifier.width(8.dp))
            Text(text = label, style = MaterialTheme.typography.labelLarge)
        }
    }
}
