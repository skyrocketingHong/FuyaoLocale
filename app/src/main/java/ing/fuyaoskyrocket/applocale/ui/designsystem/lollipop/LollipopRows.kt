package ing.fuyaoskyrocket.applocale.ui.designsystem.lollipop

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.*

internal val LocalLollipopDialogContent = staticCompositionLocalOf { false }

@Composable
internal fun LollipopVerticalScrollBar(listState: androidx.compose.foundation.lazy.LazyListState, modifier: Modifier = Modifier) {
    LegacyVerticalScrollBar(rememberLollipopDrawable(R.drawable.lollipop_scrollbar_handle_material), fadeDelayMillis = 300, fadeDurationMillis = 250, listState = listState, modifier = modifier)
}

@Composable
internal fun Modifier.lollipopListDivider(): Modifier = if (LocalLollipopDialogContent.current) this else
    legacyDrawable(rememberLollipopDrawable(R.drawable.lollipop_list_divider_material),
        boundsMode = LegacyDrawableBounds.Horizontal, alignment = Alignment.BottomCenter)

@Composable
internal fun LollipopDivider(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(1.dp).lollipopListDivider())
}

@Composable
internal fun LollipopCategory(title: String, modifier: Modifier = Modifier) {
    BasicText(title, modifier.fillMaxWidth().padding(horizontal = AppUiTheme.spacing.contentInset, vertical = AppUiTheme.spacing.bodyVerticalPadding).semantics { heading() },
        style = lollipopTextStyle(14, FontWeight.Medium).copy(color = LocalLollipopColors.current.accent))
}

@Composable
internal fun LollipopSettingsRow(title: String, modifier: Modifier = Modifier, summary: String? = null,
    titleMaxLines: Int = 1, summaryMaxLines: Int = 2, titleColor: Color = Color.Unspecified,
    summaryColor: Color = Color.Unspecified, onClick: (() -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null, trailing: (@Composable () -> Unit)? = null) {
    val colors = LocalLollipopColors.current
    val interaction = remember { MutableInteractionSource() }
    Row(modifier.fillMaxWidth().heightIn(min = 48.dp).lollipopListDivider()
        .then(if (onClick != null) Modifier.clickable(interaction, LollipopRipple(colors.ripple), onClick = onClick) else Modifier)
        .padding(horizontal = AppUiTheme.spacing.contentInset), verticalAlignment = Alignment.CenterVertically) {
        if (leading != null) Box(Modifier.width(60.dp), contentAlignment = Alignment.CenterStart) { leading() }
        Column(Modifier.weight(1f).padding(vertical = 16.dp)) {
            BasicText(title, style = lollipopTextStyle(16).copy(color = if (titleColor == Color.Unspecified) colors.foreground else titleColor),
                maxLines = titleMaxLines, overflow = TextOverflow.Ellipsis)
            if (summary != null) BasicText(summary, style = lollipopTextStyle(14).copy(color = if (summaryColor == Color.Unspecified) colors.secondary else summaryColor),
                maxLines = summaryMaxLines, overflow = TextOverflow.Ellipsis)
        }
        if (trailing != null) { Spacer(Modifier.width(16.dp)); trailing() }
    }
}

@Composable
internal fun LollipopChoiceRow(title: String, subtitle: String?, selected: Boolean, onSelect: () -> Unit,
    modifier: Modifier = Modifier, leading: (@Composable () -> Unit)? = null,
    trailingAction: (@Composable () -> Unit)? = null, titleMaxLines: Int = 2, subtitleMaxLines: Int = 2) {
    val colors = LocalLollipopColors.current
    val interaction = remember { MutableInteractionSource() }
    Row(modifier.fillMaxWidth().heightIn(min = 48.dp)
        .selectable(selected, interaction, LollipopRipple(colors.ripple), role = Role.RadioButton, onClick = onSelect)
        .padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        if (leading != null) { leading(); Spacer(Modifier.width(16.dp)) }
        Column(Modifier.weight(1f)) {
            BasicText(title, style = lollipopTextStyle(16).copy(color = colors.foreground), maxLines = titleMaxLines, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) BasicText(subtitle, style = lollipopTextStyle(14).copy(color = colors.secondary), maxLines = subtitleMaxLines, overflow = TextOverflow.Ellipsis)
        }
        trailingAction?.invoke()
        LollipopRadioButton(selected, null)
    }
}
