package ing.fuyaoskyrocket.applocale.ui.designsystem.material

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppPalette
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppShapes
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppTextStyles
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppTypography
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiThemeValues
import ing.fuyaoskyrocket.applocale.ui.designsystem.DarkColorScheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.LightColorScheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.LocalAppUiTheme

/**
 * Material You backend theme installation: dynamic colour on Android 12+
 * (minSdk 33) with the static schemes as fallback, plus the neutral
 * [LocalAppUiTheme] roles derived from the Material scheme and typography.
 */
@Composable
internal fun MaterialAppTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
    ) {
        CompositionLocalProvider(
            LocalAppUiTheme provides AppUiThemeValues(
                palette = MaterialTheme.colorScheme.toAppPalette(),
                textStyles = materialAppTextStyles(),
            ),
        ) {
            content()
        }
    }
}

/** Material source of the neutral roles, per plans/round-4/012-native-miuix.md. */
internal fun ColorScheme.toAppPalette(): AppPalette = AppPalette(
    background = background,
    foreground = onBackground,
    surface = surface,
    surfaceContent = onSurface,
    secondarySurface = surfaceContainerLow,
    secondaryContent = onSurface,
    muted = onSurfaceVariant,
    accent = primary,
    onAccent = onPrimary,
    selected = secondaryContainer,
    onSelected = onSecondaryContainer,
    quietContainer = surfaceContainerHigh,
    quietContent = onSurfaceVariant,
    error = errorContainer,
    onError = onErrorContainer,
    divider = outlineVariant,
)

@Composable
internal fun materialAppTextStyles(): AppTextStyles {
    val typography = MaterialTheme.typography
    return AppTextStyles(
        pageTitle = typography.titleLarge,
        itemTitle = typography.titleMedium,
        body = typography.bodyMedium,
        metadata = typography.bodySmall,
        label = typography.labelMedium,
    )
}
