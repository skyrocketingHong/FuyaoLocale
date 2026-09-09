package ing.fuyaoskyrocket.applocale.ui.designsystem.holo

import androidx.annotation.DrawableRes
import ing.fuyaoskyrocket.applocale.R

/** Original ICS Light counterparts; shared assets retain their original resource. */
@DrawableRes
internal fun resolveHoloDrawableId(@DrawableRes resource: Int, darkTheme: Boolean): Int {
    if (darkTheme) return resource
    return when (resource) {
        R.drawable.holo_ics_activated_background_holo_dark -> R.drawable.holo_ics_activated_background_holo_light
        R.drawable.holo_ics_btn_default_holo_dark -> R.drawable.holo_ics_btn_default_holo_light
        R.drawable.holo_ics_btn_check_holo_dark -> R.drawable.holo_ics_btn_check_holo_light
        R.drawable.holo_ics_btn_radio_holo_dark -> R.drawable.holo_ics_btn_radio_holo_light
        R.drawable.holo_ics_edit_text_holo_dark -> R.drawable.holo_ics_edit_text_holo_light
        R.drawable.holo_ics_list_selector_holo_dark -> R.drawable.holo_ics_list_selector_holo_light
        R.drawable.holo_ics_item_background_holo_dark -> R.drawable.holo_ics_item_background_holo_light
        R.drawable.holo_ics_progress_horizontal_holo_dark -> R.drawable.holo_ics_progress_horizontal_holo_light
        R.drawable.holo_ics_switch_track_holo_dark -> R.drawable.holo_ics_switch_track_holo_light
        R.drawable.holo_ics_switch_inner_holo_dark -> R.drawable.holo_ics_switch_inner_holo_light
        R.drawable.holo_ics_ab_solid_dark_holo -> R.drawable.holo_ics_ab_solid_light_holo
        R.drawable.holo_ics_ab_stacked_solid_dark_holo -> R.drawable.holo_ics_ab_stacked_solid_light_holo
        R.drawable.holo_ics_ab_bottom_solid_dark_holo -> R.drawable.holo_ics_ab_bottom_solid_light_holo
        R.drawable.holo_ics_cab_background_top_holo_dark -> R.drawable.holo_ics_cab_background_top_holo_light
        R.drawable.holo_ics_menu_dropdown_panel_holo_dark -> R.drawable.holo_ics_menu_dropdown_panel_holo_light
        R.drawable.holo_ics_dialog_full_holo_dark -> R.drawable.holo_ics_dialog_full_holo_light
        R.drawable.holo_ics_list_divider_holo_dark -> R.drawable.holo_ics_list_divider_holo_light
        R.drawable.holo_ics_scrollbar_handle_holo_dark -> R.drawable.holo_ics_scrollbar_handle_holo_light
        R.drawable.holo_ics_ic_menu_search_holo_dark -> R.drawable.holo_ics_ic_menu_search_holo_light
        R.drawable.holo_ics_ic_menu_moreoverflow_normal_holo_dark -> R.drawable.holo_ics_ic_menu_moreoverflow_normal_holo_light
        R.drawable.holo_ics_ic_ab_back_holo_dark -> R.drawable.holo_ics_ic_ab_back_holo_light
        R.drawable.holo_ics_search_textfield_holo_dark -> R.drawable.holo_ics_textfield_searchview_holo_light
        R.drawable.holo_ics_textfield_searchview_holo_dark -> R.drawable.holo_ics_textfield_searchview_holo_light
        R.drawable.holo_ics_spinner_ab_holo_dark -> R.drawable.holo_ics_spinner_ab_holo_light
        R.drawable.holo_ics_list_section_divider_holo_dark -> R.drawable.holo_ics_list_section_divider_holo_light
        R.drawable.holo_ics_divider_vertical_holo_dark -> R.drawable.holo_ics_divider_vertical_holo_light
        else -> resource
    }
}
