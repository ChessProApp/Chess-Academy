package com.chessacademy.ai.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Brett-Themes (unverändert aus der bisherigen App). */
data class BoardPreset(val name: String, val light: Color, val dark: Color, val border: Color)

val BoardPresets = listOf(
    BoardPreset("Smaragd Pro", Color(0xFFEEEED2), Color(0xFF769656), Color(0xFF4B6335)),
    BoardPreset("Obsidian Gold", Color(0xFFE8DFCA), Color(0xFF2C3639), Color(0xFFD4AF37)),
    BoardPreset("Mitternacht", Color(0xFFDCE4F2), Color(0xFF3B5998), Color(0xFF1E325C)),
    BoardPreset("Kirschblüte", Color(0xFFFCE4EC), Color(0xFFAD1457), Color(0xFF6A0032)),
    BoardPreset("Cyber Neon", Color(0xFFE0F7FA), Color(0xFF006064), Color(0xFF00E5FF)),
    BoardPreset("Walnuss Edel", Color(0xFFF3E5AB), Color(0xFF795548), Color(0xFF4E342E)),
    BoardPreset("Schiefer Grau", Color(0xFFF5F5F5), Color(0xFF546E7A), Color(0xFF263238)),
    BoardPreset("Royal Violett", Color(0xFFEDE7F6), Color(0xFF512DA8), Color(0xFF311B92))
)

/** Akzent-Themes (unverändert aus der bisherigen App). */
enum class UiAccentTheme(val label: String, val primary: Color, val bgDark: Color, val surfaceDark: Color) {
    EMERALD("Smaragd", Color(0xFF4ADE80), Color(0xFF0B110E), Color(0xFF141F1A)),
    GOLD("Royal Gold", Color(0xFFFACC15), Color(0xFF110F0A), Color(0xFF1E1B12)),
    OCEAN("Ozean Blau", Color(0xFF38BDF8), Color(0xFF090E14), Color(0xFF111B26)),
    CRIMSON("Rubin Rot", Color(0xFFFB7185), Color(0xFF14090C), Color(0xFF241116))
}

/** Einheitliche Typografie: wenige, klar abgestufte Größen. */
private val AppTypography = Typography(
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold, lineHeight = 26.sp),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, lineHeight = 22.sp),
    titleSmall = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontSize = 13.sp, lineHeight = 19.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium)
)

@Composable
fun ChessAcademyTheme(accent: UiAccentTheme, content: @Composable () -> Unit) {
    val primary = androidx.compose.animation.animateColorAsState(accent.primary, tween(240, easing = FastOutSlowInEasing), label = "primary").value
    val bg = animateColorAsState(accent.bgDark, tween(240), label = "bg").value
    val surface = animateColorAsState(accent.surfaceDark, tween(240), label = "surface").value
    val scheme = darkColorScheme(
        primary = primary, onPrimary = Color(0xFF07140D),
        background = bg, onBackground = Color(0xFFEAEFEA),
        surface = surface, onSurface = Color(0xFFEAEFEA),
        surfaceVariant = surface, onSurfaceVariant = Color(0xFFB8C2BA),
        secondaryContainer = primary.copy(alpha = 0.18f), onSecondaryContainer = primary,
        outline = Color(0x33FFFFFF)
    )
    MaterialTheme(colorScheme = scheme, typography = AppTypography, content = content)
}

val GoodGreen = Color(0xFF4ADE80)
val WarnAmber = Color(0xFFFBBF24)
val BadRed = Color(0xFFF87171)
