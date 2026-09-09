package ing.fuyaoskyrocket.applocale.ui.designsystem.legacy

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontSynthesis
import ing.fuyaoskyrocket.applocale.ui.designsystem.LocalUserPreferences

/** Bundled historical fonts have no Chinese glyphs. Let the system resolve the
 * whole weighted family by default, instead of mixing a weighted Latin face
 * with an OEM's regular-only custom-font fallback. */
@Composable
internal fun legacyFontFamily(original: FontFamily): FontFamily =
    if (LocalUserPreferences.current.systemFontForRetro) FontFamily.SansSerif else original

@Composable
internal fun TextStyle.withLegacyFontCompatibility(): TextStyle = copy(
    fontFamily = if (LocalUserPreferences.current.systemFontForRetro) FontFamily.SansSerif else fontFamily,
    fontSynthesis = FontSynthesis.Weight,
)
