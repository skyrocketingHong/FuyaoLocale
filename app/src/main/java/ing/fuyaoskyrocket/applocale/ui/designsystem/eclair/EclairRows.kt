package ing.fuyaoskyrocket.applocale.ui.designsystem.eclair

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.*

@Composable
internal fun Modifier.eclairListDivider(): Modifier = legacyDrawable(
    rememberClassicDrawable(if (LocalEclairColors.current.dark) R.drawable.eclair_divider_horizontal_dark_opaque
        else R.drawable.eclair_divider_horizontal_bright_opaque),
    boundsMode = LegacyDrawableBounds.Horizontal, alignment = Alignment.BottomCenter,
)

@Composable
internal fun EclairDivider(modifier: Modifier = Modifier) {
    val drawable = rememberClassicDrawable(if (LocalEclairColors.current.dark) R.drawable.eclair_divider_horizontal_dark_opaque
        else R.drawable.eclair_divider_horizontal_bright_opaque)
    val height = with(LocalDensity.current) { drawable.intrinsicHeight.toDp().coerceAtLeast(1.dp) }
    Box(modifier.fillMaxWidth().height(height).legacyBackground(drawable))
}

/** Widget.TextView.ListSeparator: 25dp dithered header, 14sp bold, 5px-equivalent inset. */
@Composable
internal fun EclairCategory(title: String, modifier: Modifier = Modifier) {
    val colors = LocalEclairColors.current
    Box(modifier.fillMaxWidth().heightIn(min = 25.dp)
        .legacyBackground(rememberClassicDrawable(if (colors.dark) R.drawable.eclair_dark_header_dither else R.drawable.eclair_light_header_dither))
        .padding(horizontal = AppUiTheme.spacing.contentInset).semantics { heading() }, contentAlignment = Alignment.CenterStart,
    ) { BasicText(title, style = eclairTextStyle(14, true).copy(color = if (colors.dark) colors.secondary else Color.White)) }
}

@Composable
internal fun EclairSettingsRow(
    title: String, modifier: Modifier = Modifier, summary: String? = null,
    titleMaxLines: Int = 1, summaryMaxLines: Int = 2,
    titleColor: Color = Color.Unspecified, summaryColor: Color = Color.Unspecified,
    onClick: (() -> Unit)? = null, leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val state = legacyControlState(interaction)
    val colors = LocalEclairColors.current
    val primary = eclairColor(if (colors.dark) R.color.eclair_primary_text_dark else R.color.eclair_primary_text_light, state)
    val secondary = eclairColor(if (colors.dark) R.color.eclair_secondary_text_dark else R.color.eclair_secondary_text_light, state)
    Row(modifier.fillMaxWidth().heightIn(min = 64.dp)
        .legacyBackground(rememberClassicDrawable(R.drawable.eclair_list_selector_background), state)
        .eclairListDivider()
        .then(if (onClick != null) Modifier.clickable(interaction, null, onClick = onClick) else Modifier)
        .padding(horizontal = AppUiTheme.spacing.contentInset), verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) { Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) { leading() }; Spacer(Modifier.width(6.dp)) }
        Column(Modifier.weight(1f).padding(vertical = 6.dp)) {
            BasicText(title, style = eclairTextStyle(22).copy(color = if (titleColor == Color.Unspecified) primary else titleColor),
                maxLines = titleMaxLines, overflow = TextOverflow.Ellipsis)
            if (summary != null) BasicText(summary, style = eclairTextStyle(14).copy(color = if (summaryColor == Color.Unspecified) secondary else summaryColor),
                maxLines = summaryMaxLines, overflow = TextOverflow.Ellipsis)
        }
        if (trailing != null) { Spacer(Modifier.width(6.dp)); trailing() }
    }
}

@Composable
internal fun EclairChoiceRow(
    title: String, subtitle: String?, selected: Boolean, onSelect: () -> Unit,
    modifier: Modifier = Modifier, leading: (@Composable () -> Unit)? = null,
    trailingAction: (@Composable () -> Unit)? = null, titleMaxLines: Int = 2, subtitleMaxLines: Int = 2,
) {
    val interaction = remember { MutableInteractionSource() }
    val state = legacyControlState(interaction, checked = selected)
    val colors = LocalEclairColors.current
    val primary = eclairColor(if (colors.dark) R.color.eclair_primary_text_dark else R.color.eclair_primary_text_light, state)
    val secondary = eclairColor(if (colors.dark) R.color.eclair_secondary_text_dark else R.color.eclair_secondary_text_light, state)
    Row(modifier.fillMaxWidth().heightIn(min = 64.dp)
        .legacyBackground(rememberClassicDrawable(R.drawable.eclair_list_selector_background), state).eclairListDivider()
        .selectable(selected, interaction, null, role = Role.RadioButton, onClick = onSelect)
        .padding(horizontal = AppUiTheme.spacing.contentInset, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) { leading(); Spacer(Modifier.width(6.dp)) }
        Column(Modifier.weight(1f)) {
            BasicText(title, style = eclairTextStyle(22).copy(color = primary), maxLines = titleMaxLines, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) BasicText(subtitle, style = eclairTextStyle(14).copy(color = secondary), maxLines = subtitleMaxLines, overflow = TextOverflow.Ellipsis)
        }
        trailingAction?.invoke()
        EclairRadioButton(selected, null)
    }
}
