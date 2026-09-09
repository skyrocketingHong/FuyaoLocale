package ing.fuyaoskyrocket.applocale.ui.designsystem.component

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.ToolbarActionPresentation

data class AppToolbarAction(
    val label: String,
    val onClick: () -> Unit,
    val symbol: AppSymbol? = null,
    val icon: ImageVector? = null,
    val enabled: Boolean = true,
    val loading: Boolean = false,
    val refreshAlternative: Boolean = false,
)

/** Refresh alternatives appear where pull-to-refresh is unavailable. */
@Composable
fun AppToolbarActions(actions: List<AppToolbarAction>) {
    val visible = actions.filter { !it.refreshAlternative || AppUiTheme.components.toolbarActions == ToolbarActionPresentation.Menu }
    if (AppUiTheme.components.toolbarActions == ToolbarActionPresentation.Menu) {
        var expanded by remember { mutableStateOf(false) }
        if (visible.any { it.loading }) AppCircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
        Box {
            AppToolbarIconButton(AppSymbol.Menu, stringResource(R.string.more_actions), { expanded = true }, enabled = visible.any { it.enabled && !it.loading })
            AppDropdownMenu(expanded, { expanded = false }, visible.map { action ->
                AppDropdownItem(text = action.label, onClick = { expanded = false; action.onClick() }, enabled = action.enabled && !action.loading)
            })
        }
    } else visible.forEach { action ->
        AppIconButton(action.onClick, enabled = action.enabled && !action.loading) {
            when {
                action.loading -> AppCircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                action.symbol != null -> AppSymbolIcon(action.symbol, action.label)
                action.icon != null -> AppIcon(action.icon, action.label)
                else -> AppText(action.label)
            }
        }
    }
}
