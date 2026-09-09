package ing.fuyaoskyrocket.applocale.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemeCatalog
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemePreferences
import ing.fuyaoskyrocket.applocale.ui.designsystem.ThemeGroup
import ing.fuyaoskyrocket.applocale.ui.designsystem.family
import ing.fuyaoskyrocket.applocale.ui.designsystem.LocalAppearanceRequester
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppChoicePreference
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppPreferenceOption

@Composable
fun AppThemePreference(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val requester = LocalAppearanceRequester.current
    val currentFamily = AppThemePreferences.style.family
    AppChoicePreference(
        title = stringResource(R.string.theme_family_preference),
        selected = currentFamily,
        options = AppThemeCatalog.families.map { option ->
            AppPreferenceOption(
                value = option.family,
                title = stringResource(option.titleRes),
                summary = stringResource(option.summaryRes),
                section = stringResource(
                    when (option.group) {
                        ThemeGroup.EARLY_ANDROID -> R.string.theme_group_early
                        ThemeGroup.MATERIAL -> R.string.theme_group_material
                        ThemeGroup.EXTENDED -> R.string.theme_group_extended
                    },
                ),
            )
        },
        onSelected = { family, focus ->
            val style = AppThemePreferences.preferredVariant(family)
            requester?.requestTheme(style, focus) ?: AppThemePreferences.setStyle(context, style)
        },
        modifier = modifier,
    )
    val variants = AppThemeCatalog.variants(currentFamily)
    if (variants.size > 1) AppChoicePreference(
        title = stringResource(R.string.theme_variant_preference),
        selected = AppThemePreferences.style,
        options = variants.map { option ->
            AppPreferenceOption(option.style, stringResource(option.variantTitleRes), stringResource(option.summaryRes))
        },
        onSelected = { style, focus ->
            requester?.requestTheme(style, focus) ?: AppThemePreferences.setStyle(context, style)
        },
        modifier = modifier,
    )
}
