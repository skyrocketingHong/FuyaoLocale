package ing.fuyaoskyrocket.applocale.ui.screen.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.data.preferences.AppUserPreferences
import ing.fuyaoskyrocket.applocale.data.preferences.BooleanPreference
import ing.fuyaoskyrocket.applocale.ui.components.AppColorModePreference
import ing.fuyaoskyrocket.applocale.ui.components.AppLanguagePreference
import ing.fuyaoskyrocket.applocale.ui.components.AppThemePreference
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemePreferences
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.LocalAppearanceRequester
import ing.fuyaoskyrocket.applocale.ui.designsystem.LocalUserPreferences
import ing.fuyaoskyrocket.applocale.ui.designsystem.isEffectRenderingSupported
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppPreferenceCategory

@Composable
internal fun SettingsSection() {
    val context = LocalContext.current
    val requester = LocalAppearanceRequester.current
    val themeSupportsEffects = AppUiTheme.policy.supportsBackdropEffects
    val deviceSupportsEffects = isEffectRenderingSupported()
    val effectsEnabled = themeSupportsEffects && deviceSupportsEffects
    val unavailableSummary = when {
        !themeSupportsEffects -> stringResource(R.string.visual_effect_theme_unavailable)
        !deviceSupportsEffects -> stringResource(R.string.visual_effect_unavailable)
        else -> null
    }
    AppPreferenceCategory(stringResource(R.string.settings_appearance))
    AppLanguagePreference(Modifier.fillMaxWidth())
    AppThemePreference(Modifier.fillMaxWidth())
    AppColorModePreference(Modifier.fillMaxWidth())
    UserPreference(BooleanPreference.SystemFontForRetro, R.string.settings_system_font, R.string.settings_system_font_summary)
    EffectPreference(
        title = stringResource(R.string.more_blur),
        summary = unavailableSummary ?: stringResource(R.string.more_blur_summary),
        checked = AppThemePreferences.moreBlur,
        enabled = effectsEnabled,
        onChange = { value ->
            requester?.requestMoreBlur(value) ?: AppThemePreferences.setMoreBlur(context, value)
        },
    )
    EffectPreference(
        title = stringResource(R.string.theme_liquid_glass_navigation_bar),
        summary = unavailableSummary ?: stringResource(R.string.theme_liquid_glass_navigation_bar_summary),
        checked = AppThemePreferences.liquidGlassNavigationBar,
        enabled = effectsEnabled,
        onChange = { value ->
            requester?.requestGlass(value) ?: AppThemePreferences.setLiquidGlassNavigationBar(context, value)
        },
    )
    AppPreferenceCategory(stringResource(R.string.settings_app_list))
    UserPreference(BooleanPreference.ShowSystemApps, R.string.settings_system_apps, R.string.settings_system_apps_summary)
    UserPreference(BooleanPreference.ShowPackageNames, R.string.settings_package_names, R.string.settings_package_names_summary)
    UserPreference(BooleanPreference.ShowAppTypes, R.string.settings_app_types, R.string.settings_app_types_summary)
    AppPreferenceCategory(stringResource(R.string.settings_language_display))
    UserPreference(BooleanPreference.ShowRegionFlags, R.string.settings_region_flags, R.string.settings_region_flags_summary)
    AppPreferenceCategory(stringResource(R.string.settings_navigation))
    UserPreference(BooleanPreference.SwipeBetweenPages, R.string.settings_swipe_pages, R.string.settings_swipe_pages_summary)
    UserPreference(BooleanPreference.DoubleTapToTop, R.string.settings_double_tap, R.string.settings_double_tap_summary)
    UserPreference(BooleanPreference.RememberLastPage, R.string.settings_remember_page, R.string.settings_remember_page_summary)
}

@Composable
private fun UserPreference(setting: BooleanPreference, title: Int, summary: Int) {
    EffectPreference(
        title = stringResource(title),
        summary = stringResource(summary),
        checked = LocalUserPreferences.current.value(setting),
        enabled = true,
        onChange = { AppUserPreferences.set(setting, it) },
    )
}

@Composable
private fun EffectPreference(
    title: String,
    summary: String,
    checked: Boolean,
    enabled: Boolean,
    onChange: (Boolean) -> Unit,
) {
    ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppBooleanPreference(title, summary, checked, enabled, onChange)
}
