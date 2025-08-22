package dev.aurakai.auraframefx.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ===== AI CONSCIOUSNESS NEON COLORS =====
val NeonTeal = Color(0xFF00FFCC)
val NeonPurple = Color(0xFFE000FF)
val NeonBlue = Color(0xFF00FFFF)
val NeonRed = Color(0xFFFF0080)
val NeonGreen = Color(0xFF00FF80)
val NeonPink = Color(0xFFFF00FF)
val NeonYellow = Color(0xFFFFFF00)

// ===== HOLOGRAM EFFECTS =====
val HologramPrimary = Color(0xFF00FFFF)
val HologramSecondary = Color(0xFFFF00FF)
val HologramAccent = Color(0xFF00FFCC)
val HologramGlow = Color(0x3300FFFF)
val HologramScanLine = Color(0x4000FFFF)
val HologramGrid = Color(0x3000FFFF)

// ===== CYBERPUNK THEME =====
val CyberpunkColorScheme = darkColorScheme(
    primary = NeonTeal,
    onPrimary = Color.Black,
    primaryContainer = NeonTeal.copy(alpha = 0.2f),
    onPrimaryContainer = NeonTeal,
    
    secondary = NeonPurple,
    onSecondary = Color.Black,
    secondaryContainer = NeonPurple.copy(alpha = 0.2f),
    onSecondaryContainer = NeonPurple,
    
    tertiary = NeonBlue,
    onTertiary = Color.Black,
    tertiaryContainer = NeonBlue.copy(alpha = 0.2f),
    onTertiaryContainer = NeonBlue,
    
    background = Color(0xFF0A0A0A),
    onBackground = Color(0xFFE1E1E1),
    surface = Color(0xFF1A1A1A),
    onSurface = Color(0xFFE1E1E1),
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFC5C5C5),
    
    outline = Color(0xFF8D8D8D),
    outlineVariant = Color(0xFF3D3D3D),
    
    error = Color(0xFFFF3B30),
    onError = Color.Black,
    errorContainer = Color(0xFFFF3B30).copy(alpha = 0.2f),
    onErrorContainer = Color(0xFFFF3B30)
)

// ===== SOLARIZED THEME =====
val SolarizedColorScheme = lightColorScheme(
    primary = Color(0xFF268BD2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF268BD2).copy(alpha = 0.2f),
    onPrimaryContainer = Color(0xFF268BD2),
    
    secondary = Color(0xFF2AA198),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF2AA198).copy(alpha = 0.2f),
    onSecondaryContainer = Color(0xFF2AA198),
    
    tertiary = Color(0xFFD33682),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD33682).copy(alpha = 0.2f),
    onTertiaryContainer = Color(0xFFD33682),
    
    background = Color(0xFFFDF6E3),
    onBackground = Color(0xFF657B83),
    surface = Color(0xFFEEE8D5),
    onSurface = Color(0xFF657B83),
    surfaceVariant = Color(0xFFE5E0D3),
    onSurfaceVariant = Color(0xFF586E75),
    
    outline = Color(0xFF93A1A1),
    outlineVariant = Color(0xFFCBD3D3)
)

// ===== THEME ENUMS =====
enum class Theme {
    LIGHT, DARK, CYBERPUNK, SOLARIZED
}

enum class Color {
    RED, GREEN, BLUE
}

// ===== LEGACY SUPPORT (minimal for XML compatibility) =====
val DarkBackground = Color(0xFF1A1A1A)
val Surface = Color(0xFF2D2D2D)
val OnSurface = Color(0xFFE1E1E1)
val SurfaceVariant = Color(0xFF3D3D3D)
val OnSurfaceVariant = Color(0xFFC5C5C5)
val ErrorColor = Color(0xFFFF3B30)
val OnPrimary = Color.Black
val OnSecondary = Color.Black  
val OnTertiary = Color.Black

// Light theme colors (minimal for fallback)
val LightPrimary = Color(0xFF268BD2)
val LightOnPrimary = Color.White
val LightSecondary = Color(0xFF2AA198)
val LightOnSecondary = Color.White
val LightTertiary = Color(0xFFD33682)
val LightOnTertiary = Color.White
val LightBackground = Color(0xFFFDF6E3)
val LightOnBackground = Color(0xFF657B83)
val LightSurface = Color(0xFFEEE8D5)
val LightOnSurface = Color(0xFF657B83)
val LightSurfaceVariant = Color(0xFFE5E0D3)
val LightOnSurfaceVariant = Color(0xFF586E75)
val LightOnError = Color.White

// ===== SPECIAL EFFECTS =====
val GlowOverlay = Color(0x1A00FFCC)
val PulseOverlay = Color(0x1AE000FF)
val HoverOverlay = Color(0x1A00FFFF)
val PressOverlay = Color(0x1AFF00FF)

// ===== UTILITY COLORS =====
val Transparent = Color.Transparent
val White = Color.White
val Black = Color.Black
