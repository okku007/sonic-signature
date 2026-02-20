package com.sonicsignature.android.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val FallbackDarkColorScheme = darkColorScheme(
    primary = TealPrimary,
    onPrimary = TealOnPrimary,
    primaryContainer = TealContainer,
    background = NavyBackground,
    surface = NavySurface,
    surfaceVariant = NavyContainer,
    onBackground = OnBackground,
    onSurface = OnSurface,
    error = ErrorColor
)

/**
 * App theme with Material You dynamic color on Android 12+ (API 31+).
 * Falls back to the hardcoded navy/teal palette on older devices.
 * Per user requirement: Material You with hardcoded fallback.
 */
@Composable
fun SonicSignatureTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        else -> FallbackDarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
