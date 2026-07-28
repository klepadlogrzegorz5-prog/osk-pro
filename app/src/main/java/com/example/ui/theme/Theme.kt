package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF3B82F6),          // Vibrant Electric Royal Blue
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFBFDBFE),
    secondary = Color(0xFF06B6D4),        // Neon Cyan
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF0891B2),
    onSecondaryContainer = Color(0xFFCFFAFE),
    tertiary = Color(0xFF10B981),         // Emerald Green
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF065F46),
    onTertiaryContainer = Color(0xFFA7F3D0),
    background = Color(0xFF0B1120),       // Ultra Dark Slate Blue Navy
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),          // Elevated Slate Card Surface
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1E40AF),          // Executive Deep Blue
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEFF6FF),
    onPrimaryContainer = Color(0xFF1D4ED8),
    secondary = Color(0xFF0284C7),        // Vibrant Cyan Blue
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF0F9FF),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = Color(0xFF059669),         // Safety Emerald Green
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFECFDF5),
    onTertiaryContainer = Color(0xFF047857),
    background = Color(0xFFF1F5F9),       // Soft Luxury Slate White
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Enforce our custom professional theme for consistency
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

