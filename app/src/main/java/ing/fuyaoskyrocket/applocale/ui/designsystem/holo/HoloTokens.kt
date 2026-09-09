package ing.fuyaoskyrocket.applocale.ui.designsystem.holo

import androidx.compose.animation.core.Easing
import androidx.annotation.DrawableRes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ing.fuyaoskyrocket.applocale.R

/**
 * Fixed-era Holo ICS tokens resolved from the AOSP android-4.0.4_r2.1
 * resources imported under docs/holo-ics/asset-manifest.json. Every number
 * here traces to a styles.xml/dimens.xml/colors.xml entry of that commit —
 * see the per-field notes. Nothing may be re-tuned "for taste": changes
 * require re-checking the reference XMLs.
 */

/**
 * DecelerateInterpolator with factor 2.5 (interpolator/decelerate_quint.xml)
 * — used by the dialog scale and scroll finishes.
 */
val HoloDecelerateQuint = Easing { t -> 1f - (1f - t).toDouble().pow(5.0).toFloat() }

/** DecelerateInterpolator with factor 1.5 (interpolator/decelerate_cubic.xml). */
val HoloDecelerateCubic = Easing { t -> 1f - (1f - t).toDouble().pow(3.0).toFloat() }

private fun Double.pow(e: Double): Double = Math.pow(this, e)

/** config.xml config_activityDefaultDur — dialog scale duration. */
const val HoloDialogDefaultDurMillis = 220

/** config.xml config_activityShortDur — dialog alpha duration. */
const val HoloDialogShortDurMillis = 150

/** ProgressBar default indeterminate duration (initProgressBar mDuration = 4000). */
const val HoloIndeterminateDurationMillis = 4000

/** ProgressBar MAX_LEVEL — the drawable level range driven by animations. */
const val HoloMaxLevel = 10000

/**
 * Widget metrics of the Holo backend. Values resolve from the
 * qualifier-aware holo_ics dimens so landscape (40dp) and sw600dp (56dp)
 * action bar heights come from the same resource overlay as the original.
 */
class HoloMetrics internal constructor(
    /** Widget.Holo.ActionBar height: 48dp / land 40dp / sw600dp 56dp. */
    val actionBarHeight: Dp,
    /** TextAppearance.Holo.Widget.ActionBar.Title — action_bar_title_text_size. */
    val actionBarTitleTextSize: TextUnit,
    /** action_bar_subtitle_text_size. */
    val actionBarSubtitleTextSize: TextUnit,
    /** Widget.Holo.ActionBar.TabView horizontal padding. */
    val actionBarTabPaddingHorizontal: Dp,
    /** Widget.Holo.ActionButton minWidth (action_button_min_width). */
    val actionButtonMinWidth: Dp,
    /** Widget.Holo.Button minHeight/minWidth. */
    val buttonMinHeight: Dp,
    val buttonMinWidth: Dp,
    /** Theme.Holo listPreferredItemHeight{,Small,Large}. */
    val listPreferredItemHeight: Dp,
    val listPreferredItemHeightSmall: Dp,
    val listPreferredItemHeightLarge: Dp,
    /** Theme.Holo listPreferredItemPaddingLeft/Right (8dp; dialogs override 16dp). */
    val listPreferredItemPaddingHorizontal: Dp,
    val dialogListPreferredItemPaddingHorizontal: Dp,
    /** Widget.Holo.CompoundButton.Switch switchMinWidth. */
    val switchMinWidth: Dp,
    /** Widget.Holo.CompoundButton.Switch thumbTextPadding. */
    val switchThumbTextPadding: Dp,
    /** Widget.Holo.CompoundButton.Switch switchPadding. */
    val switchPadding: Dp,
    /** alert_dialog_title_height / alert_dialog_button_bar_height. */
    val alertDialogTitleHeight: Dp,
    val alertDialogButtonBarHeight: Dp,
    val actionButtonPaddingHorizontal: Dp = 12.dp,
    val actionBarTabTextSize: TextUnit = 12.sp,
    val actionBarTabBold: Boolean = true,
    val indeterminateDurationMillis: Int = 4000,
    val inputTextSize: TextUnit = 16.sp,
    val inputUsesPrimaryCursor: Boolean = false,
    val categoryUppercase: Boolean = true,
    val categoryPaddingHorizontal: Dp = 8.dp,
    val preferencePaddingHorizontal: Dp = listPreferredItemPaddingHorizontal,
    val borderlessSmallTextSize: TextUnit = 14.sp,
    val menuTextSize: TextUnit = 16.sp,
)

/**
 * Read linked resource IDs at runtime. A const alias inlines the compile-time
 * R stub's zero value, which cannot be repaired when Android links resources.
 */
object HoloDrawables {
    @get:DrawableRes val Button: Int get() = R.drawable.holo_ics_btn_default_holo_dark
    @get:DrawableRes val Checkbox: Int get() = R.drawable.holo_ics_btn_check_holo_dark
    @get:DrawableRes val RadioButton: Int get() = R.drawable.holo_ics_btn_radio_holo_dark
    @get:DrawableRes val EditText: Int get() = R.drawable.holo_ics_edit_text_holo_dark
    @get:DrawableRes val ListSelector: Int get() = R.drawable.holo_ics_list_selector_holo_dark
    @get:DrawableRes val ItemBackground: Int get() = R.drawable.holo_ics_item_background_holo_dark
    @get:DrawableRes val TabIndicator: Int get() = R.drawable.holo_ics_tab_indicator_ab_holo
    @get:DrawableRes val ProgressMedium: Int get() = R.drawable.holo_ics_progress_medium_holo
    @get:DrawableRes val ProgressHorizontal: Int get() = R.drawable.holo_ics_progress_horizontal_holo_dark
    @get:DrawableRes val SwitchTrack: Int get() = R.drawable.holo_ics_switch_track_holo_dark
    @get:DrawableRes val SwitchThumb: Int get() = R.drawable.holo_ics_switch_inner_holo_dark
    @get:DrawableRes val ActionBar: Int get() = R.drawable.holo_ics_ab_solid_dark_holo
    @get:DrawableRes val ActionBarStacked: Int get() = R.drawable.holo_ics_ab_stacked_solid_dark_holo
    @get:DrawableRes val ActionBarBottom: Int get() = R.drawable.holo_ics_ab_bottom_solid_dark_holo
    @get:DrawableRes val ActionBarShadow: Int get() = R.drawable.holo_ics_ab_solid_shadow_holo
    @get:DrawableRes val CabBackground: Int get() = R.drawable.holo_ics_cab_background_top_holo_dark
    @get:DrawableRes val MenuPanel: Int get() = R.drawable.holo_ics_menu_dropdown_panel_holo_dark
    @get:DrawableRes val ToastFrame: Int get() = R.drawable.holo_ics_toast_frame_holo
    @get:DrawableRes val DialogFrame: Int get() = R.drawable.holo_ics_dialog_full_holo_dark
    @get:DrawableRes val Divider: Int get() = R.drawable.holo_ics_list_divider_holo_dark
    @get:DrawableRes val ScrollbarHandle: Int get() = R.drawable.holo_ics_scrollbar_handle_holo_dark
    @get:DrawableRes val Clear: Int get() = R.drawable.holo_ics_ic_clear
}

/** Named framework glyph assets (AppSymbol — the holo backend resolves by name). */
object HoloSymbolDrawables {
    @get:DrawableRes val Search: Int get() = R.drawable.holo_ics_ic_menu_search_holo_dark
    @get:DrawableRes val Overflow: Int get() = R.drawable.holo_ics_ic_menu_moreoverflow_normal_holo_dark
    @get:DrawableRes val Back: Int get() = R.drawable.holo_ics_ic_ab_back_holo_dark
    @get:DrawableRes val Close: Int get() = R.drawable.holo_ics_ic_menu_close_clear_cancel
    @get:DrawableRes val Refresh: Int get() = R.drawable.holo_ics_ic_menu_refresh
    @get:DrawableRes val Delete: Int get() = R.drawable.holo_ics_ic_menu_delete
    @get:DrawableRes val Add: Int get() = R.drawable.holo_ics_ic_input_add
}
