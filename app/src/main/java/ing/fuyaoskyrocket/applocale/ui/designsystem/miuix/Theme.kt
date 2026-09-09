package ing.fuyaoskyrocket.applocale.ui.designsystem.miuix

import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontWeight
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppPalette
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppTextStyles
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppControlFamily
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemeStyle
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiMetrics
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiThemeValues
import ing.fuyaoskyrocket.applocale.ui.designsystem.LocalAppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.material.ModernPresentationPolicy
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.Colors as MiuixColors
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

/**
 * Miuix backend theme installation. Uses the controller overload because it also
 * provides the miuix LocalContentColor and LocalColorSchemeMode; the colors-only
 * overload leaves LocalContentColor at its black default, which breaks miuix icon
 * tints in dark mode. The neutral [LocalAppUiTheme] roles are derived from
 * MiuixTheme inside the composition, never cached statically.
 */
@Composable
internal fun MiuixAppTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    val controller = remember(darkTheme) {
        ThemeController(
            // The host already resolved Light/Dark/System; do not override it.
            colorSchemeMode = if (darkTheme) ColorSchemeMode.Dark else ColorSchemeMode.Light,
            isDark = darkTheme,
        )
    }
    MiuixTheme(controller = controller) {
        CompositionLocalProvider(
            LocalAppUiTheme provides AppUiThemeValues(
                palette = MiuixTheme.colorScheme.toAppPalette(),
                textStyles = miuixAppTextStyles(),
                style = AppThemeStyle.MIUIX,
                metrics = AppUiMetrics.Modern.copy(toolbarHeight = 50.dp, iconButtonSize = 40.dp),
                policy = ModernPresentationPolicy.copy(controls = AppControlFamily.Miuix),
            ),
        ) {
            content()
        }
    }
}

/** Miuix source of the neutral roles, per plans/round-4/012 and round-5/020. */
internal fun MiuixColors.toAppPalette(): AppPalette = AppPalette(
    background = background,
    foreground = onBackground,
    surface = surface,
    surfaceContent = onSurface,
    secondarySurface = surfaceContainer,
    secondaryContent = onSurfaceContainer,
    muted = onSurfaceVariantSummary,
    accent = primary,
    onAccent = onPrimary,
    // Selection stays NEUTRAL in miuix (secondaryVariant); blue stays reserved
    // for primary actions and the selection check itself (round-5 020).
    selected = secondaryVariant,
    onSelected = onSecondaryVariant,
    quietContainer = secondaryVariant,
    quietContent = onSecondaryVariant,
    error = errorContainer,
    onError = onErrorContainer,
    divider = dividerLine,
)

@Composable
internal fun miuixAppTextStyles(): AppTextStyles {
    val textStyles = MiuixTheme.textStyles
    // Weight budget (round-5 020): titles SemiBold, body/metadata Normal, the
    // action/label role Medium — real hierarchy without forcing everything bold.
    return AppTextStyles(
        pageTitle = textStyles.title3.copy(fontWeight = FontWeight.SemiBold),
        itemTitle = textStyles.headline1.copy(fontWeight = FontWeight.SemiBold),
        body = textStyles.body2.copy(fontWeight = FontWeight.Normal),
        metadata = textStyles.footnote1.copy(fontWeight = FontWeight.Normal),
        label = textStyles.footnote1.copy(fontWeight = FontWeight.Medium),
    )
}
