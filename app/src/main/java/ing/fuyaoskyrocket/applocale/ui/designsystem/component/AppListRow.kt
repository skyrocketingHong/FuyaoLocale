package ing.fuyaoskyrocket.applocale.ui.designsystem.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.ui.components.highlightedText
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.legacyControlState

/** A continuous record row. Callers supply facts and actions, never a theme identity. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    query: String = "",
    spacious: Boolean = false,
    selected: Boolean = false,
    selectionMode: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    leading: (@Composable (Modifier) -> Unit)? = null,
    leadingSize: Dp = 48.dp,
    metaLabel: String? = null,
    detail: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val continuous = AppUiTheme.policy.continuousLists
    val palette = AppUiTheme.palette
    val metrics = AppUiTheme.metrics
    val interaction = remember { MutableInteractionSource() }
    val state = legacyControlState(interaction, checked = selected, selected = selected, activated = selected)
    val primary = if (continuous) appRowPrimary(state) else if (selected) palette.onSelected else palette.surfaceContent
    val secondary = if (continuous) appRowSecondary(state) else if (selected) palette.onSelected else palette.muted
    val highlight = if (continuous || selected) SpanStyle(color = primary, fontWeight = FontWeight.Bold)
        else SpanStyle(background = palette.selected, color = palette.onSelected)
    val highlightedTitle = remember(title, query, highlight) { highlightedText(title, query, highlight) }
    val highlightedSubtitle = remember(subtitle, query, highlight) { subtitle?.let { highlightedText(it, query, highlight) } }
    val shape = RoundedCornerShape(AppUiTheme.shapes.row.radius)
    val frame = modifier.fillMaxWidth().heightIn(min = if (!continuous && spacious) 80.dp else metrics.listPreferredItemHeightLarge)
    val painted = if (continuous) frame.appContinuousRow(state, interaction, activated = true)
        else frame.clip(shape).background(if (selected) palette.selected else Color.Transparent)
    val clickable = if (onClick == null) painted else if (continuous) painted.combinedClickable(
        interactionSource = interaction, indication = null, onClick = onClick, onLongClick = onLongClick,
    ) else painted.combinedClickable(onClick = onClick, onLongClick = onLongClick)
    Row(
        modifier = clickable.then(if (selectionMode) Modifier.semantics {
            role = Role.Checkbox
            toggleableState = ToggleableState(selected)
        } else Modifier).padding(horizontal = if (continuous) appListContentInset() else AppUiTheme.spacing.contentInset - AppUiTheme.spacing.rowOuterInset, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            Column(Modifier.width(leadingSize), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                leading(Modifier.size(leadingSize))
                if (!continuous && metaLabel != null) AppText(metaLabel, style = AppUiTheme.textStyles.label, color = secondary, maxLines = 1)
            }
            Spacer(Modifier.width(if (continuous) 8.dp else 12.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            AppText(highlightedTitle, style = AppUiTheme.textStyles.itemTitle, color = primary,
                maxLines = if (LocalDensity.current.fontScale > 1.3f) 2 else 1, overflow = TextOverflow.Ellipsis)
            if (highlightedSubtitle != null) AppText(highlightedSubtitle,
                style = AppUiTheme.textStyles.metadata, color = secondary,
                maxLines = if (continuous) 1 else 2, overflow = TextOverflow.Ellipsis)
            if (detail != null || (continuous && metaLabel != null)) Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) { detail?.invoke() }
                if (continuous && metaLabel != null) AppText(metaLabel, Modifier.padding(start = 8.dp),
                    style = AppUiTheme.textStyles.metadata, color = secondary, maxLines = 1)
            }
        }
        if (selectionMode) Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            AppCheckbox(checked = selected, onCheckedChange = null)
        } else trailing?.invoke()
    }
}

/** Optional navigation/progress affordance; era rows rely on the full-row target. */
@Composable
fun AppRowAffordance(processing: Boolean = false, navigable: Boolean = true, expanded: Boolean = false) {
    if (!processing && (!navigable || AppUiTheme.policy.continuousLists)) return
    Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
        if (processing) AppCircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
        else AppSymbolIcon(AppSymbol.Forward, contentDescription = null, tint = AppUiTheme.palette.muted, modifier = Modifier.size(24.dp).graphicsLayer { rotationZ = if (expanded) 90f else 0f })
    }
}
