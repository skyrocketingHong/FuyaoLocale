package ing.fuyaoskyrocket.applocale.ui.designsystem.lollipop

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.*

@Composable
internal fun LollipopSelectionBar(selectedCount: Int, onClose: () -> Unit, onSelectAll: () -> Unit,
    onClear: () -> Unit, onApply: (() -> Unit)?, isApplying: Boolean) {
    LollipopToolbar(
        title = { LollipopToolbarTitle(stringResource(R.string.selected_count, selectedCount)) },
        navigation = { AppToolbarIconButton(AppSymbol.Close, stringResource(R.string.exit_selection), onClose) },
        actions = {
            if (isApplying) LollipopProgress()
            else if (onApply != null) LollipopButton(stringResource(R.string.set_language), onApply,
                enabled = selectedCount > 0, borderless = true)
            Box {
                var menuOpen by remember { mutableStateOf(false) }
                AppToolbarIconButton(AppSymbol.Menu, stringResource(R.string.more_actions), { menuOpen = true })
                LollipopMenu(menuOpen, { menuOpen = false }, listOf(
                    AppDropdownItem(stringResource(R.string.select_all), onClick = onSelectAll),
                    AppDropdownItem(stringResource(R.string.clear_selection), onClick = onClear)))
            }
        })
}
