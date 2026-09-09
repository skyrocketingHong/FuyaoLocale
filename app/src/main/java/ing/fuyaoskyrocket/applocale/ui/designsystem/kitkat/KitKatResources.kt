package ing.fuyaoskyrocket.applocale.ui.designsystem.kitkat

import androidx.annotation.DrawableRes
import ing.fuyaoskyrocket.applocale.R

/** Fixed android-4.4_r1 resources. ICS IDs are compatibility keys, never the result. */
@DrawableRes
internal fun resolveKitKatDrawableId(@DrawableRes resource: Int, darkTheme: Boolean): Int = when (resource) {
    R.drawable.holo_ics_ab_bottom_solid_dark_holo -> if (darkTheme) R.drawable.kitkat_ab_bottom_transparent_dark_holo else R.drawable.kitkat_ab_bottom_solid_light_holo
    R.drawable.holo_ics_ab_bottom_solid_light_holo -> R.drawable.kitkat_ab_bottom_solid_light_holo
    R.drawable.holo_ics_ab_solid_dark_holo -> if (darkTheme) R.drawable.kitkat_ab_transparent_dark_holo else R.drawable.kitkat_ab_solid_light_holo
    R.drawable.holo_ics_ab_solid_light_holo -> R.drawable.kitkat_ab_solid_light_holo
    R.drawable.holo_ics_ab_solid_shadow_holo -> R.drawable.kitkat_ab_solid_shadow_holo
    R.drawable.holo_ics_ab_stacked_solid_dark_holo -> if (darkTheme) R.drawable.kitkat_ab_stacked_transparent_dark_holo else R.drawable.kitkat_ab_stacked_solid_light_holo
    R.drawable.holo_ics_ab_stacked_solid_light_holo -> R.drawable.kitkat_ab_stacked_solid_light_holo
    R.drawable.holo_ics_activated_background_holo_dark -> if (darkTheme) R.drawable.kitkat_activated_background_holo_dark else R.drawable.kitkat_activated_background_holo_light
    R.drawable.holo_ics_activated_background_holo_light -> R.drawable.kitkat_activated_background_holo_light
    R.drawable.holo_ics_btn_check_holo_dark -> if (darkTheme) R.drawable.kitkat_btn_check_holo_dark else R.drawable.kitkat_btn_check_holo_light
    R.drawable.holo_ics_btn_check_holo_light -> R.drawable.kitkat_btn_check_holo_light
    R.drawable.holo_ics_btn_default_holo_dark -> if (darkTheme) R.drawable.kitkat_btn_default_holo_dark else R.drawable.kitkat_btn_default_holo_light
    R.drawable.holo_ics_btn_default_holo_light -> R.drawable.kitkat_btn_default_holo_light
    R.drawable.holo_ics_btn_radio_holo_dark -> if (darkTheme) R.drawable.kitkat_btn_radio_holo_dark else R.drawable.kitkat_btn_radio_holo_light
    R.drawable.holo_ics_btn_radio_holo_light -> R.drawable.kitkat_btn_radio_holo_light
    R.drawable.holo_ics_cab_background_top_holo_dark -> if (darkTheme) R.drawable.kitkat_cab_background_top_holo_dark else R.drawable.kitkat_cab_background_top_holo_light
    R.drawable.holo_ics_cab_background_top_holo_light -> R.drawable.kitkat_cab_background_top_holo_light
    R.drawable.holo_ics_dialog_full_holo_dark -> if (darkTheme) R.drawable.kitkat_dialog_full_holo_dark else R.drawable.kitkat_dialog_full_holo_light
    R.drawable.holo_ics_dialog_full_holo_light -> R.drawable.kitkat_dialog_full_holo_light
    R.drawable.holo_ics_divider_vertical_holo_dark -> if (darkTheme) R.drawable.kitkat_divider_vertical_holo_dark else R.drawable.kitkat_divider_vertical_holo_light
    R.drawable.holo_ics_divider_vertical_holo_light -> R.drawable.kitkat_divider_vertical_holo_light
    R.drawable.holo_ics_edit_text_holo_dark -> if (darkTheme) R.drawable.kitkat_edit_text_holo_dark else R.drawable.kitkat_edit_text_holo_light
    R.drawable.holo_ics_edit_text_holo_light -> R.drawable.kitkat_edit_text_holo_light
    R.drawable.holo_ics_ic_ab_back_holo_dark -> if (darkTheme) R.drawable.kitkat_ic_ab_back_holo_dark else R.drawable.kitkat_ic_ab_back_holo_light
    R.drawable.holo_ics_ic_ab_back_holo_light -> R.drawable.kitkat_ic_ab_back_holo_light
    R.drawable.holo_ics_ic_clear -> R.drawable.kitkat_ic_clear
    R.drawable.holo_ics_ic_input_add -> R.drawable.kitkat_ic_input_add
    R.drawable.holo_ics_ic_menu_close_clear_cancel -> R.drawable.kitkat_ic_menu_close_clear_cancel
    R.drawable.holo_ics_ic_menu_delete -> R.drawable.kitkat_ic_menu_delete
    R.drawable.holo_ics_ic_menu_moreoverflow_normal_holo_dark -> if (darkTheme) R.drawable.kitkat_ic_menu_moreoverflow_normal_holo_dark else R.drawable.kitkat_ic_menu_moreoverflow_normal_holo_light
    R.drawable.holo_ics_ic_menu_moreoverflow_normal_holo_light -> R.drawable.kitkat_ic_menu_moreoverflow_normal_holo_light
    R.drawable.holo_ics_ic_menu_refresh -> R.drawable.kitkat_ic_menu_refresh
    R.drawable.holo_ics_ic_menu_search_holo_dark -> if (darkTheme) R.drawable.kitkat_ic_menu_search_holo_dark else R.drawable.kitkat_ic_menu_search_holo_light
    R.drawable.holo_ics_ic_menu_search_holo_light -> R.drawable.kitkat_ic_menu_search_holo_light
    R.drawable.holo_ics_item_background_holo_dark -> if (darkTheme) R.drawable.kitkat_item_background_holo_dark else R.drawable.kitkat_item_background_holo_light
    R.drawable.holo_ics_item_background_holo_light -> R.drawable.kitkat_item_background_holo_light
    R.drawable.holo_ics_list_divider_holo_dark -> if (darkTheme) R.drawable.kitkat_list_divider_holo_dark else R.drawable.kitkat_list_divider_holo_light
    R.drawable.holo_ics_list_divider_holo_light -> R.drawable.kitkat_list_divider_holo_light
    R.drawable.holo_ics_list_section_divider_holo_dark -> if (darkTheme) R.drawable.kitkat_list_section_divider_holo_dark else R.drawable.kitkat_list_section_divider_holo_light
    R.drawable.holo_ics_list_section_divider_holo_light -> R.drawable.kitkat_list_section_divider_holo_light
    R.drawable.holo_ics_list_selector_holo_dark -> if (darkTheme) R.drawable.kitkat_list_selector_holo_dark else R.drawable.kitkat_list_selector_holo_light
    R.drawable.holo_ics_list_selector_holo_light -> R.drawable.kitkat_list_selector_holo_light
    R.drawable.holo_ics_menu_dropdown_panel_holo_dark -> if (darkTheme) R.drawable.kitkat_menu_dropdown_panel_holo_dark else R.drawable.kitkat_menu_dropdown_panel_holo_light
    R.drawable.holo_ics_menu_dropdown_panel_holo_light -> R.drawable.kitkat_menu_dropdown_panel_holo_light
    R.drawable.holo_ics_progress_horizontal_holo_dark -> if (darkTheme) R.drawable.kitkat_progress_horizontal_holo_dark else R.drawable.kitkat_progress_horizontal_holo_light
    R.drawable.holo_ics_progress_horizontal_holo_light -> R.drawable.kitkat_progress_horizontal_holo_light
    R.drawable.holo_ics_progress_indeterminate_horizontal_holo -> R.drawable.kitkat_progress_indeterminate_horizontal_holo
    R.drawable.holo_ics_progress_medium_holo -> R.drawable.kitkat_progress_medium_holo
    R.drawable.holo_ics_scrollbar_handle_holo_dark -> if (darkTheme) R.drawable.kitkat_scrollbar_handle_holo_dark else R.drawable.kitkat_scrollbar_handle_holo_light
    R.drawable.holo_ics_scrollbar_handle_holo_light -> R.drawable.kitkat_scrollbar_handle_holo_light
    R.drawable.holo_ics_search_textfield_holo_dark -> if (darkTheme) R.drawable.kitkat_textfield_searchview_holo_dark else R.drawable.kitkat_textfield_searchview_holo_light
    R.drawable.holo_ics_spinner_ab_holo_dark -> if (darkTheme) R.drawable.kitkat_spinner_ab_holo_dark else R.drawable.kitkat_spinner_ab_holo_light
    R.drawable.holo_ics_spinner_ab_holo_light -> R.drawable.kitkat_spinner_ab_holo_light
    R.drawable.holo_ics_switch_inner_holo_dark -> if (darkTheme) R.drawable.kitkat_switch_inner_holo_dark else R.drawable.kitkat_switch_inner_holo_light
    R.drawable.holo_ics_switch_inner_holo_light -> R.drawable.kitkat_switch_inner_holo_light
    R.drawable.holo_ics_switch_track_holo_dark -> if (darkTheme) R.drawable.kitkat_switch_track_holo_dark else R.drawable.kitkat_switch_track_holo_light
    R.drawable.holo_ics_switch_track_holo_light -> R.drawable.kitkat_switch_track_holo_light
    R.drawable.holo_ics_tab_indicator_ab_holo -> R.drawable.kitkat_tab_indicator_ab_holo
    R.drawable.holo_ics_textfield_searchview_holo_dark -> if (darkTheme) R.drawable.kitkat_textfield_searchview_holo_dark else R.drawable.kitkat_textfield_searchview_holo_light
    R.drawable.holo_ics_textfield_searchview_holo_light -> R.drawable.kitkat_textfield_searchview_holo_light
    R.drawable.holo_ics_toast_frame_holo -> R.drawable.kitkat_toast_frame_holo
    else -> error("Unmapped KitKat widget resource: $resource")
}
