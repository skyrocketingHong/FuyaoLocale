package ing.fuyaoskyrocket.applocale.ui.designsystem.holo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppControlFamily
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppNavigationPresentation
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppPickerPresentation
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppPresentationPolicy
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemeStyle
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiMetrics
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiThemeValues
import ing.fuyaoskyrocket.applocale.ui.designsystem.LocalAppUiTheme

/**
 * The Holo ICS presentation contract: top tabs + Action Bar navigation,
 * floating list dialogs, backdrop effects disabled while active (the saved
 * modern preferences are untouched — this policy only gates rendering), and
 * selection modes surface through a contextual action bar.
 */
val HoloPresentationPolicy = AppPresentationPolicy(
    controls = AppControlFamily.Holo,
    navigation = AppNavigationPresentation.ActionBarTabs,
    picker = AppPickerPresentation.HoloDialog,
    supportsBackdropEffects = false,
    showsModernEffectSettings = false,
    usesContextualActions = true,
    continuousLists = true,
)

/** Era-specific control colours and resolved text colour roles. */
val LocalHoloControlColors = staticCompositionLocalOf { HoloControlColors.Default }
val LocalHoloTextColors = staticCompositionLocalOf { HoloTextColors.Default }
val LocalHoloDarkTheme = staticCompositionLocalOf { true }
val LocalHoloListPadding = staticCompositionLocalOf { 8.dp }

/** The Holo widget metrics resolved from the qualifier-aware holo_ics dimens. */
val LocalHoloMetrics = staticCompositionLocalOf<HoloMetrics> {
    error("HoloMetrics is not installed; wrap content in HoloAppTheme first.")
}

/**
 * The Holo content roles: default content colour (bright foreground) and the
 * base 16sp TextAppearance, playing the role MaterialTheme's
 * LocalContentColor/LocalTextStyle play for that backend. Installed only by
 * [HoloAppTheme]; holo components resolve their explicit colours from
 * [LocalHoloTextColors] and fall back to these for unspecified content.
 */
val LocalHoloContentColor = staticCompositionLocalOf { Color(0xfff3f3f3) }
val LocalHoloTextStyle = staticCompositionLocalOf {
    TextStyle.Default
}

@Composable
private fun holoMetrics(): HoloMetrics = HoloMetrics(
    actionBarHeight = dimensionResource(R.dimen.holo_ics_action_bar_height),
    actionBarTitleTextSize = with(LocalDensity.current) {
        dimensionResource(R.dimen.holo_ics_action_bar_title_text_size).toSp()
    },
    actionBarSubtitleTextSize = with(LocalDensity.current) {
        dimensionResource(R.dimen.holo_ics_action_bar_subtitle_text_size).toSp()
    },
    actionBarTabPaddingHorizontal = dimensionResource(R.dimen.holo_ics_action_bar_tab_padding),
    actionButtonMinWidth = dimensionResource(R.dimen.holo_ics_action_button_min_width),
    buttonMinHeight = dimensionResource(R.dimen.holo_ics_button_min_height),
    buttonMinWidth = dimensionResource(R.dimen.holo_ics_button_min_width),
    listPreferredItemHeight = dimensionResource(R.dimen.holo_ics_list_preferred_item_height),
    listPreferredItemHeightSmall = dimensionResource(R.dimen.holo_ics_list_preferred_item_height_small),
    listPreferredItemHeightLarge = dimensionResource(R.dimen.holo_ics_list_preferred_item_height_large),
    listPreferredItemPaddingHorizontal = dimensionResource(R.dimen.holo_ics_list_preferred_item_padding),
    dialogListPreferredItemPaddingHorizontal =
        dimensionResource(R.dimen.holo_ics_dialog_list_preferred_item_padding),
    switchMinWidth = dimensionResource(R.dimen.holo_ics_switch_min_width),
    switchThumbTextPadding = dimensionResource(R.dimen.holo_ics_switch_thumb_text_padding),
    switchPadding = dimensionResource(R.dimen.holo_ics_switch_padding),
    alertDialogTitleHeight = dimensionResource(R.dimen.holo_ics_alert_dialog_title_height),
    alertDialogButtonBarHeight = dimensionResource(R.dimen.holo_ics_alert_dialog_button_bar_height),
)

/**
 * The Holo ICS backend with the original Light and Dark asset families. Installs NO
 * MaterialTheme and NO MiuixTheme: holo components draw through the drawable
 * bridge and these locals; only neutral Compose foundation (layout, text,
 * graphics, semantics) is used underneath.
 */
@Composable
fun HoloAppTheme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    val metrics = holoMetrics()
    val fontFamily = ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.legacyFontFamily(HoloFontFamily)
    val textStyles = holoTextStyles(fontFamily)
    val textColors = if (darkTheme) HoloTextColors.Default else HoloTextColors.Light
    CompositionLocalProvider(
        ing.fuyaoskyrocket.applocale.ui.designsystem.holo.LocalHoloAssetResolver provides ::resolveIcsAsset,
        LocalHoloDrawableResolver provides ::resolveHoloDrawableId,
        LocalHoloWindowBackground provides null,
        LocalHoloFontFamily provides fontFamily,
        LocalHoloDarkTheme provides darkTheme,
        LocalHoloMetrics provides metrics,
        LocalHoloControlColors provides HoloControlColors.Default,
        LocalHoloTextColors provides textColors,
        LocalHoloContentColor provides textColors.primary,
        LocalHoloTextStyle provides textStyles.body,
        LocalAppUiTheme provides AppUiThemeValues(
            palette = holoAppPalette(darkTheme),
            textStyles = textStyles,
            style = AppThemeStyle.HOLO_ICS,
            metrics = AppUiMetrics(
                listPreferredItemHeightSmall = metrics.listPreferredItemHeightSmall,
                listPreferredItemHeight = metrics.listPreferredItemHeight,
                listPreferredItemHeightLarge = metrics.listPreferredItemHeightLarge,
                listPreferredItemPaddingHorizontal = metrics.listPreferredItemPaddingHorizontal,
                dialogListPreferredItemPaddingHorizontal = metrics.dialogListPreferredItemPaddingHorizontal,
                toolbarHeight = metrics.actionBarHeight, tabHeight = metrics.actionBarHeight,
            ),
            policy = HoloPresentationPolicy,
        ),
    ) {
        content()
    }
}
