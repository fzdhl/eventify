package com.example.tugasakhirpam.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColorScheme = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = BrandBlueSoft,
    onPrimaryContainer = BrandBlueDark,
    secondary = BrandBlue,
    secondaryContainer = BrandBlueSoft,
    onSecondaryContainer = BrandBlue,
    background = AppBackground,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = SegmentTrack,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLowest = SurfaceWhite,
    surfaceContainerLow = SurfaceWhite,
    surfaceContainer = SurfaceWhite,
    surfaceContainerHigh = SurfaceWhite,
    surfaceContainerHighest = SurfaceWhite,
    outline = BorderInput,
    outlineVariant = BorderSoft,
    error = DangerText,
    errorContainer = DangerBg,
)

/** Selalu light mode, mengikuti desain. */
@Composable
fun TugasAkhirPAMTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = AppTypography,
        content = content
    )
}