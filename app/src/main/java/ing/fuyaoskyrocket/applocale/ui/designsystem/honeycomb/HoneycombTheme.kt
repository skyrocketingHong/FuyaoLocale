package ing.fuyaoskyrocket.applocale.ui.designsystem.honeycomb

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.*
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.*

/** Exact SDK 3.0 r02 fonts. Their bytes also match the separately acquired 2.0 files. */
internal val HoneycombFontFamily = FontFamily(
    Font(R.font.honeycomb_droid_sans), Font(R.font.honeycomb_droid_sans_bold, FontWeight.Bold),
)

@Composable
internal fun HoneycombWidgetTheme(darkTheme: Boolean, content: @Composable (AppPalette, AppTextStyles, HoloMetrics) -> Unit) {
    val context = LocalContext.current
    val fontFamily = ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.legacyFontFamily(HoneycombFontFamily)
    val primary = holoColorList(context, if (darkTheme) R.color.honeycomb_primary_text_holo_dark else R.color.honeycomb_primary_text_holo_light)
    val secondary = holoColorList(context, if (darkTheme) R.color.honeycomb_secondary_text_holo_dark else R.color.honeycomb_secondary_text_holo_light)
    val disabled = HoloControlState(enabled = false)
    val textColors = HoloTextColors(
        primary = Color(HoloControlState().resolve(primary)), primaryDisabled = Color(disabled.resolve(primary)),
        secondary = Color(HoloControlState().resolve(secondary)), secondaryDisabled = Color(disabled.resolve(secondary)),
        hint = colorResource(if (darkTheme) R.color.honeycomb_hint_foreground_holo_dark else R.color.honeycomb_hint_foreground_holo_light),
    )
    val background = colorResource(if (darkTheme) R.color.honeycomb_background_holo_dark else R.color.honeycomb_background_holo_light)
    val link = colorResource(if (darkTheme) R.color.honeycomb_link_text_holo_dark else R.color.honeycomb_link_text_holo_light)
    val selection = colorResource(if (darkTheme) R.color.honeycomb_highlighted_text_holo_dark else R.color.honeycomb_highlighted_text_holo_light)
    val palette = AppPalette(
        background, textColors.primary, background, textColors.primary, background, textColors.secondary,
        textColors.secondary, link, background, selection, textColors.primary,
        background, textColors.secondary,
        if (darkTheme) Color(0xffff8080) else Color(0xffb00000), background,
        if (darkTheme) Color(0x26ffffff) else Color(0x33000000),
    )
    fun text(size: Int) = TextStyle(fontFamily = fontFamily, fontSize = size.sp,
        platformStyle = PlatformTextStyle(includeFontPadding = true))
    val type = AppTextStyles(text(18), text(22), text(16), text(14), text(14))
    // API 11 Theme.Holo + simple_list_item_2/preference + original widget styles.
    val metrics = HoloMetrics(
        actionBarHeight = 56.dp, actionBarTitleTextSize = 18.sp, actionBarSubtitleTextSize = 14.sp,
        actionBarTabPaddingHorizontal = 16.dp, actionButtonMinWidth = 64.dp,
        buttonMinHeight = 48.dp, buttonMinWidth = 64.dp,
        listPreferredItemHeight = 64.dp, listPreferredItemHeightSmall = 64.dp, listPreferredItemHeightLarge = 64.dp,
        listPreferredItemPaddingHorizontal = 6.dp, dialogListPreferredItemPaddingHorizontal = 16.dp,
        switchMinWidth = 96.dp, switchThumbTextPadding = 12.dp, switchPadding = 16.dp,
        alertDialogTitleHeight = 60.dp, alertDialogButtonBarHeight = 54.dp,
        actionButtonPaddingHorizontal = 16.dp, actionBarTabTextSize = 18.sp, actionBarTabBold = false,
        indeterminateDurationMillis = 3500, inputTextSize = 18.sp, inputUsesPrimaryCursor = true,
        categoryUppercase = false, categoryPaddingHorizontal = with(LocalDensity.current) { 5.sp.toDp() },
        preferencePaddingHorizontal = 15.dp, borderlessSmallTextSize = 18.sp,
        menuTextSize = 18.sp,
    )
    CompositionLocalProvider(
        LocalHoloAssetResolver provides ::resolveHoneycombAsset,
        LocalHoloWindowBackground provides if (darkTheme) R.drawable.honeycomb_background_holo_dark else R.drawable.honeycomb_background_holo_light,
        LocalHoloDarkTheme provides darkTheme, LocalHoloFontFamily provides fontFamily,
        LocalHoloMetrics provides metrics, LocalHoloListPadding provides 6.dp,
        LocalHoloTextColors provides textColors, LocalHoloContentColor provides textColors.primary,
        LocalHoloTextStyle provides type.body,
        LocalHoloControlColors provides HoloControlColors(selection, selection, selection, selection, selection),
    ) { content(palette, type, metrics) }
}

@Composable
internal fun HoneycombAppTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    HoneycombWidgetTheme(darkTheme) { palette, type, metrics ->
        CompositionLocalProvider(LocalAppUiTheme provides AppUiThemeValues(
            palette, type, AppThemeStyle.HOLO_HONEYCOMB,
            AppUiMetrics(metrics.listPreferredItemHeightSmall, metrics.listPreferredItemHeight,
                metrics.listPreferredItemHeightLarge, metrics.listPreferredItemPaddingHorizontal, 16.dp,
                toolbarHeight = metrics.actionBarHeight, tabHeight = metrics.actionBarHeight),
            HoloPresentationPolicy.copy(picker = AppPickerPresentation.HoneycombDialog, toggleUsesCheckbox = true),
        ), content = content)
    }
}
