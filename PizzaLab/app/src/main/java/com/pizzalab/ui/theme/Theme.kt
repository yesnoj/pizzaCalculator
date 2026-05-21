package com.pizzalab.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Pizza-themed colors
private val DeepOrange = Color(0xFFD84315)
private val DeepOrangeLight = Color(0xFFFF7043)
private val DeepOrangeDark = Color(0xFFBF360C)
private val WarmAmber = Color(0xFFFF8F00)
private val WarmAmberLight = Color(0xFFFFB300)
private val WarmAmberDark = Color(0xFFFF6F00)
private val TomatoRed = Color(0xFFC62828)
private val TomatoRedLight = Color(0xFFEF5350)
private val TomatoRedDark = Color(0xFFB71C1C)

private val CreamWhite = Color(0xFFFFF8E1)
private val WarmSurface = Color(0xFFFFFBF5)
private val DarkSurface = Color(0xFF1A1210)
private val DarkBackground = Color(0xFF121010)

private val LightColorScheme = lightColorScheme(
    primary = DeepOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBCF),
    onPrimaryContainer = Color(0xFF3A0A00),
    secondary = WarmAmber,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE082),
    onSecondaryContainer = Color(0xFF261A00),
    tertiary = TomatoRed,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDAD6),
    onTertiaryContainer = Color(0xFF410002),
    background = WarmSurface,
    onBackground = Color(0xFF201A17),
    surface = WarmSurface,
    onSurface = Color(0xFF201A17),
    surfaceVariant = Color(0xFFF5DED3),
    onSurfaceVariant = Color(0xFF53433D),
    outline = Color(0xFF85736B),
    outlineVariant = Color(0xFFD8C2B8),
)

private val DarkColorScheme = darkColorScheme(
    primary = DeepOrangeLight,
    onPrimary = Color(0xFF5F1600),
    primaryContainer = DeepOrangeDark,
    onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = WarmAmberLight,
    onSecondary = Color(0xFF422C00),
    secondaryContainer = WarmAmberDark,
    onSecondaryContainer = Color(0xFFFFE082),
    tertiary = TomatoRedLight,
    onTertiary = Color(0xFF690005),
    tertiaryContainer = TomatoRedDark,
    onTertiaryContainer = Color(0xFFFFDAD6),
    background = DarkBackground,
    onBackground = Color(0xFFEDE0DA),
    surface = DarkSurface,
    onSurface = Color(0xFFEDE0DA),
    surfaceVariant = Color(0xFF53433D),
    onSurfaceVariant = Color(0xFFD8C2B8),
    outline = Color(0xFFA08D84),
    outlineVariant = Color(0xFF53433D),
)

@Composable
fun PizzaLabTheme(
    darkTheme: Boolean = false,  // Sempre tema chiaro
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
