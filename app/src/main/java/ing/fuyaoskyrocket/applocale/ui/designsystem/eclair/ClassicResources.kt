package ing.fuyaoskyrocket.applocale.ui.designsystem.eclair

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.ThemeVariant
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.rememberLegacyDrawable

/** Classic compatibility keys resolve to the selected version's actual resources. */
@Composable
internal fun classicResource(resource: Int): Int =
    if (LocalClassicEra.current == ThemeVariant.GINGERBREAD) gingerbreadResource(resource) else resource

@Composable
internal fun rememberClassicDrawable(resource: Int) = rememberLegacyDrawable(classicResource(resource))

internal fun gingerbreadResource(resource: Int): Int = when (resource) {
    R.color.eclair_background_dark -> R.color.gingerbread_background_dark
    R.color.eclair_background_light -> R.color.gingerbread_background_light
    R.color.eclair_primary_text_dark -> R.color.gingerbread_primary_text_dark
    R.color.eclair_primary_text_light -> R.color.gingerbread_primary_text_light
    R.color.eclair_secondary_text_dark -> R.color.gingerbread_secondary_text_dark
    R.color.eclair_secondary_text_light -> R.color.gingerbread_secondary_text_light
    R.color.eclair_tab_indicator_text -> R.color.gingerbread_tab_indicator_text
    R.drawable.eclair_btn_check -> R.drawable.gingerbread_btn_check
    R.drawable.eclair_btn_default -> R.drawable.gingerbread_btn_default
    R.drawable.eclair_btn_dropdown -> R.drawable.gingerbread_btn_dropdown
    R.drawable.eclair_btn_radio -> R.drawable.gingerbread_btn_radio
    R.drawable.eclair_dark_header_dither -> R.drawable.gingerbread_dark_header_dither
    R.drawable.eclair_divider_horizontal_bright_opaque -> R.drawable.gingerbread_divider_horizontal_bright_opaque
    R.drawable.eclair_divider_horizontal_dark_opaque -> R.drawable.gingerbread_divider_horizontal_dark_opaque
    R.drawable.eclair_edit_text -> R.drawable.gingerbread_edit_text
    R.drawable.eclair_ic_menu_add -> R.drawable.gingerbread_ic_menu_add
    R.drawable.eclair_ic_menu_back -> R.drawable.gingerbread_ic_menu_back
    R.drawable.eclair_ic_menu_close_clear_cancel -> R.drawable.gingerbread_ic_menu_close_clear_cancel
    R.drawable.eclair_ic_menu_delete -> R.drawable.gingerbread_ic_menu_delete
    R.drawable.eclair_ic_menu_forward -> R.drawable.gingerbread_ic_menu_forward
    R.drawable.eclair_ic_menu_info_details -> R.drawable.gingerbread_ic_menu_info_details
    R.drawable.eclair_ic_menu_manage -> R.drawable.gingerbread_ic_menu_manage
    R.drawable.eclair_ic_menu_mapmode -> R.drawable.gingerbread_ic_menu_mapmode
    R.drawable.eclair_ic_menu_mark -> R.drawable.gingerbread_ic_menu_mark
    R.drawable.eclair_ic_menu_more -> R.drawable.gingerbread_ic_menu_more
    R.drawable.eclair_ic_menu_preferences -> R.drawable.gingerbread_ic_menu_preferences
    R.drawable.eclair_ic_menu_refresh -> R.drawable.gingerbread_ic_menu_refresh
    R.drawable.eclair_ic_menu_search -> R.drawable.gingerbread_ic_menu_search
    R.drawable.eclair_ic_menu_sort_alphabetically -> R.drawable.gingerbread_ic_menu_sort_alphabetically
    R.drawable.eclair_ic_menu_star -> R.drawable.gingerbread_ic_menu_star
    R.drawable.eclair_ic_menu_view -> R.drawable.gingerbread_ic_menu_view
    R.drawable.eclair_light_header_dither -> R.drawable.gingerbread_light_header_dither
    R.drawable.eclair_list_selector_background -> R.drawable.gingerbread_list_selector_background
    R.drawable.eclair_menu_background -> R.drawable.gingerbread_menu_background
    R.drawable.eclair_menu_selector -> R.drawable.gingerbread_menu_selector
    R.drawable.eclair_menuitem_background -> R.drawable.gingerbread_menuitem_background
    R.drawable.eclair_popup_bottom_bright -> R.drawable.gingerbread_popup_bottom_bright
    R.drawable.eclair_popup_bottom_medium -> R.drawable.gingerbread_popup_bottom_medium
    R.drawable.eclair_popup_center_dark -> R.drawable.gingerbread_popup_center_dark
    R.drawable.eclair_popup_full_bright -> R.drawable.gingerbread_popup_full_bright
    R.drawable.eclair_popup_top_dark -> R.drawable.gingerbread_popup_top_dark
    R.drawable.eclair_progress_horizontal -> R.drawable.gingerbread_progress_horizontal
    R.drawable.eclair_progress_indeterminate_horizontal -> R.drawable.gingerbread_progress_indeterminate_horizontal
    R.drawable.eclair_progress_medium -> R.drawable.gingerbread_progress_medium
    R.drawable.eclair_progress_medium_white -> R.drawable.gingerbread_progress_medium_white
    R.drawable.eclair_scrollbar_handle_vertical -> R.drawable.gingerbread_scrollbar_handle_vertical
    R.drawable.eclair_tab_indicator -> R.drawable.gingerbread_tab_indicator
    R.drawable.eclair_title_bar -> R.drawable.gingerbread_title_bar
    R.drawable.eclair_toast_frame -> R.drawable.gingerbread_toast_frame
    R.font.eclair_droid_sans -> R.font.gingerbread_droid_sans
    R.font.eclair_droid_sans_bold -> R.font.gingerbread_droid_sans_bold
    R.integer.eclair_config_shortAnimTime -> R.integer.gingerbread_config_shortAnimTime
    else -> resource // Application-owned images and explicitly versioned Froyo resources.
}

internal val classicDialogDarkFace: Color
    @Composable get() = if (LocalClassicEra.current == ThemeVariant.GINGERBREAD) Color.Black else Color(0xff494949)
internal val classicDialogButtonFace: Color
    @Composable get() = if (LocalClassicEra.current == ThemeVariant.GINGERBREAD) Color(0xff9a9a9a) else Color(0xffcbcbcb)
internal val classicMenuFace: Color
    @Composable get() = if (LocalClassicEra.current == ThemeVariant.GINGERBREAD) Color(0xff5a5a5a) else Color.White
internal val classicMenuTextResource: Int
    @Composable get() = if (LocalClassicEra.current == ThemeVariant.GINGERBREAD) R.color.eclair_primary_text_dark else R.color.eclair_primary_text_light
