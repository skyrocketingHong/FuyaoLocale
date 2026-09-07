package ing.fuyaoskyrocket.applocale.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Deselect
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppIcon
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppIconButton
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSymbol
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSymbolVector
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppTopAppBar

/**
 * Contextual top app bar shown during multi-selection. Exit and select-all
 * resolve their native glyphs by symbol; miuix has no deselect glyph, so that
 * one stays a project vector drawn by the backend icon control (asset
 * exception, see the 012 execution record).
 */
@Composable
fun SelectionTopAppBar(
    selectedCount: Int,
    onClose: () -> Unit,
    onSelectAll: () -> Unit,
    onClear: () -> Unit,
) {
    AppTopAppBar(
        navigationIcon = {
            AppIconButton(onClick = onClose) {
                AppIcon(
                    imageVector = AppSymbolVector(AppSymbol.Close),
                    contentDescription = stringResource(R.string.exit_selection),
                )
            }
        },
        title = stringResource(R.string.selected_count, selectedCount),
        actions = {
            AppIconButton(onClick = onSelectAll) {
                AppIcon(
                    imageVector = AppSymbolVector(AppSymbol.SelectAll),
                    contentDescription = stringResource(R.string.select_all),
                )
            }
            AppIconButton(onClick = onClear) {
                AppIcon(
                    imageVector = Icons.Outlined.Deselect,
                    contentDescription = stringResource(R.string.clear_selection),
                )
            }
        },
    )
}
