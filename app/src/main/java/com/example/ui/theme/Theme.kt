package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.example.data.model.ThemeMode
import com.example.data.model.KeyboardFontStyle

private val BayanDarkColorScheme = darkColorScheme(
    primary = EmeraldGlow,
    onPrimary = Color(0xFF042F24),
    primaryContainer = Color(0xFF064E3B),
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = AmberGold,
    onSecondary = Color(0xFF2D1900),
    secondaryContainer = Color(0xFF452A06),
    onSecondaryContainer = Color(0xFFFDE68A),
    tertiary = Color(0xFF38BDF8),
    onTertiary = Color(0xFF082F49),
    background = MidnightSlate,
    onBackground = Color(0xFFF1F5F9),
    surface = DeepNavySurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = ElevatedSlate,
    onSurfaceVariant = LightMutedText,
    outline = Color(0xFF293548),
    error = SoftErrorRed,
    onError = Color.White
)

private val BayanLightColorScheme = lightColorScheme(
    primary = CobaltAccent,
    onPrimary = CrispWhite,
    primaryContainer = Color(0xFFCCFBF1),
    onPrimaryContainer = Color(0xFF042F2E),
    secondary = Color(0xFFD97706),
    onSecondary = CrispWhite,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF451A03),
    tertiary = Color(0xFF0284C7),
    onTertiary = CrispWhite,
    background = WarmSand,
    onBackground = DeepInk,
    surface = CrispWhite,
    onSurface = DeepInk,
    surfaceVariant = IvorySurface,
    onSurfaceVariant = MutedSlateText,
    outline = Color(0xFFE2E8F0),
    error = SoftErrorRed,
    onError = Color.White
)

@Composable
fun BayanTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    customAccentColor: Color? = null,
    fontStyle: KeyboardFontStyle = KeyboardFontStyle.CAIRO,
    content: @Composable () -> Unit
) {
    val fontFamily = fontFamilyFor(fontStyle)
    val systemDark = isSystemInDarkTheme()
    val useDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.CUSTOM -> systemDark
    }

    val baseScheme = if (useDark) BayanDarkColorScheme else BayanLightColorScheme
    val colorScheme = if (themeMode == ThemeMode.CUSTOM && customAccentColor != null) {
        baseScheme.copy(
            primary = customAccentColor,
            primaryContainer = customAccentColor.copy(alpha = 0.18f),
            onPrimaryContainer = if (useDark) Color.White else DeepInk
        )
    } else {
        baseScheme
    }

    CompositionLocalProvider(LocalBayanFontFamily provides fontFamily) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = appTypography(fontFamily),
            content = content
        )
    }
}
