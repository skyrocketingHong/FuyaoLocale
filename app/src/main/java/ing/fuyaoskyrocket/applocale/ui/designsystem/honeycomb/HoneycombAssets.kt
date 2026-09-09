package ing.fuyaoskyrocket.applocale.ui.designsystem.honeycomb

import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloAsset

/** API 11 SDK resources: no ICS resource aliases or fallback lookup. */
internal fun resolveHoneycombAsset(asset: HoloAsset, darkTheme: Boolean): Int = when (asset) {
    HoloAsset.Button -> if (darkTheme) R.drawable.honeycomb_btn_default_holo_dark else R.drawable.honeycomb_btn_default_holo_light
    HoloAsset.Checkbox -> if (darkTheme) R.drawable.honeycomb_btn_check_holo_dark else R.drawable.honeycomb_btn_check_holo_light
    HoloAsset.RadioButton -> if (darkTheme) R.drawable.honeycomb_btn_radio_holo_dark else R.drawable.honeycomb_btn_radio_holo_light
    HoloAsset.EditText -> if (darkTheme) R.drawable.honeycomb_edit_text_holo_dark else R.drawable.honeycomb_edit_text_holo_light
    HoloAsset.ListSelector -> if (darkTheme) R.drawable.honeycomb_list_selector_holo_dark else R.drawable.honeycomb_list_selector_holo_light
    HoloAsset.ItemBackground -> if (darkTheme) R.drawable.honeycomb_item_background_holo_dark else R.drawable.honeycomb_item_background_holo_light
    HoloAsset.TabIndicator -> R.drawable.honeycomb_tab_indicator_holo
    HoloAsset.ProgressMedium -> R.drawable.honeycomb_progress_medium_holo
    HoloAsset.ProgressHorizontal -> if (darkTheme) R.drawable.honeycomb_progress_horizontal_holo_dark else R.drawable.honeycomb_progress_horizontal_holo_light
    HoloAsset.SwitchTrack -> if (darkTheme) R.drawable.honeycomb_switch_track_holo_dark else R.drawable.honeycomb_switch_track_holo_light
    HoloAsset.SwitchThumb -> if (darkTheme) R.drawable.honeycomb_switch_inner_holo_dark else R.drawable.honeycomb_switch_inner_holo_light
    HoloAsset.ActionBar -> R.color.honeycomb_transparent
    HoloAsset.ActionBarStacked -> R.color.honeycomb_transparent
    HoloAsset.ActionBarBottom -> R.color.honeycomb_transparent
    HoloAsset.ActionBarShadow -> R.color.honeycomb_transparent
    HoloAsset.CabBackground -> if (darkTheme) R.drawable.honeycomb_cab_background_holo_dark else R.drawable.honeycomb_cab_background_holo_light
    HoloAsset.MenuPanel -> if (darkTheme) R.drawable.honeycomb_menu_dropdown_panel_holo_dark else R.drawable.honeycomb_menu_dropdown_panel_holo_light
    HoloAsset.ToastFrame -> R.drawable.honeycomb_toast_frame_holo
    HoloAsset.DialogFrame -> if (darkTheme) R.drawable.honeycomb_dialog_full_holo_dark else R.drawable.honeycomb_dialog_full_holo_light
    HoloAsset.Divider -> if (darkTheme) R.drawable.honeycomb_list_divider_holo_dark else R.drawable.honeycomb_list_divider_holo_light
    HoloAsset.ScrollbarHandle -> if (darkTheme) R.drawable.honeycomb_scrollbar_handle_holo_dark else R.drawable.honeycomb_scrollbar_handle_holo_light
    HoloAsset.Clear -> R.drawable.honeycomb_ic_clear
    HoloAsset.Search -> if (darkTheme) R.drawable.honeycomb_ic_menu_search else R.drawable.honeycomb_ic_menu_search_holo_light
    HoloAsset.Overflow -> if (darkTheme) R.drawable.honeycomb_ic_menu_moreoverflow_normal_holo_dark else R.drawable.honeycomb_ic_menu_moreoverflow_normal_holo_light
    HoloAsset.Back -> if (darkTheme) R.drawable.honeycomb_ic_ab_back_holo_dark else R.drawable.honeycomb_ic_ab_back_holo_light
    HoloAsset.CabClose -> R.drawable.honeycomb_cab_ic_close_holo
    HoloAsset.Close -> R.drawable.honeycomb_ic_menu_close_clear_cancel
    HoloAsset.Refresh -> R.drawable.honeycomb_ic_menu_refresh
    HoloAsset.Delete -> R.drawable.honeycomb_ic_menu_delete
    HoloAsset.Add -> R.drawable.honeycomb_ic_input_add
    HoloAsset.ActivatedBackground -> if (darkTheme) R.drawable.honeycomb_activated_background_holo_dark else R.drawable.honeycomb_activated_background_holo_light
    HoloAsset.ProgressIndeterminateHorizontal -> R.drawable.honeycomb_progress_indeterminate_horizontal_holo
    HoloAsset.SearchField -> if (darkTheme) R.drawable.honeycomb_edit_text_holo_dark else R.drawable.honeycomb_edit_text_holo_light
    HoloAsset.Spinner -> if (darkTheme) R.drawable.honeycomb_spinner_background_holo_dark else R.drawable.honeycomb_spinner_background_holo_light
    HoloAsset.CategoryDivider -> if (darkTheme) R.drawable.honeycomb_list_section_divider_holo_dark else R.drawable.honeycomb_list_section_divider_holo_light
    HoloAsset.VerticalDivider -> if (darkTheme) R.drawable.honeycomb_divider_vertical_holo_dark else R.drawable.honeycomb_divider_vertical_holo_light
}
