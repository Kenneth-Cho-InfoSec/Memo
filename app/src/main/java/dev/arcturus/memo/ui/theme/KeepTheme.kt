package dev.arcturus.memo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.TextStyle

private val KeepProductSans = FontFamily(Font(dev.arcturus.memo.R.font.product_sans_regular))
private val PlatformTypography = Typography()
private fun TextStyle.withKeepFont() = copy(fontFamily = KeepProductSans)
private val KeepTypography = Typography(
    displayLarge = PlatformTypography.displayLarge.withKeepFont(),
    displayMedium = PlatformTypography.displayMedium.withKeepFont(),
    displaySmall = PlatformTypography.displaySmall.withKeepFont(),
    headlineLarge = PlatformTypography.headlineLarge.withKeepFont(),
    headlineMedium = PlatformTypography.headlineMedium.withKeepFont(),
    headlineSmall = PlatformTypography.headlineSmall.withKeepFont(),
    titleLarge = PlatformTypography.titleLarge.withKeepFont(),
    titleMedium = PlatformTypography.titleMedium.withKeepFont(),
    titleSmall = PlatformTypography.titleSmall.withKeepFont(),
    bodyLarge = PlatformTypography.bodyLarge.withKeepFont(),
    bodyMedium = PlatformTypography.bodyMedium.withKeepFont(),
    bodySmall = PlatformTypography.bodySmall.withKeepFont(),
    labelLarge = PlatformTypography.labelLarge.withKeepFont(),
    labelMedium = PlatformTypography.labelMedium.withKeepFont(),
    labelSmall = PlatformTypography.labelSmall.withKeepFont(),
)

@Composable
fun KeepTheme(dark: Boolean, content: @Composable () -> Unit) {
    val keep = if (dark) KeepDarkColors else KeepLightColors
    val scheme = if (dark) {
        darkColorScheme(
            primary = keep.fabSurface,
            onPrimary = keep.fabForeground,
            background = keep.background,
            onBackground = keep.foreground,
            surface = keep.background,
            onSurface = keep.foreground,
            surfaceVariant = keep.searchSurface,
            onSurfaceVariant = keep.secondaryForeground,
            outline = keep.divider,
            error = Color(0xFFf2b8b5),
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF0b57d0),
            onPrimary = Color.White,
            background = keep.background,
            onBackground = keep.foreground,
            surface = keep.background,
            onSurface = keep.foreground,
            surfaceVariant = keep.searchSurface,
            onSurfaceVariant = keep.secondaryForeground,
            outline = keep.divider,
            error = Color(0xFFb3261e),
        )
    }

    MaterialTheme(colorScheme = scheme, typography = KeepTypography, content = content)
}
