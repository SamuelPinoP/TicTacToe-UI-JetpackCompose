package com.example.tictactoe.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = ArenaTeal80,
    onPrimary = ArenaBackgroundDark,
    primaryContainer = Color(0xFF154A52),
    onPrimaryContainer = Color(0xFFE4FAFC),
    secondary = ArenaCoral80,
    onSecondary = ArenaBackgroundDark,
    secondaryContainer = Color(0xFF5D2630),
    onSecondaryContainer = Color(0xFFFFEDEC),
    tertiary = ArenaGold80,
    onTertiary = ArenaBackgroundDark,
    tertiaryContainer = Color(0xFF514116),
    onTertiaryContainer = Color(0xFFFFF5CE),
    background = ArenaBackgroundDark,
    onBackground = ArenaInk80,
    surface = Color(0xFF102027),
    onSurface = ArenaInk80,
    surfaceVariant = Color(0xFF233B42),
    outline = Color(0xFF557179)
)

private val LightColorScheme = lightColorScheme(
    primary = ArenaTeal40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7F3F5),
    onPrimaryContainer = Color(0xFF062326),
    secondary = ArenaCoral40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDAD5),
    onSecondaryContainer = Color(0xFF3F0713),
    tertiary = ArenaGold40,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFEDB5),
    onTertiaryContainer = Color(0xFF281B00),
    background = ArenaBackgroundLight,
    onBackground = ArenaInk40,
    surface = Color.White,
    onSurface = ArenaInk40,
    surfaceVariant = Color(0xFFDCEAEC),
    outline = Color(0xFF8AA0A6)

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun TicTacToeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
