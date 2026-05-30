package com.gestorconecta2.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Esquema de colores para tema claro
private val LightColorScheme = lightColorScheme(
    primary = NavyBlue,
    onPrimary = Color.White,
    primaryContainer = NavyBlueLight,
    onPrimaryContainer = Color.White,
    secondary = AccentBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDBE7FF),
    onSecondaryContainer = NavyBlue,
    tertiary = AccentGreen,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD1FDE6),
    onTertiaryContainer = Color(0xFF003824),
    error = AccentRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454E),
    outline = Color(0xFF7A757F)
)

// Esquema de colores para tema oscuro
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBAC6FF),
    onPrimary = NavyBlue,
    primaryContainer = NavyBlueDark,
    onPrimaryContainer = Color(0xFFBAC6FF),
    secondary = Color(0xFFBAC6FF),
    onSecondary = NavyBlue,
    secondaryContainer = NavyBlueDark,
    onSecondaryContainer = Color(0xFFBAC6FF),
    tertiary = Color(0xFF80D8A8),
    onTertiary = Color(0xFF003824),
    tertiaryContainer = Color(0xFF005236),
    onTertiaryContainer = Color(0xFF80D8A8),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99)
)

/**
 * Tema principal de la aplicación.
 * Soporta modo claro y oscuro, siguiendo la preferencia del sistema o manual.
 */
@Composable
fun GestorConecta2Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Desactivado para mantener colores de marca
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            
            // Configurar color de barra de estado
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            
            // Configurar color de barra de navegación
            window.navigationBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
