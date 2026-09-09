package ing.fuyaoskyrocket.applocale.ui.designsystem.component

import ing.fuyaoskyrocket.applocale.ui.designsystem.lollipop.*
import ing.fuyaoskyrocket.applocale.ui.designsystem.eclair.*
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppControlFamily
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloDivider
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloText
import ing.fuyaoskyrocket.applocale.ui.designsystem.material.MaterialDivider
import ing.fuyaoskyrocket.applocale.ui.designsystem.material.MaterialText
import ing.fuyaoskyrocket.applocale.ui.designsystem.miuix.MiuixDivider
import ing.fuyaoskyrocket.applocale.ui.designsystem.miuix.MiuixText

/**
 * Theme-neutral text primitive. Business screens render text through this
 * dispatcher (plus [AppUiTheme] roles) instead of a specific control library;
 * dispatch follows the style of the provider that is actually rendering —
 * never a global preference guess — and is exhaustive over [AppThemeStyle].
 */
@Composable
fun AppText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = AppUiTheme.textStyles.body,
    color: Color = appDefaultTextColor(),
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    textAlign: TextAlign? = null,
    fontWeight: FontWeight? = null,
    softWrap: Boolean = true,
) {
    when (AppUiTheme.policy.controls) {
        AppControlFamily.Miuix -> MiuixText(
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

        AppControlFamily.Lollipop, AppControlFamily.Eclair -> ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.LegacyText(
            text, modifier, style, color, maxLines, overflow, textAlign, fontWeight, softWrap,
        )

        AppControlFamily.Holo -> HoloText(
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

        AppControlFamily.Material2 -> androidx.compose.material.Text(
            text = text, modifier = modifier, style = style, color = color, maxLines = maxLines,
            overflow = overflow, textAlign = textAlign, fontWeight = fontWeight, softWrap = softWrap,
        )

        AppControlFamily.Material3 -> MaterialText(
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
 * styles (for example search-match highlights); it must never be flattened to
 * a plain String.
 */
@Composable
fun AppText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = AppUiTheme.textStyles.body,
    color: Color = appDefaultTextColor(),
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    textAlign: TextAlign? = null,
    fontWeight: FontWeight? = null,
    softWrap: Boolean = true,
) {
    when (AppUiTheme.policy.controls) {
        AppControlFamily.Miuix -> MiuixText(
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

        AppControlFamily.Lollipop, AppControlFamily.Eclair -> ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.LegacyText(
            text, modifier, style, color, maxLines, overflow, textAlign, fontWeight, softWrap,
        )

        AppControlFamily.Holo -> HoloText(
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

        AppControlFamily.Material2 -> androidx.compose.material.Text(
            text = text, modifier = modifier, style = style, color = color, maxLines = maxLines,
            overflow = overflow, textAlign = textAlign, fontWeight = fontWeight, softWrap = softWrap,
        )

        AppControlFamily.Material3 -> MaterialText(
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
 * Theme-neutral full-width divider line on the active backend's divider
 * treatment (miuix dividerLine / Material outlineVariant / the Holo
 * list_divider 9-patch).
 */
@Composable
fun AppDivider(modifier: Modifier = Modifier) {
    when (AppUiTheme.policy.controls) {
        AppControlFamily.Miuix -> MiuixDivider(modifier)
        AppControlFamily.Lollipop -> LollipopDivider(modifier)
        AppControlFamily.Eclair -> EclairDivider(modifier)
        AppControlFamily.Holo -> HoloDivider(modifier)
        AppControlFamily.Material2 -> androidx.compose.material.Divider(modifier, color = AppUiTheme.palette.divider)
        AppControlFamily.Material3 -> MaterialDivider(modifier)
    }
}

@Composable
private fun appDefaultTextColor(): Color = if (AppUiTheme.policy.controls == AppControlFamily.Lollipop)
    LocalLollipopContentColor.current.takeOrElse { AppUiTheme.palette.foreground } else AppUiTheme.palette.foreground
