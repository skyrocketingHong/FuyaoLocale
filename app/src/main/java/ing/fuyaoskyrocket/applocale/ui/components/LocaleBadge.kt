package ing.fuyaoskyrocket.applocale.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.EmojiSupportMatch
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppLayout
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSurface
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppText
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
    val marker = localeMarker(languageTag, preferRegion)
    // QuietBadge role (020): the flag/letter backing is a non-interactive info
    // surface, never a selection-coloured one.
    AppSurface(
        modifier = modifier.size(AppLayout.localeBadgeSize),
        shape = RoundedCornerShape(8.dp),
        color = AppUiTheme.palette.quietContainer,
        contentColor = AppUiTheme.palette.quietContent,
    ) {
        Box(contentAlignment = Alignment.Center) {
            AppText(
                text = marker.text,
                style = marker.textStyle().let { style ->
                    if (marker.isFlag) {
                        style.copy(
                            platformStyle = PlatformTextStyle(
                                emojiSupportMatch = EmojiSupportMatch.None,
                            ),
                        )
                    } else {
                        style
                    }
                },
                fontWeight = if (marker.isFlag) FontWeight.Normal else FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

private data class LocaleMarker(
    val text: String,
    val isFlag: Boolean,
)

@Composable
private fun LocaleMarker.textStyle(): TextStyle {
    val textStyles = AppUiTheme.textStyles
    return when {
        isFlag -> textStyles.pageTitle
        text.length <= 2 -> textStyles.label.copy(fontSize = 14.sp)
        text.length == 3 -> textStyles.label
        else -> textStyles.label.copy(fontSize = 11.sp)
    }
}

private fun localeMarker(languageTag: String, preferRegion: Boolean): LocaleMarker {
    val locale = Locale.forLanguageTag(languageTag)
    val language = locale.language.uppercase(Locale.ROOT)
    val script = locale.script.uppercase(Locale.ROOT)
    val region = locale.country.uppercase(Locale.ROOT)

    if (preferRegion && region.length == 2 && region.all { it in 'A'..'Z' }) {
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
