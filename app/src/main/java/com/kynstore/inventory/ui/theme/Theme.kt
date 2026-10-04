package com.kynstore.inventory.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light, high-contrast White and Blue color scheme tailored for seniors
private val WhiteAndBlueColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = BlueContainer,
    onPrimaryContainer = BlueOnContainer,
    secondary = BlueSecondary,
    onSecondary = Color.White,
    secondaryContainer = BlueLightPill,
    onSecondaryContainer = BlueDark,
    tertiary = BlueDark,
    onTertiary = Color.White,
    tertiaryContainer = BlueContainer,
    onTertiaryContainer = BlueOnContainer,
    background = SoftBlueBackground,
    onBackground = TextDark,
    surface = CardWhite,
    onSurface = TextDark,
    surfaceVariant = BlueLightPill,
    onSurfaceVariant = TextSubtle,
    error = StatusRed,
    onError = Color.White,
    errorContainer = StatusRedContainer,
    onErrorContainer = StatusRed,
    outline = BlueBorder,
    outlineVariant = BlueContainer
)

@Composable
fun KYNStoreTheme(
    darkTheme: Boolean = false, // Keep high-contrast white & blue active for elders
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WhiteAndBlueColorScheme,
        typography = Typography,
        content = content
    )
}
