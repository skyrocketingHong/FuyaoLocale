package ing.fuyaoskyrocket.applocale.ui.designsystem.miuix

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSymbol
import top.yukonga.miuix.kmp.basic.DividerDefaults
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
// Glyph extensions live in the basic (miuix-ui) and extended (miuix-icons)
// packages and must be imported explicitly.
import top.yukonga.miuix.kmp.icon.basic.Check
import top.yukonga.miuix.kmp.icon.basic.Sidebar
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.Pin
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.icon.extended.Search
import top.yukonga.miuix.kmp.icon.extended.SelectAll
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Sort
import top.yukonga.miuix.kmp.theme.LocalContentColor

/** Miuix rendering of the neutral text primitive; colour comes resolved. */
@Composable
internal fun MiuixText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle,
    color: Color,
    maxLines: Int,
    overflow: TextOverflow,
    textAlign: TextAlign? = null,
    fontWeight: FontWeight? = null,
    softWrap: Boolean = true,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        style = style,
        maxLines = maxLines,
        overflow = overflow,
        textAlign = textAlign,
        fontWeight = fontWeight,
        softWrap = softWrap,
    )
}

/** Miuix rendering for rich text; the AnnotatedString keeps search highlights. */
@Composable
internal fun MiuixText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle,
    color: Color,
    maxLines: Int,
    overflow: TextOverflow,
    textAlign: TextAlign? = null,
    fontWeight: FontWeight? = null,
    softWrap: Boolean = true,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        style = style,
        maxLines = maxLines,
        overflow = overflow,
        textAlign = textAlign,
        fontWeight = fontWeight,
        softWrap = softWrap,
    )
}

/**
 * Miuix divider. 0.9.3 ships HorizontalDivider/VerticalDivider with a
 * theme-resolving default colour (dividerLine); thickness stays at the native
 * 0.75dp default.
 */
@Composable
internal fun MiuixDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        thickness = DividerDefaults.Thickness,
        color = DividerDefaults.DividerColor,
    )
}

/**
 * Miuix glyph choice for the named symbols: the extended Regular weight for
 * glyphs that ship in miuix-icons-android, and the three-line Sidebar from the
 * basic set for the menu symbol.
 */
internal fun AppSymbol.miuixSymbolVector(): ImageVector = when (this) {
    AppSymbol.Search -> MiuixIcons.Regular.Search
    AppSymbol.Menu -> MiuixIcons.Basic.Sidebar
    AppSymbol.Back -> MiuixIcons.Regular.Back
    AppSymbol.Close -> MiuixIcons.Regular.Close
    AppSymbol.Refresh -> MiuixIcons.Regular.Refresh
    AppSymbol.Settings -> MiuixIcons.Regular.Settings
    AppSymbol.Pin -> MiuixIcons.Regular.Pin
    AppSymbol.PinActive -> MiuixIcons.Demibold.Pin
    AppSymbol.Check -> MiuixIcons.Basic.Check
    AppSymbol.SelectAll -> MiuixIcons.Regular.SelectAll
    AppSymbol.Sort -> MiuixIcons.Regular.Sort
    AppSymbol.Forward -> MiuixIcons.Regular.ChevronForward
    AppSymbol.Delete -> MiuixIcons.Regular.Delete
    AppSymbol.Info -> MiuixIcons.Regular.Info
    AppSymbol.Add -> MiuixIcons.Regular.Add
}

/** Miuix icon control; unspecified tint falls back to miuix content colour. */
@Composable
internal fun MiuixIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint.takeOrElse { LocalContentColor.current },
    )
}
