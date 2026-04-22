package com.sonicsignature.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.Font
import sonic_signature.shared.generated.resources.Res
import sonic_signature.shared.generated.resources.inter_variable
import sonic_signature.shared.generated.resources.space_grotesk_variable

private fun sonicTextStyle(
        fontFamily: FontFamily,
        fontWeight: FontWeight,
        fontSize: Int,
        lineHeight: Int,
        letterSpacing: Float = 0f
) =
        TextStyle(
                fontFamily = fontFamily,
                fontWeight = fontWeight,
                fontSize = fontSize.sp,
                lineHeight = lineHeight.sp,
                letterSpacing = letterSpacing.sp
        )

@Composable
fun sonicTypography(): Typography {
        val spaceGrotesk =
                FontFamily(
                        Font(Res.font.space_grotesk_variable, weight = FontWeight.Normal),
                        Font(Res.font.space_grotesk_variable, weight = FontWeight.Medium),
                        Font(Res.font.space_grotesk_variable, weight = FontWeight.SemiBold),
                        Font(Res.font.space_grotesk_variable, weight = FontWeight.Bold)
                )
        val inter =
                FontFamily(
                        Font(Res.font.inter_variable, weight = FontWeight.Normal),
                        Font(Res.font.inter_variable, weight = FontWeight.Medium),
                        Font(Res.font.inter_variable, weight = FontWeight.SemiBold),
                        Font(Res.font.inter_variable, weight = FontWeight.Bold)
                )

        return Typography(
                displayLarge =
                        sonicTextStyle(
                                fontFamily = spaceGrotesk,
                                fontWeight = FontWeight.Bold,
                                fontSize = 48,
                                lineHeight = 52,
                                letterSpacing = (-0.8f)
                        ),
                displayMedium =
                        sonicTextStyle(
                                fontFamily = spaceGrotesk,
                                fontWeight = FontWeight.Bold,
                                fontSize = 40,
                                lineHeight = 44,
                                letterSpacing = (-0.6f)
                        ),
                headlineLarge =
                        sonicTextStyle(
                                fontFamily = spaceGrotesk,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 32,
                                lineHeight = 38,
                                letterSpacing = (-0.3f)
                        ),
                headlineMedium =
                        sonicTextStyle(
                                fontFamily = spaceGrotesk,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 24,
                                lineHeight = 30,
                                letterSpacing = (-0.2f)
                        ),
                titleLarge =
                        sonicTextStyle(
                                fontFamily = spaceGrotesk,
                                fontWeight = FontWeight.Medium,
                                fontSize = 20,
                                lineHeight = 24,
                                letterSpacing = 0.1f
                        ),
                titleMedium =
                        sonicTextStyle(
                                fontFamily = spaceGrotesk,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16,
                                lineHeight = 20,
                                letterSpacing = 0.1f
                        ),
                bodyLarge =
                        sonicTextStyle(
                                fontFamily = inter,
                                fontWeight = FontWeight.Normal,
                                fontSize = 16,
                                lineHeight = 26
                        ),
                bodyMedium =
                        sonicTextStyle(
                                fontFamily = inter,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14,
                                lineHeight = 22,
                                letterSpacing = 0.1f
                        ),
                bodySmall =
                        sonicTextStyle(
                                fontFamily = inter,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12,
                                lineHeight = 18,
                                letterSpacing = 0.1f
                        ),
                labelLarge =
                        sonicTextStyle(
                                fontFamily = inter,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12,
                                lineHeight = 14,
                                letterSpacing = 1.4f
                        ),
                labelMedium =
                        sonicTextStyle(
                                fontFamily = inter,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11,
                                lineHeight = 14,
                                letterSpacing = 1.1f
                        ),
                labelSmall =
                        sonicTextStyle(
                                fontFamily = spaceGrotesk,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13,
                                lineHeight = 18,
                                letterSpacing = 0.6f
                        )
        )
}
