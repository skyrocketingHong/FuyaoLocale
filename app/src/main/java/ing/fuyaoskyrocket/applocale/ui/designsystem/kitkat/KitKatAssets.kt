package ing.fuyaoskyrocket.applocale.ui.designsystem.kitkat

import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloAsset

/** Original 4.4 resources resolved directly from semantic widget names. */
internal fun resolveKitKatAsset(asset: HoloAsset, darkTheme: Boolean): Int = when (asset) {
    HoloAsset.Button -> if (darkTheme) R.drawable.kitkat_btn_default_holo_dark else R.drawable.kitkat_btn_default_holo_light
    HoloAsset.Checkbox -> if (darkTheme) R.drawable.kitkat_btn_check_holo_dark else R.drawable.kitkat_btn_check_holo_light
    HoloAsset.RadioButton -> if (darkTheme) R.drawable.kitkat_btn_radio_holo_dark else R.drawable.kitkat_btn_radio_holo_light
    HoloAsset.EditText -> if (darkTheme) R.drawable.kitkat_edit_text_holo_dark else R.drawable.kitkat_edit_text_holo_light
    HoloAsset.ListSelector -> if (darkTheme) R.drawable.kitkat_list_selector_holo_dark else R.drawable.kitkat_list_selector_holo_light
    HoloAsset.ItemBackground -> if (darkTheme) R.drawable.kitkat_item_background_holo_dark else R.drawable.kitkat_item_background_holo_light
    HoloAsset.TabIndicator -> R.drawable.kitkat_tab_indicator_ab_holo
    HoloAsset.ProgressMedium -> R.drawable.kitkat_progress_medium_holo
    HoloAsset.ProgressHorizontal -> if (darkTheme) R.drawable.kitkat_progress_horizontal_holo_dark else R.drawable.kitkat_progress_horizontal_holo_light
    HoloAsset.SwitchTrack -> if (darkTheme) R.drawable.kitkat_switch_track_holo_dark else R.drawable.kitkat_switch_track_holo_light
    HoloAsset.SwitchThumb -> if (darkTheme) R.drawable.kitkat_switch_inner_holo_dark else R.drawable.kitkat_switch_inner_holo_light
    HoloAsset.ActionBar -> if (darkTheme) R.drawable.kitkat_ab_solid_dark_holo else R.drawable.kitkat_ab_solid_light_holo
    HoloAsset.ActionBarStacked -> if (darkTheme) R.drawable.kitkat_ab_stacked_solid_dark_holo else R.drawable.kitkat_ab_stacked_solid_light_holo
    HoloAsset.ActionBarBottom -> if (darkTheme) R.drawable.kitkat_ab_bottom_solid_dark_holo else R.drawable.kitkat_ab_bottom_solid_light_holo
    HoloAsset.ActionBarShadow -> R.drawable.kitkat_ab_solid_shadow_holo
    HoloAsset.CabBackground -> if (darkTheme) R.drawable.kitkat_cab_background_top_holo_dark else R.drawable.kitkat_cab_background_top_holo_light
    HoloAsset.MenuPanel -> if (darkTheme) R.drawable.kitkat_menu_dropdown_panel_holo_dark else R.drawable.kitkat_menu_dropdown_panel_holo_light
    HoloAsset.ToastFrame -> R.drawable.kitkat_toast_frame_holo
    HoloAsset.DialogFrame -> if (darkTheme) R.drawable.kitkat_dialog_full_holo_dark else R.drawable.kitkat_dialog_full_holo_light
    HoloAsset.Divider -> if (darkTheme) R.drawable.kitkat_list_divider_holo_dark else R.drawable.kitkat_list_divider_holo_light
    HoloAsset.ScrollbarHandle -> if (darkTheme) R.drawable.kitkat_scrollbar_handle_holo_dark else R.drawable.kitkat_scrollbar_handle_holo_light
    HoloAsset.Clear -> R.drawable.kitkat_ic_clear
    HoloAsset.Search -> if (darkTheme) R.drawable.kitkat_ic_menu_search_holo_dark else R.drawable.kitkat_ic_menu_search_holo_light
    HoloAsset.Overflow -> if (darkTheme) R.drawable.kitkat_ic_menu_moreoverflow_normal_holo_dark else R.drawable.kitkat_ic_menu_moreoverflow_normal_holo_light
    HoloAsset.Back -> if (darkTheme) R.drawable.kitkat_ic_ab_back_holo_dark else R.drawable.kitkat_ic_ab_back_holo_light
    HoloAsset.CabClose -> R.drawable.kitkat_ic_menu_close_clear_cancel
    HoloAsset.Close -> R.drawable.kitkat_ic_menu_close_clear_cancel
    HoloAsset.Refresh -> R.drawable.kitkat_ic_menu_refresh
    HoloAsset.Delete -> R.drawable.kitkat_ic_menu_delete
    HoloAsset.Add -> R.drawable.kitkat_ic_input_add
    HoloAsset.ActivatedBackground -> if (darkTheme) R.drawable.kitkat_activated_background_holo_dark else R.drawable.kitkat_activated_background_holo_light
    HoloAsset.ProgressIndeterminateHorizontal -> R.drawable.kitkat_progress_indeterminate_horizontal_holo
    HoloAsset.SearchField -> if (darkTheme) R.drawable.kitkat_textfield_searchview_holo_dark else R.drawable.kitkat_textfield_searchview_holo_light
    HoloAsset.Spinner -> if (darkTheme) R.drawable.kitkat_spinner_ab_holo_dark else R.drawable.kitkat_spinner_ab_holo_light
    HoloAsset.CategoryDivider -> if (darkTheme) R.drawable.kitkat_list_section_divider_holo_dark else R.drawable.kitkat_list_section_divider_holo_light
    HoloAsset.VerticalDivider -> if (darkTheme) R.drawable.kitkat_divider_vertical_holo_dark else R.drawable.kitkat_divider_vertical_holo_light
}
