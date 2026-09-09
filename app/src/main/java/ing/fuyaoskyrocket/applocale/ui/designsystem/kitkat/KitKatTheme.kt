package ing.fuyaoskyrocket.applocale.ui.designsystem.kitkat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppPalette
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppTextStyles
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloControlColors
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloControlState
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloMetrics
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloTextColors
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.LocalHoloContentColor
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.LocalHoloControlColors
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.LocalHoloDarkTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.LocalHoloDrawableResolver
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.LocalHoloFontFamily
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.LocalHoloListPadding
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.LocalHoloMetrics
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.LocalHoloTextColors
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.LocalHoloTextStyle
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.LocalHoloWindowBackground
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.holoColorList
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.resolve

import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemeStyle
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiMetrics
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiThemeValues
import ing.fuyaoskyrocket.applocale.ui.designsystem.LocalAppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloPresentationPolicy

/** Full app Provider; business pages use the common Holo layout with 4.4 assets. */
@Composable
fun KitKatAppTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    KitKatWidgetTheme(darkTheme) { palette, textStyles, metrics ->
        CompositionLocalProvider(
            LocalAppUiTheme provides AppUiThemeValues(
                palette = palette,
                textStyles = textStyles,
                style = AppThemeStyle.HOLO_KITKAT,
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
        ) { content() }
    }
}

/** Roboto as shipped at the pinned 4.4 commit, separate from the 4.0 font files. */
internal val KitKatFontFamily = FontFamily(
    Font(R.font.kitkat_roboto_regular),
    Font(R.font.kitkat_roboto_bold, FontWeight.Bold),
)

/** Versioned values; shared Holo layout consumes these without inferring an era. */
@Composable
internal fun KitKatWidgetTheme(
    darkTheme: Boolean,
    content: @Composable (AppPalette, AppTextStyles, HoloMetrics) -> Unit,
) {
    val context = LocalContext.current
    val fontFamily = ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.legacyFontFamily(KitKatFontFamily)
    val primary = holoColorList(context, if (darkTheme) R.color.kitkat_primary_text_holo_dark else R.color.kitkat_primary_text_holo_light)
    val secondary = holoColorList(context, if (darkTheme) R.color.kitkat_secondary_text_holo_dark else R.color.kitkat_secondary_text_holo_light)
    val enabled = HoloControlState()
    val disabled = HoloControlState(enabled = false)
    val textColors = HoloTextColors(
        primary = Color(enabled.resolve(primary)),
        primaryDisabled = Color(disabled.resolve(primary)),
        secondary = Color(enabled.resolve(secondary)),
        secondaryDisabled = Color(disabled.resolve(secondary)),
        hint = colorResource(if (darkTheme) R.color.kitkat_hint_foreground_holo_dark else R.color.kitkat_hint_foreground_holo_light),
    )
    val accent = if (darkTheme) Color.White else Color(0xff444444)
    val activated = if (darkTheme) Color(0xffcccccc) else Color(0xff666666)
    val background = colorResource(if (darkTheme) R.color.kitkat_background_holo_dark else R.color.kitkat_background_holo_light)
    val palette = AppPalette(
        background = background,
        foreground = textColors.primary,
        surface = background,
        surfaceContent = textColors.primary,
        secondarySurface = background,
        secondaryContent = textColors.secondary,
        muted = textColors.secondary,
        accent = accent,
        onAccent = if (darkTheme) Color.Black else Color.White,
        selected = accent.copy(alpha = .18f),
        onSelected = textColors.primary,
        quietContainer = background,
        quietContent = textColors.secondary,
        error = colorResource(R.color.kitkat_holo_red_light),
        onError = Color.Black,
        divider = if (darkTheme) Color(0x26ffffff) else Color(0x33000000),
    )
    val textStyles = AppTextStyles(
        pageTitle = TextStyle(fontFamily = fontFamily, fontSize = 18.sp),
        itemTitle = TextStyle(fontFamily = fontFamily, fontSize = 18.sp),
        body = TextStyle(fontFamily = fontFamily, fontSize = 16.sp),
        metadata = TextStyle(fontFamily = fontFamily, fontSize = 14.sp),
        label = TextStyle(fontFamily = fontFamily, fontSize = 14.sp),
    )
    // Source: Widget.Holo.*, Theme.Holo, and the qualifier-aware 4.4 dimens.
    val metrics = HoloMetrics(
        actionBarHeight = dimensionResource(R.dimen.kitkat_action_bar_default_height),
        actionBarTitleTextSize = with(LocalDensity.current) { dimensionResource(R.dimen.kitkat_action_bar_title_text_size).toSp() },
        actionBarSubtitleTextSize = with(LocalDensity.current) { dimensionResource(R.dimen.kitkat_action_bar_subtitle_text_size).toSp() },
        actionBarTabPaddingHorizontal = 16.dp,
        actionButtonMinWidth = dimensionResource(R.dimen.kitkat_action_button_min_width),
        buttonMinHeight = 48.dp,
        buttonMinWidth = 64.dp,
        listPreferredItemHeight = 64.dp,
        listPreferredItemHeightSmall = 48.dp,
        listPreferredItemHeightLarge = 80.dp,
        listPreferredItemPaddingHorizontal = 8.dp,
        dialogListPreferredItemPaddingHorizontal = 16.dp,
        switchMinWidth = 96.dp,
        switchThumbTextPadding = 12.dp,
        switchPadding = 16.dp,
        alertDialogTitleHeight = dimensionResource(R.dimen.kitkat_alert_dialog_title_height),
        alertDialogButtonBarHeight = dimensionResource(R.dimen.kitkat_alert_dialog_button_bar_height),
    )
    CompositionLocalProvider(
        ing.fuyaoskyrocket.applocale.ui.designsystem.holo.LocalHoloDrawableTransform provides ::applyKitKatControlPalette,
        ing.fuyaoskyrocket.applocale.ui.designsystem.holo.LocalHoloAssetResolver provides ::resolveKitKatAsset,
        LocalHoloDrawableResolver provides ::resolveKitKatDrawableId,
        LocalHoloWindowBackground provides if (darkTheme) R.drawable.kitkat_background_holo_dark else R.drawable.kitkat_background_holo_light,
        LocalHoloFontFamily provides fontFamily,
        LocalHoloDarkTheme provides darkTheme,
        LocalHoloMetrics provides metrics,
        LocalHoloListPadding provides 8.dp,
        LocalHoloControlColors provides HoloControlColors(
            pressedHighlight = colorResource(R.color.kitkat_holo_gray_light),
            focusedHighlight = activated,
            activatedHighlight = activated,
            longPressedHighlight = colorResource(R.color.kitkat_holo_gray_bright),
            multiSelectHighlight = activated,
        ),
        LocalHoloTextColors provides textColors,
        LocalHoloContentColor provides textColors.primary,
        LocalHoloTextStyle provides textStyles.body,
    ) { content(palette, textStyles, metrics) }
}
