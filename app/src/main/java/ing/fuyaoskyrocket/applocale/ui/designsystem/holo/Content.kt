package ing.fuyaoskyrocket.applocale.ui.designsystem.holo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.withLegacyFontCompatibility
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

/**
 * Holo rendering of the neutral text primitive on BasicText: the style keeps
 * its spans (search highlights stay AnnotatedString), colour falls back to
 * the bright foreground content role when unspecified.
 */
@Composable
internal fun HoloText(
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
internal fun HoloText(
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

/**
 * Holo list divider: the imported list_divider_holo_dark 9-patch stretched
 * across the full width at its density-scaled intrinsic height (the visual
 * line is its 1dp white-38 centre row), isolated per site.
 */
@Composable
internal fun HoloDivider(modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val drawable = rememberHoloDrawable(HoloAsset.Divider)
    val height = with(density) { drawable.intrinsicHeight.toDp() }.takeIf { it > 0.dp } ?: 1.dp
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .holoBackground(drawable),
    )
}

/** Draw the original divider at its natural height across a continuous row. */
@Composable
internal fun Modifier.holoListDivider(): Modifier = holoDrawable(
    drawable = rememberHoloDrawable(HoloAsset.Divider),
    boundsMode = HoloDrawableBounds.Horizontal,
    alignment = androidx.compose.ui.Alignment.BottomCenter,
)
