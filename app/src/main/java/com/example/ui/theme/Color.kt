package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// PostureBelt Vibrant & High-Contrast Design Tokens

// Light Theme - Fresh, energetic, crisp modern aesthetic
val LightBackground = Color(0xFFF4F6FC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceAlt = Color(0xFFEDF2F9)
val LightSurfaceElevated = Color(0xFFFFFFFF)
val LightText = Color(0xFF0F172A)
val LightTextMuted = Color(0xFF64748B)
val LightBorder = Color(0xFFE2E8F0)
val LightPrimary = Color(0xFF4F46E5)
val LightPrimaryVariant = Color(0xFF6366F1)
val LightAccent = Color(0xFF06B6D4)
val LightGood = Color(0xFF059669)
val LightWarning = Color(0xFFD97706)
val LightBad = Color(0xFFDC2626)

// Dark Theme - Deep cyber-obsidian, glowing accents, non-dull rich contrasts
val DarkBackground = Color(0xFF090D16)
val DarkSurface = Color(0xFF111726)
val DarkSurfaceAlt = Color(0xFF182236)
val DarkSurfaceElevated = Color(0xFF1E2942)
val DarkText = Color(0xFFF8FAFC)
val DarkTextMuted = Color(0xFF94A3B8)
val DarkBorder = Color(0xFF1E293B)
val DarkPrimary = Color(0xFF6366F1)
val DarkPrimaryVariant = Color(0xFF818CF8)
val DarkAccent = Color(0xFF38BDF8)
val DarkGood = Color(0xFF10B981)
val DarkWarning = Color(0xFFF59E0B)
val DarkBad = Color(0xFFF43F5E)

// Gradient presets
val VibrantIndigoCyanGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF4F46E5), Color(0xFF2563EB), Color(0xFF06B6D4))
)

val DarkVibrantIndigoCyanGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF6366F1), Color(0xFF38BDF8), Color(0xFF818CF8))
)

val VibrantGoodGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF10B981), Color(0xFF059669))
)

val VibrantWarningGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFF59E0B), Color(0xFFD97706))
)

val VibrantBadGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFEF4444), Color(0xFFDC2626))
)
