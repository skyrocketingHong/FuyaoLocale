package ing.fuyaoskyrocket.applocale.ui.designsystem.holo

import ing.fuyaoskyrocket.applocale.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HoloAppearanceTest {
    @Test
    fun dialogCurvesUseTwiceTheAndroidDecelerationFactor() {
        assertEquals(0f, HoloDecelerateQuint.transform(0f), 0.00001f)
        assertEquals(0.96875f, HoloDecelerateQuint.transform(0.5f), 0.00001f)
        assertEquals(0.875f, HoloDecelerateCubic.transform(0.5f), 0.00001f)
        assertEquals(1f, HoloDecelerateCubic.transform(1f), 0.00001f)
    }

    @Test
    fun enabledControlsDoNotMatchDisabledSelectors() {
        val state = HoloControlState().toStateSet()
        assertTrue(android.R.attr.state_enabled in state)
        assertTrue(android.R.attr.state_window_focused in state)
    }

    @Test
    fun checkedAndPressedAreIndependentOfEnabled() {
        val state = HoloControlState(checked = true, pressed = true).toStateSet()
        assertTrue(android.R.attr.state_enabled in state)
        assertTrue(android.R.attr.state_checked in state)
        assertTrue(android.R.attr.state_pressed in state)
        val disabled = HoloControlState(enabled = false, checked = true).toStateSet()
        assertFalse(android.R.attr.state_enabled in disabled)
        assertTrue(android.R.attr.state_checked in disabled)
        assertTrue("Drawable state sets contain only active positive attributes", disabled.all { it > 0 })
    }

    @Test
    fun lightControlsResolveToOriginalLightAssets() {
        val pairs = listOf(
            HoloDrawables.Button to R.drawable.holo_ics_btn_default_holo_light,
            HoloDrawables.RadioButton to R.drawable.holo_ics_btn_radio_holo_light,
            HoloDrawables.Checkbox to R.drawable.holo_ics_btn_check_holo_light,
            HoloDrawables.SwitchTrack to R.drawable.holo_ics_switch_track_holo_light,
            HoloDrawables.SwitchThumb to R.drawable.holo_ics_switch_inner_holo_light,
            HoloDrawables.ActionBar to R.drawable.holo_ics_ab_solid_light_holo,
            HoloDrawables.DialogFrame to R.drawable.holo_ics_dialog_full_holo_light,
            HoloDrawables.EditText to R.drawable.holo_ics_edit_text_holo_light,
            HoloSymbolDrawables.Search to R.drawable.holo_ics_ic_menu_search_holo_light,
        )
        pairs.forEach { (dark, light) ->
            assertTrue(dark != 0 && light != 0 && dark != light)
            assertEquals(light, resolveHoloDrawableId(dark, false))
            assertEquals(dark, resolveHoloDrawableId(dark, true))
        }
    }

    @Test
    fun sharedProgressIsNotReplacedWithAnUnrelatedAsset() {
        assertEquals(HoloDrawables.ProgressMedium, resolveHoloDrawableId(HoloDrawables.ProgressMedium, false))
    }

    @Test
    fun lightAndDarkHaveTheirOwnTextAndBackgroundRoles() {
        val light = holoAppPalette(false)
        val dark = holoAppPalette(true)
        assertEquals(HoloTextColors.Light.primary, light.foreground)
        assertEquals(HoloTextColors.Default.primary, dark.foreground)
        assertTrue(light.background != dark.background)
        assertTrue(light.divider != dark.divider)
    }
}
