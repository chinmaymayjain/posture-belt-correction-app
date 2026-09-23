package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Immutable
data class PostureColors(
    val background: Color,
    val surface: Color,
    val surfaceAlt: Color,
    val surfaceElevated: Color,
    val text: Color,
    val textMuted: Color,
    val border: Color,
    val primary: Color,
    val primaryVariant: Color,
    val accent: Color,
    val good: Color,
    val warning: Color,
    val bad: Color,
    val primaryGradient: Brush,
    val goodGradient: Brush,
    val warningGradient: Brush,
    val badGradient: Brush,
    val isDark: Boolean
)

val LightPostureColors = PostureColors(
    background = LightBackground,
    surface = LightSurface,
    surfaceAlt = LightSurfaceAlt,
    surfaceElevated = LightSurfaceElevated,
    text = LightText,
    textMuted = LightTextMuted,
    border = LightBorder,
    primary = LightPrimary,
    primaryVariant = LightPrimaryVariant,
    accent = LightAccent,
    good = LightGood,
    warning = LightWarning,
    bad = LightBad,
    primaryGradient = VibrantIndigoCyanGradient,
    goodGradient = VibrantGoodGradient,
    warningGradient = VibrantWarningGradient,
    badGradient = VibrantBadGradient,
    isDark = false
)

val DarkPostureColors = PostureColors(
    background = DarkBackground,
    surface = DarkSurface,
    surfaceAlt = DarkSurfaceAlt,
    surfaceElevated = DarkSurfaceElevated,
    text = DarkText,
    textMuted = DarkTextMuted,
    border = DarkBorder,
    primary = DarkPrimary,
    primaryVariant = DarkPrimaryVariant,
    accent = DarkAccent,
    good = DarkGood,
    warning = DarkWarning,
    bad = DarkBad,
    primaryGradient = DarkVibrantIndigoCyanGradient,
    goodGradient = VibrantGoodGradient,
    warningGradient = VibrantWarningGradient,
    badGradient = VibrantBadGradient,
    isDark = true
)

val LocalPostureColors = staticCompositionLocalOf { LightPostureColors }

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color.White,
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurfaceAlt,
    onSurfaceVariant = DarkTextMuted,
    outline = DarkBorder,
    secondary = DarkAccent,
    tertiary = DarkGood
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    background = LightBackground,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = LightSurfaceAlt,
    onSurfaceVariant = LightTextMuted,
    outline = LightBorder,
    secondary = LightAccent,
    tertiary = LightGood
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val postureColors = if (darkTheme) DarkPostureColors else LightPostureColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalPostureColors provides postureColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

object PostureTheme {
    val colors: PostureColors
        @Composable
        get() = LocalPostureColors.current
}
