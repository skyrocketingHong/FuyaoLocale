package ing.fuyaoskyrocket.applocale.ui.designsystem.miuix

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontWeight
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppPalette
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppTextStyles
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiThemeValues
import ing.fuyaoskyrocket.applocale.ui.designsystem.LocalAppUiTheme
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
            colorSchemeMode = ColorSchemeMode.System,
            isDark = darkTheme,
        )
    }
    MiuixTheme(controller = controller) {
        CompositionLocalProvider(
            LocalAppUiTheme provides AppUiThemeValues(
                palette = MiuixTheme.colorScheme.toAppPalette(),
                textStyles = miuixAppTextStyles(),
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
