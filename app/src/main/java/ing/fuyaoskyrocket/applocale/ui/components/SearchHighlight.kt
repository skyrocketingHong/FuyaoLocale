package ing.fuyaoskyrocket.applocale.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString

/**
 * Marks every case-insensitive occurrence of [query] in [text] with [highlightStyle].
 *
 * Pure function: no regex, no lowercased index remapping and no character
 * replacement, so the annotated content stays byte-identical to [text] for
 * accessibility readout. Matches never overlap; a blank query (after trim)
 * returns the untouched text.
 */
fun highlightedText(
    text: String,
    query: String,
    highlightStyle: SpanStyle,
): AnnotatedString {
    val needle = query.trim()
    if (needle.isEmpty()) return AnnotatedString(text)
    return buildAnnotatedString {
        append(text)
        var start = 0
        while (true) {
            val index = text.indexOf(needle, start, ignoreCase = true)
            if (index < 0) break
            val end = index + needle.length
            addStyle(highlightStyle, index, end)
            start = end
        }
    }
}
