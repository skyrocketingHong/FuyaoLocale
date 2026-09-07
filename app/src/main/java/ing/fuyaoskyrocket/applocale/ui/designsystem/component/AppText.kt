package ing.fuyaoskyrocket.applocale.ui.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemePreferences
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemeStyle
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.material.MaterialDivider
import ing.fuyaoskyrocket.applocale.ui.designsystem.material.MaterialText
import ing.fuyaoskyrocket.applocale.ui.designsystem.miuix.MiuixDivider
import ing.fuyaoskyrocket.applocale.ui.designsystem.miuix.MiuixText

/**
 * Theme-neutral text primitive. Business screens render text through this
 * dispatcher (plus [AppUiTheme] roles) instead of a specific control library;
 * the MIUIX style draws with the miuix Text and Material You with the
 * Material 3 Text.
 */
@Composable
fun AppText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = AppUiTheme.textStyles.body,
    color: Color = AppUiTheme.palette.foreground,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    textAlign: TextAlign? = null,
    fontWeight: FontWeight? = null,
    softWrap: Boolean = true,
) {
    if (AppThemePreferences.style == AppThemeStyle.MIUIX) {
        MiuixText(
            text = text,
            modifier = modifier,
            style = style,
            color = color,
            maxLines = maxLines,
            overflow = overflow,
            textAlign = textAlign,
            fontWeight = fontWeight,
            softWrap = softWrap,
        )
    } else {
        MaterialText(
            text = text,
            modifier = modifier,
            style = style,
            color = color,
            maxLines = maxLines,
            overflow = overflow,
            textAlign = textAlign,
            fontWeight = fontWeight,
            softWrap = softWrap,
        )
    }
}

/**
 * Rich-text variant. The [AnnotatedString] is forwarded as-is to keep span
 * styles (for example search-match highlights); it must never be flattened to a
 * plain String.
 */
@Composable
fun AppText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = AppUiTheme.textStyles.body,
    color: Color = AppUiTheme.palette.foreground,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    textAlign: TextAlign? = null,
    fontWeight: FontWeight? = null,
    softWrap: Boolean = true,
) {
    if (AppThemePreferences.style == AppThemeStyle.MIUIX) {
        MiuixText(
            text = text,
            modifier = modifier,
            style = style,
            color = color,
            maxLines = maxLines,
            overflow = overflow,
            textAlign = textAlign,
            fontWeight = fontWeight,
            softWrap = softWrap,
        )
    } else {
        MaterialText(
            text = text,
            modifier = modifier,
            style = style,
            color = color,
            maxLines = maxLines,
            overflow = overflow,
            textAlign = textAlign,
            fontWeight = fontWeight,
            softWrap = softWrap,
        )
    }
}

/**
 * Theme-neutral full-width divider line on the palette divider role (miuix
 * dividerLine / Material outlineVariant), rendered by the active backend.
 */
@Composable
fun AppDivider(modifier: Modifier = Modifier) {
    if (AppThemePreferences.style == AppThemeStyle.MIUIX) {
        MiuixDivider(modifier)
    } else {
        MaterialDivider(modifier)
    }
}
