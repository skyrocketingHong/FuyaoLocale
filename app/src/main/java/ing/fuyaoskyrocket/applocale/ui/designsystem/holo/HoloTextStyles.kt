package ing.fuyaoskyrocket.applocale.ui.designsystem.holo

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppTextStyles

/**
 * The ICS Roboto files imported from AOSP data/fonts (Apache-2.0,
 * docs/holo-ics/asset-manifest.json). ICS shipped no medium/light weights in
 * this app's scope; hierarchy comes from the 22/18/16/14sp TextAppearance
 * ladder and bold, exactly like TextAppearance.Holo.
 */
val HoloFontFamily = FontFamily(
    Font(R.font.holo_ics_roboto_regular),
    Font(R.font.holo_ics_roboto_bold, FontWeight.Bold),
)

internal val LocalHoloFontFamily = staticCompositionLocalOf { HoloFontFamily }

/**
 * Neutral text roles mapped onto the Holo TextAppearance inheritance:
 * - pageTitle: TextAppearance.Holo.Widget.ActionBar.Title (Medium → 18sp);
 * - itemTitle: TextAppearance.Medium (18sp) — two-line list primary text;
 * - body: TextAppearance base (16sp);
 * - metadata/label: TextAppearance.Small (14sp). Colour roles are set by the
 * palette at the call site (primary vs dim), not baked into the styles.
 */
internal fun holoTextStyles(fontFamily: FontFamily = HoloFontFamily): AppTextStyles = AppTextStyles(
    pageTitle = TextStyle(
        fontFamily = fontFamily,
        fontSize = 18.sp,
    ),
    itemTitle = TextStyle(
        fontFamily = fontFamily,
        fontSize = 18.sp,
    ),
    body = TextStyle(
        fontFamily = fontFamily,
        fontSize = 16.sp,
    ),
    metadata = TextStyle(
        fontFamily = fontFamily,
        fontSize = 14.sp,
    ),
    label = TextStyle(
        fontFamily = fontFamily,
        fontSize = 14.sp,
    ),
)
