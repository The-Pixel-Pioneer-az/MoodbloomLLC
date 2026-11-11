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

private val MoodBloomLightColorScheme = lightColorScheme(
    primary = TerracottaPrimary,
    onPrimary = Color.White,
    primaryContainer = TerracottaContainer,
    onPrimaryContainer = OnTerracottaContainer,
    secondary = AgaveSecondary,
    onSecondary = Color.White,
    secondaryContainer = AgaveSecondaryContainer,
    onSecondaryContainer = OnAgaveSecondaryContainer,
    tertiary = MaizeTertiary,
    onTertiary = Color.White,
    tertiaryContainer = MaizeContainer,
    onTertiaryContainer = OnMaizeContainer,
    background = CremaBackground,
    onBackground = Color(0xFF231B17),
    surface = CremaSurface,
    onSurface = Color(0xFF231B17),
    surfaceVariant = CremaSurfaceVariant,
    onSurfaceVariant = Color(0xFF56473E),
    outline = CremaBorder,
    error = SpicyRed,
    onError = Color.White
)

private val MoodBloomDarkColorScheme = darkColorScheme(
    primary = TerracottaLight,
    onPrimary = Color(0xFF4C1002),
    primaryContainer = TerracottaDark,
    onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = AgaveSecondaryLight,
    onSecondary = Color(0xFF032119),
    secondaryContainer = AgaveSecondaryDark,
    onSecondaryContainer = Color(0xFFCFEADE),
    tertiary = GoldenCheddar,
    onTertiary = Color(0xFF381C00),
    tertiaryContainer = Color(0xFF5A3100),
    onTertiaryContainer = Color(0xFFFFEECC),
    background = DarkBackground,
    onBackground = Color(0xFFEBE3DC),
    surface = DarkSurface,
    onSurface = Color(0xFFEBE3DC),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCBC0B6),
    outline = DarkBorder,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Preserve custom branding by defaulting dynamicColor to false
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> MoodBloomDarkColorScheme
        else -> MoodBloomLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
