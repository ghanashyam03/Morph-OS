package com.morphos.app.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class WindowSizeClass(
    val widthDp: Dp,
    val heightDp: Dp
)

val WindowSizeClass.isCompact: Boolean get() = widthDp < 600.dp
val WindowSizeClass.isMedium: Boolean get() = widthDp >= 600.dp && widthDp < 840.dp

val LocalWindowSizeClass = staticCompositionLocalOf {
    WindowSizeClass(360.dp, 640.dp) // Fallback default
}

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6650A4),
    secondary = Color(0xFF625B71),
    tertiary = Color(0xFF7D5260),
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    secondary = Color(0xFFCCC2DC),
    tertiary = Color(0xFFEFB8C8),
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF1C1B1F),
    onPrimary = Color(0xFF381E72),
    onSecondary = Color(0xFF332D41),
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5)
)

@Composable
fun MorphOsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Estimate window size class from screen density & dimensions dynamically
    val displayMetrics = context.resources.displayMetrics
    val widthDp = (displayMetrics.widthPixels / displayMetrics.density).dp
    val heightDp = (displayMetrics.heightPixels / displayMetrics.density).dp
    val windowSizeClass = WindowSizeClass(widthDp, heightDp)

    CompositionLocalProvider(
        LocalWindowSizeClass provides windowSizeClass
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
