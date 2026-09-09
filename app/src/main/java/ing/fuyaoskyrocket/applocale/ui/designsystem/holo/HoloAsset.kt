package ing.fuyaoskyrocket.applocale.ui.designsystem.holo

import androidx.annotation.DrawableRes
import ing.fuyaoskyrocket.applocale.R

/** Version-neutral names requested by common Holo layout code. */
internal enum class HoloAsset {
    Button,
    Checkbox,
    RadioButton,
    EditText,
    ListSelector,
    ItemBackground,
    TabIndicator,
    ProgressMedium,
    ProgressHorizontal,
    SwitchTrack,
    SwitchThumb,
    ActionBar,
    ActionBarStacked,
    ActionBarBottom,
    ActionBarShadow,
    CabBackground,
    MenuPanel,
    ToastFrame,
    DialogFrame,
    Divider,
    ScrollbarHandle,
    Clear,
    Search,
    Overflow,
    Back,
    Close,
    CabClose,
    Refresh,
    Delete,
    Add,
    ActivatedBackground,
    ProgressIndeterminateHorizontal,
    SearchField,
    Spinner,
    CategoryDivider,
    VerticalDivider,
}

/** The ICS provider owns its own mapping; other eras do not use its resource IDs as keys. */
@DrawableRes
internal fun resolveIcsAsset(asset: HoloAsset, darkTheme: Boolean): Int = resolveHoloDrawableId(when (asset) {
    HoloAsset.Button -> R.drawable.holo_ics_btn_default_holo_dark
    HoloAsset.Checkbox -> R.drawable.holo_ics_btn_check_holo_dark
    HoloAsset.RadioButton -> R.drawable.holo_ics_btn_radio_holo_dark
    HoloAsset.EditText -> R.drawable.holo_ics_edit_text_holo_dark
    HoloAsset.ListSelector -> R.drawable.holo_ics_list_selector_holo_dark
    HoloAsset.ItemBackground -> R.drawable.holo_ics_item_background_holo_dark
    HoloAsset.TabIndicator -> R.drawable.holo_ics_tab_indicator_ab_holo
    HoloAsset.ProgressMedium -> R.drawable.holo_ics_progress_medium_holo
    HoloAsset.ProgressHorizontal -> R.drawable.holo_ics_progress_horizontal_holo_dark
    HoloAsset.SwitchTrack -> R.drawable.holo_ics_switch_track_holo_dark
    HoloAsset.SwitchThumb -> R.drawable.holo_ics_switch_inner_holo_dark
    HoloAsset.ActionBar -> R.drawable.holo_ics_ab_solid_dark_holo
    HoloAsset.ActionBarStacked -> R.drawable.holo_ics_ab_stacked_solid_dark_holo
    HoloAsset.ActionBarBottom -> R.drawable.holo_ics_ab_bottom_solid_dark_holo
    HoloAsset.ActionBarShadow -> R.drawable.holo_ics_ab_solid_shadow_holo
    HoloAsset.CabBackground -> R.drawable.holo_ics_cab_background_top_holo_dark
    HoloAsset.MenuPanel -> R.drawable.holo_ics_menu_dropdown_panel_holo_dark
    HoloAsset.ToastFrame -> R.drawable.holo_ics_toast_frame_holo
    HoloAsset.DialogFrame -> R.drawable.holo_ics_dialog_full_holo_dark
    HoloAsset.Divider -> R.drawable.holo_ics_list_divider_holo_dark
    HoloAsset.ScrollbarHandle -> R.drawable.holo_ics_scrollbar_handle_holo_dark
    HoloAsset.Clear -> R.drawable.holo_ics_ic_clear
    HoloAsset.Search -> R.drawable.holo_ics_ic_menu_search_holo_dark
    HoloAsset.Overflow -> R.drawable.holo_ics_ic_menu_moreoverflow_normal_holo_dark
    HoloAsset.Back -> R.drawable.holo_ics_ic_ab_back_holo_dark
    HoloAsset.CabClose -> R.drawable.holo_ics_ic_menu_close_clear_cancel
    HoloAsset.Close -> R.drawable.holo_ics_ic_menu_close_clear_cancel
    HoloAsset.Refresh -> R.drawable.holo_ics_ic_menu_refresh
    HoloAsset.Delete -> R.drawable.holo_ics_ic_menu_delete
    HoloAsset.Add -> R.drawable.holo_ics_ic_input_add
    HoloAsset.ActivatedBackground -> R.drawable.holo_ics_activated_background_holo_dark
    HoloAsset.ProgressIndeterminateHorizontal -> R.drawable.holo_ics_progress_indeterminate_horizontal_holo
    HoloAsset.SearchField -> R.drawable.holo_ics_textfield_searchview_holo_dark
    HoloAsset.Spinner -> R.drawable.holo_ics_spinner_ab_holo_dark
    HoloAsset.CategoryDivider -> R.drawable.holo_ics_list_section_divider_holo_dark
    HoloAsset.VerticalDivider -> R.drawable.holo_ics_divider_vertical_holo_dark
}, darkTheme)
