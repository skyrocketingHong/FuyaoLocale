package ing.fuyaoskyrocket.applocale.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ing.fuyaoskyrocket.applocale.ui.designsystem.LocalUserPreferences
import java.util.Locale

/**
 * Shared language/region marker for locale lists.
 *
 * Directory rows show the complete language subtag (including three-letter ISO codes).
 * Locale variants prefer a regional flag and fall back to region, script, or language letters.
 */
@Composable
fun LocaleBadge(
    languageTag: String,
    preferRegion: Boolean,
    modifier: Modifier = Modifier,
) {
    val marker = localeMarker(languageTag, preferRegion, LocalUserPreferences.current.showRegionFlags)
    ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppBadge(marker.text, marker.isFlag, modifier)
}

private data class LocaleMarker(val text: String, val isFlag: Boolean)

private fun localeMarker(languageTag: String, preferRegion: Boolean, showFlags: Boolean): LocaleMarker {
    val locale = Locale.forLanguageTag(languageTag)
    val language = locale.language.uppercase(Locale.ROOT)
    val script = locale.script.uppercase(Locale.ROOT)
    val region = locale.country.uppercase(Locale.ROOT)

    if (preferRegion && showFlags && region.length == 2 && region.all { it in 'A'..'Z' }) {
        return LocaleMarker(text = region.toFlagEmoji(), isFlag = true)
    }

    val fallback = when {
        preferRegion && region.isNotBlank() -> region
        preferRegion && script.isNotBlank() -> script
        language.isNotBlank() -> language
        else -> languageTag.substringBefore('-').uppercase(Locale.ROOT)
    }
    return LocaleMarker(text = fallback.ifBlank { "—" }, isFlag = false)
}

private fun String.toFlagEmoji(): String = buildString(length * 2) {
    this@toFlagEmoji.forEach { letter ->
        append(Character.toChars(REGIONAL_INDICATOR_A + (letter - 'A')))
    }
}

private const val REGIONAL_INDICATOR_A = 0x1F1E6
