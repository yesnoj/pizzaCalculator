/* ============================================================
 * PizzaLab — Quaderno direction
 * Compose / Material 3 tokens.
 *
 * Drop this in app/src/main/java/com/pizzalab/ui/theme/QuadernoTheme.kt
 * and call PizzaLabTheme to use. Replaces the existing Theme.kt's
 * LightColorScheme.
 * ============================================================ */

package com.pizzalab.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Palette ──────────────────────────────────────────────────
object QuadernoColors {
    val Paper       = Color(0xFFFDF9EE)  // card surface, dialog
    val Bg          = Color(0xFFF5EEDC)  // app background — aged paper
    val BgWarmer    = Color(0xFFEFE7D2)  // sunken / pressed
    val Ink         = Color(0xFF2B1F12)  // primary text, dark CTA
    val Ink2        = Color(0xFF65553E)  // secondary text
    val Ink3        = Color(0xFF9D8B6E)  // meta, placeholder, italic
    val Rule        = Color(0xFFD9CEB3)  // solid hairline
    val RuleDots    = Color(0xFFC5B591)  // dotted hairline (use dashedStroke)
    val Primary     = Color(0xFFA8392B)  // tomato — single accent
    val PrimaryHi   = Color(0xFFC04A2A)  // hover / pressed
    val Olive       = Color(0xFF5E6B3D)  // secondary accent
    val OliveDk     = Color(0xFF3F4A28)

    // Calculator category accents
    val CatImpasto  = Primary
    val CatFarine   = Olive
    val CatUtility  = Color(0xFF7A6043)
}

// ── ColorScheme — maps the Quaderno palette into Material 3 slots ──
//
// onPrimary uses Paper (not pure white) so primary buttons stay
// warm. surface = Paper so cards lift slightly off the bg.
val QuadernoColorScheme = lightColorScheme(
    primary             = QuadernoColors.Primary,
    onPrimary           = QuadernoColors.Paper,
    primaryContainer    = Color(0xFFF5D4CB),       // 14% Primary on Paper
    onPrimaryContainer  = QuadernoColors.Primary,

    secondary           = QuadernoColors.Olive,
    onSecondary         = QuadernoColors.Paper,
    secondaryContainer  = Color(0xFFDDE2CC),       // 14% Olive on Paper
    onSecondaryContainer= QuadernoColors.OliveDk,

    tertiary            = QuadernoColors.Ink,      // dark CTA = "neutral primary"
    onTertiary          = QuadernoColors.Paper,
    tertiaryContainer   = QuadernoColors.BgWarmer,
    onTertiaryContainer = QuadernoColors.Ink,

    background          = QuadernoColors.Bg,
    onBackground        = QuadernoColors.Ink,
    surface             = QuadernoColors.Paper,
    onSurface           = QuadernoColors.Ink,
    surfaceVariant      = QuadernoColors.BgWarmer,
    onSurfaceVariant    = QuadernoColors.Ink2,
    outline             = QuadernoColors.Rule,
    outlineVariant      = QuadernoColors.RuleDots,

    error               = Color(0xFFB00020),
    onError             = QuadernoColors.Paper,
)

// ── Typography — single family (Inter), distinct weights ─────
//
// Letter-spacing values come from the JSX (`letterSpacing: '-0.02em'`
// etc.). Convert: -0.02em ≈ -0.02 * sp size.
val QuadernoTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Black,             // 900
        fontSize = 56.sp, lineHeight = 56.sp, letterSpacing = (-2.2).sp
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.ExtraBold,         // 800
        fontSize = 38.sp, lineHeight = 38.sp, letterSpacing = (-1.5).sp
    ),
    headlineMedium = TextStyle(                    // screen titles
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp, lineHeight = 32.sp, letterSpacing = (-0.7).sp
    ),
    headlineSmall = TextStyle(                     // card titles
        fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp, lineHeight = 26.sp, letterSpacing = (-0.5).sp
    ),
    titleLarge = TextStyle(                        // phase name
        fontWeight = FontWeight.ExtraBold,
        fontSize = 18.sp, lineHeight = 22.sp, letterSpacing = (-0.4).sp
    ),
    titleMedium = TextStyle(                       // section header in cards
        fontWeight = FontWeight.Bold,              // 700
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(                        // tab label (active)
        fontWeight = FontWeight.Bold,
        fontSize = 12.5.sp, lineHeight = 16.sp, letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Medium,            // 500
        fontSize = 13.sp, lineHeight = 19.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 17.sp
    ),
    labelLarge = TextStyle(                        // CTA text
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.3.sp
    ),
    labelSmall = TextStyle(                        // kicker
        fontWeight = FontWeight.Bold,
        fontSize = 10.5.sp, lineHeight = 14.sp, letterSpacing = 2.5.sp  // 0.24em ≈ 2.5sp
    ),
)

// ── Theme entry point ────────────────────────────────────────
//
// Replaces PizzaLabTheme(darkTheme = false) with QuadernoTheme.
@Composable
fun QuadernoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = QuadernoColorScheme,
        typography  = QuadernoTypography,
        content     = content
    )
}

// ── Numeric text style — for tabular numerals (W, grams, times) ──
//
// Apply with .copy(fontFeatureSettings = "tnum") on any of the
// title* / display* styles above.
// Example:
//   Text(
//     "614",
//     style = MaterialTheme.typography.headlineSmall.copy(
//       fontFeatureSettings = "tnum"
//     )
//   )
