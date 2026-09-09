package ing.fuyaoskyrocket.applocale.ui.designsystem.legacy

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
internal fun LegacyText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle,
    color: Color,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    textAlign: TextAlign? = null,
    fontWeight: FontWeight? = null,
    softWrap: Boolean = true,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = style.copy(
            color = color,
            fontWeight = fontWeight ?: style.fontWeight,
            textAlign = textAlign ?: style.textAlign,
            platformStyle = style.platformStyle ?: PlatformTextStyle(includeFontPadding = true),
        ).withLegacyFontCompatibility(),
        onTextLayout = null,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
    )
}

/** Rich-text variant; spans forwarded untouched. */
@Composable
internal fun LegacyText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle,
    color: Color,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    textAlign: TextAlign? = null,
    fontWeight: FontWeight? = null,
    softWrap: Boolean = true,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = style.copy(
            color = color,
            fontWeight = fontWeight ?: style.fontWeight,
            textAlign = textAlign ?: style.textAlign,
            platformStyle = style.platformStyle ?: PlatformTextStyle(includeFontPadding = true),
        ).withLegacyFontCompatibility(),
        onTextLayout = null,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
    )
}
