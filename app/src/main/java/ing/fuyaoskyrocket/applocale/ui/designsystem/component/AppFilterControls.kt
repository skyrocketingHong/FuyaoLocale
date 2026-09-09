package ing.fuyaoskyrocket.applocale.ui.designsystem.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.FilterPresentation

data class AppFilterOption(
    val key: String,
    val label: String,
    val selected: Boolean,
    val onClick: () -> Unit,
    val icon: ImageVector? = null,
    val state: String? = null,
)

@Composable
fun AppFilterControls(
    currentLabel: String,
    options: List<AppFilterOption>,
    menuItems: List<AppDropdownItem>,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 0.dp,
) {
    if (AppUiTheme.components.filters == FilterPresentation.Dropdown && menuItems.isNotEmpty()) {
        var expanded by remember { mutableStateOf(false) }
        Box(modifier.padding(horizontal = horizontalPadding)) {
            AppDropdownButton(currentLabel, { expanded = true })
            AppDropdownMenu(expanded, { expanded = false }, menuItems.map { item ->
                item.copy(onClick = { expanded = false; item.onClick() })
            })
        }
    } else LazyRow(modifier, contentPadding = PaddingValues(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(AppUiTheme.spacing.filterGap), verticalAlignment = Alignment.CenterVertically) {
        items(options.size, key = { options[it].key }) { index ->
            val item = options[index]
            AppFilterChip(item.selected, item.onClick, item.label,
                modifier = Modifier.semantics { item.state?.let { stateDescription = it } }, leadingIcon = item.icon)
        }
    }
}
