package ing.fuyaoskyrocket.applocale.ui.designsystem.kitkat

import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemeStyle
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloTextColors
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.LocalHoloContentColor
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.LocalHoloDarkTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.LocalHoloTextColors

/**
 * App-level KitKat white/neutral accents. Framework 4.4 still contains blue Holo assets;
 * keep those source files intact and adjust only accent-bearing controls at draw time.
 * Luminance/alpha retain the original edges, cut-outs and disabled-state geometry.
 */
internal fun applyKitKatControlPalette(resource: Int, drawable: Drawable, dark: Boolean) {
    if (resource !in monochromeControls) return
    val rgb = if (dark) floatArrayOf(0f, 0f, 255f / 229f) else floatArrayOf(.19134f, .64368f, .06498f)
    drawable.colorFilter = ColorMatrixColorFilter(floatArrayOf(
        rgb[0], rgb[1], rgb[2], 0f, 0f,
        rgb[0], rgb[1], rgb[2], 0f, 0f,
        rgb[0], rgb[1], rgb[2], 0f, 0f,
        0f, 0f, 0f, 1f, 0f,
    ))
}

private val monochromeControls by lazy {
    setOf(
        R.drawable.kitkat_btn_default_holo_dark, R.drawable.kitkat_btn_default_holo_light,
        R.drawable.kitkat_btn_check_holo_dark, R.drawable.kitkat_btn_check_holo_light,
        R.drawable.kitkat_btn_radio_holo_dark, R.drawable.kitkat_btn_radio_holo_light,
        R.drawable.kitkat_edit_text_holo_dark, R.drawable.kitkat_edit_text_holo_light,
        R.drawable.kitkat_tab_indicator_ab_holo,
        R.drawable.kitkat_list_selector_holo_dark, R.drawable.kitkat_list_selector_holo_light,
        R.drawable.kitkat_item_background_holo_dark, R.drawable.kitkat_item_background_holo_light,
        R.drawable.kitkat_activated_background_holo_dark, R.drawable.kitkat_activated_background_holo_light,
        R.drawable.kitkat_switch_inner_holo_dark, R.drawable.kitkat_switch_inner_holo_light,
        R.drawable.kitkat_switch_track_holo_dark, R.drawable.kitkat_switch_track_holo_light,
        R.drawable.kitkat_spinner_ab_holo_dark, R.drawable.kitkat_spinner_ab_holo_light,
        R.drawable.kitkat_progress_medium_holo, R.drawable.kitkat_progress_indeterminate_horizontal_holo,
        R.drawable.kitkat_progress_horizontal_holo_dark, R.drawable.kitkat_progress_horizontal_holo_light,
        R.drawable.kitkat_textfield_searchview_holo_dark, R.drawable.kitkat_textfield_searchview_holo_light,
        R.drawable.kitkat_list_section_divider_holo_dark, R.drawable.kitkat_list_section_divider_holo_light,
    )
}

/** Light content uses the real Holo Light.DarkActionBar arrangement, with white chrome accents. */
@Composable
internal fun KitKatChrome(content: @Composable () -> Unit) {
    if (AppUiTheme.style != AppThemeStyle.HOLO_KITKAT) {
        content()
        return
    }
    CompositionLocalProvider(
        LocalHoloDarkTheme provides true,
        LocalHoloTextColors provides HoloTextColors.Default,
        LocalHoloContentColor provides Color.White,
        content = content,
    )
}
