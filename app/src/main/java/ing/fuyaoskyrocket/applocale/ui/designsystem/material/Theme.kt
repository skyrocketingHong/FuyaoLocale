package ing.fuyaoskyrocket.applocale.ui.designsystem.material

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppPalette
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppPickerPresentation
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppControlFamily
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppNavigationPresentation
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppPresentationPolicy
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppShapes
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppTextStyles
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemeStyle
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppTypography
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiMetrics
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiThemeValues
import ing.fuyaoskyrocket.applocale.ui.designsystem.DarkColorScheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.LightColorScheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.LocalAppUiTheme

/** The modern presentation contract shared by the Material You backend. */
internal val ModernPresentationPolicy = AppPresentationPolicy(
    controls = AppControlFamily.Material3,
    navigation = AppNavigationPresentation.AdaptiveBottomOrRail,
    picker = AppPickerPresentation.NativeBottomSheet,
    supportsBackdropEffects = true,
    showsModernEffectSettings = true,
    usesContextualActions = false,
)

/**
 * Material You backend theme installation: dynamic colour on Android 12+
 * (minSdk 33) with the static schemes as fallback, plus the neutral
 * [LocalAppUiTheme] roles derived from the Material scheme and typography.
 */
@Composable
internal fun MaterialAppTheme(
    darkTheme: Boolean,
    expressive: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val providedContent: @Composable () -> Unit = {
        CompositionLocalProvider(
            LocalAppUiTheme provides AppUiThemeValues(
                palette = MaterialTheme.colorScheme.toAppPalette(),
                textStyles = materialAppTextStyles(expressive),
                style = if (expressive) AppThemeStyle.MATERIAL3_EXPRESSIVE else AppThemeStyle.MATERIAL_YOU,
                metrics = AppUiMetrics.Modern.copy(toolbarHeight = if (expressive) 64.dp else 48.dp),
                policy = ModernPresentationPolicy,
            ),
        ) {
            content()
        }
    }
    if (expressive) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            typography = ExpressiveAppTypography,
            content = providedContent,
        )
    } else {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            motionScheme = MotionScheme.standard(),
            content = providedContent,
        )
    }
}

private val ExpressiveBaseTypography = Typography()
private val ExpressiveAppTypography = Typography(
    titleLarge = ExpressiveBaseTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
    titleMedium = ExpressiveBaseTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = ExpressiveBaseTypography.labelLarge.copy(fontWeight = FontWeight.Bold),
)

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
internal fun materialAppTextStyles(expressive: Boolean = false): AppTextStyles {
    val typography = MaterialTheme.typography
    return AppTextStyles(
        pageTitle = if (expressive) typography.headlineSmall.copy(fontWeight = FontWeight.Bold) else typography.titleLarge,
        itemTitle = typography.titleMedium,
        body = typography.bodyMedium,
        metadata = typography.bodySmall,
        label = typography.labelMedium,
    )
}
