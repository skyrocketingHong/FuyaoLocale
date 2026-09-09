package ing.fuyaoskyrocket.applocale.ui.designsystem.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.ui.designsystem.*

@Composable
fun AppReorderRow(title: String, subtitle: String, leading: @Composable () -> Unit, trailing: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = if (AppUiTheme.policy.continuousLists) AppUiTheme.metrics.listPreferredItemHeight else AppLayout.appListItemMinHeight).appListDivider(),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        leading()
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            AppText(title, style = AppUiTheme.textStyles.itemTitle, maxLines = 2, overflow = TextOverflow.Ellipsis)
            AppText(subtitle, style = AppUiTheme.textStyles.metadata, color = AppUiTheme.palette.muted,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        trailing()
    }
}

/** Presentation-only drag copy; the feature owns coordinates, size and reorder state. */
@Composable
fun AppDragSurface(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val flat = AppUiTheme.policy.continuousLists
    val shape = RoundedCornerShape(AppUiTheme.shapes.row.radius)
    AppSurface(modifier.graphicsLayer { shadowElevation = if (flat) 0f else 6.dp.toPx(); this.shape = shape },
        shape = shape, color = if (flat) Color.Transparent else AppUiTheme.palette.secondarySurface, content = content)
}
