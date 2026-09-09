package ing.fuyaoskyrocket.applocale.ui.designsystem.holo

import androidx.compose.runtime.Immutable
import androidx.annotation.ColorRes
import androidx.compose.ui.graphics.Color
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppPalette

/**
 * Holo Dark colour facts, every value copied from AOSP 4.0.4 colors.xml /
 * Theme.Holo highlight attributes. The AOSP accent roles stay distinct:
 * pressed uses holo_blue_light, focused/activated use holo_blue_dark — the
 * backend never collapses them into one "accent" blue.
 */
@Immutable
class HoloControlColors internal constructor(
    /** colorPressedHighlight = holo_blue_light. */
    val pressedHighlight: Color,
    /** colorFocusedHighlight / colorActivatedHighlight = holo_blue_dark. */
    val focusedHighlight: Color,
    val activatedHighlight: Color,
    /** colorLongPressedHighlight = holo_blue_bright. */
    val longPressedHighlight: Color,
    /** colorMultiSelectHighlight = holo_green_light. */
    val multiSelectHighlight: Color,
) {
    companion object {
        /** values/colors.xml + Theme.Holo highlight items, fixed commit. */
        val Default = HoloControlColors(
            pressedHighlight = Color(0xff33b5e5),
            focusedHighlight = Color(0xff0099cc),
            activatedHighlight = Color(0xff0099cc),
            longPressedHighlight = Color(0xff00ddff),
            multiSelectHighlight = Color(0xff99cc00),
        )
    }
}

/** Resolved text colour roles of primary_text/secondary_text_holo_dark. */
@Immutable
class HoloTextColors internal constructor(
    val primary: Color,
    val primaryDisabled: Color,
    val secondary: Color,
    val secondaryDisabled: Color,
    /** hint_foreground_holo_dark. */
    val hint: Color,
) {
    companion object {
        val Default = HoloTextColors(
            // bright_foreground_holo_dark = background_holo_light = #fff3f3f3
            primary = Color(0xfff3f3f3),
            // bright_foreground_disabled_holo_dark
            primaryDisabled = Color(0xff4c4c4c),
            // dim_foreground_holo_dark
            secondary = Color(0xffbebebe),
            // dim_foreground_disabled_holo_dark (color WITH alpha)
            secondaryDisabled = Color(0x80bebebe),
            hint = Color(0xff808080),
        )
        val Light = HoloTextColors(
            primary = Color(0xff000000),
            primaryDisabled = Color(0xffb2b2b2),
            secondary = Color(0xff323232),
            secondaryDisabled = Color(0x80323232),
            hint = Color(0xff808080),
        )
    }
}

/**
 * The Holo contribution to the neutral [AppPalette]:
 * - surface/secondary surfaces are the Theme.Holo black background; Holo
 *   containers are flat, their chrome draws the ab 9-patches instead;
 * - accent = holo_blue_light with black foreground (the ICS switch/check blue);
 * - selected = holo_blue_dark (colorActivatedHighlight) for the rare role use;
 * - quiet info rows are plain text on black (no modern quiet container);
 * - divider = the white-38 line of list_divider_holo_dark.
 */
internal fun holoAppPalette(darkTheme: Boolean = true): AppPalette {
    val text = if (darkTheme) HoloTextColors.Default else HoloTextColors.Light
    val background = if (darkTheme) Color(0xff000000) else Color(0xfff3f3f3)
    return AppPalette(
    background = background,
    foreground = text.primary,
    surface = background,
    surfaceContent = text.primary,
    secondarySurface = background,
    secondaryContent = text.secondary,
    muted = text.secondary,
    accent = Color(0xff33b5e5),
    onAccent = Color(0xff000000),
    selected = Color(0xff0099cc),
    onSelected = Color(0xfff3f3f3),
    quietContainer = background,
    quietContent = text.secondary,
    error = Color(0xffff4444),
    onError = Color(0xff000000),
    divider = if (darkTheme) Color(0x26ffffff) else Color(0x33000000),
)
}

/** Res id of the selector-backed text colours (state lists kept for bridging). */
object HoloColorLists {
    // Keep the R field read until resource linking; const aliases inline stub 0.
    @get:ColorRes val PrimaryText: Int get() = R.color.holo_ics_primary_text_dark
    @get:ColorRes val SecondaryText: Int get() = R.color.holo_ics_secondary_text_dark
}
