package ing.fuyaoskyrocket.applocale.ui.designsystem.material

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material.icons.outlined.Settings
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSymbol

/** Material rendering of the neutral text primitive; colour comes resolved. */
@Composable
internal fun MaterialText(
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

/** Material rendering for rich text; the AnnotatedString keeps search highlights. */
@Composable
internal fun MaterialText(
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

/** Material divider on the neutral divider role (outlineVariant). */
@Composable
internal fun MaterialDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

/**
 * Material glyph choice for the named symbols. Only this backend picks Material
 * icons; the miuix backend resolves the same names to MiuixIcons.
 */
internal fun AppSymbol.materialSymbolVector(): ImageVector = when (this) {
    AppSymbol.Search -> Icons.Outlined.Search
    AppSymbol.Menu -> Icons.Outlined.Menu
    AppSymbol.Back -> Icons.AutoMirrored.Outlined.ArrowBack
    AppSymbol.Close -> Icons.Outlined.Close
    AppSymbol.Refresh -> Icons.Outlined.Refresh
    AppSymbol.Settings -> Icons.Outlined.Settings
    AppSymbol.Pin -> Icons.Outlined.PushPin
    AppSymbol.PinActive -> Icons.Filled.PushPin
    AppSymbol.Check -> Icons.Outlined.Check
    AppSymbol.SelectAll -> Icons.Outlined.SelectAll
    AppSymbol.Sort -> Icons.AutoMirrored.Outlined.Sort
    AppSymbol.Forward -> Icons.AutoMirrored.Outlined.ArrowForward
    AppSymbol.Delete -> Icons.Outlined.DeleteOutline
    AppSymbol.Info -> Icons.Outlined.Info
    AppSymbol.Add -> Icons.Outlined.Add
}

/** Material icon control; unspecified tint falls back to Material content colour. */
@Composable
internal fun MaterialIcon(
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
