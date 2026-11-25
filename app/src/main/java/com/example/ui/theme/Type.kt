package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.KeyboardFontStyle

val CairoFontFamily = FontFamily(
    Font(R.font.cairo, FontWeight.Normal),
    Font(R.font.cairo, FontWeight.Medium),
    Font(R.font.cairo, FontWeight.SemiBold),
    Font(R.font.cairo, FontWeight.Bold)
)

val IbmPlexArabicFontFamily = FontFamily(
    Font(R.font.ibm_plex_sans_arabic, FontWeight.Normal),
    Font(R.font.ibm_plex_sans_arabic, FontWeight.Medium),
    Font(R.font.ibm_plex_sans_arabic, FontWeight.SemiBold)
)

fun fontFamilyFor(style: KeyboardFontStyle): FontFamily = when (style) {
    KeyboardFontStyle.CAIRO -> CairoFontFamily
    KeyboardFontStyle.IBM_PLEX -> IbmPlexArabicFontFamily
    KeyboardFontStyle.SYSTEM_SANS -> FontFamily.SansSerif
    KeyboardFontStyle.SYSTEM_SERIF -> FontFamily.Serif
}

val LocalBayanFontFamily = compositionLocalOf { CairoFontFamily }

fun appTypography(fontFamily: FontFamily = CairoFontFamily): Typography = Typography(
    displaySmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 36.sp),
    headlineMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 32.sp),
    headlineSmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),
    titleMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp)
)
