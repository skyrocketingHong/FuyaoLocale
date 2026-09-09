package ing.fuyaoskyrocket.applocale.ui.designsystem.component

import ing.fuyaoskyrocket.applocale.ui.designsystem.AppControlFamily
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Deselect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppDropdownItem
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppDropdownMenu
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppIcon
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppIconButton
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSymbol
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSymbolVector
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSymbolIcon
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppTextButton
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppToolbarIconButton
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppTopAppBar
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloContextualBar

/**
 * Contextual top bar for multi-selection. The modern styles keep the compact
 * selection top bar (exit / select-all / clear); Holo renders the era
 * Contextual Action Bar in the same slot with the batch action lifted into
 * the bar ([onApply]) — the modern bottom batch bar does not compose there.
 */
@Composable
fun SelectionTopAppBar(
    selectedCount: Int,
    onClose: () -> Unit,
    onSelectAll: () -> Unit,
    onClear: () -> Unit,
    onApply: (() -> Unit)? = null,
    isApplying: Boolean = false,
) {
    if (AppUiTheme.policy.controls == AppControlFamily.Lollipop) {
        ing.fuyaoskyrocket.applocale.ui.designsystem.lollipop.LollipopSelectionBar(
            selectedCount, onClose, onSelectAll, onClear, onApply, isApplying)
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Eclair) {
        ing.fuyaoskyrocket.applocale.ui.designsystem.eclair.EclairTitleBar(stringResource(R.string.app_name))
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Holo) {
        HoloSelectionTopAppBar(
            selectedCount = selectedCount,
            onClose = onClose,
            onSelectAll = onSelectAll,
            onClear = onClear,
            onApply = onApply,
            isApplying = isApplying,
        )
        return
    }
    AppTopAppBar(
        navigationIcon = {
            AppIconButton(onClick = onClose) {
                AppSymbolIcon(
                    symbol = AppSymbol.Close,
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

/**
 * The Holo CAB: close affordance, selected-count title, the batch action as a
 * borderless ICS text button (disabled without a selection), and select-all /
 * clear behind the overflow. Applying swaps the action row for the era
 * circular progress.
 */
@Composable
private fun HoloSelectionTopAppBar(
    selectedCount: Int,
    onClose: () -> Unit,
    onSelectAll: () -> Unit,
    onClear: () -> Unit,
    onApply: (() -> Unit)?,
    isApplying: Boolean,
) {
    HoloContextualBar(
        title = stringResource(R.string.selected_count, selectedCount),
        onClose = onClose,
        isApplying = isApplying,
        primaryAction = if (onApply != null) {
            {
                AppTextButton(
                    text = stringResource(R.string.set_language),
                    onClick = onApply,
                    enabled = selectedCount > 0,
                )
            }
        } else {
            null
        },
        overflow = {
            Box {
                var menuExpanded by remember { mutableStateOf(false) }
                AppToolbarIconButton(
                    symbol = AppSymbol.Menu,
                    contentDescription = stringResource(R.string.more_actions),
                    onClick = { menuExpanded = true },
                )
                AppDropdownMenu(
                    expanded = menuExpanded,
                    onDismiss = { menuExpanded = false },
                    items = listOf(
                        AppDropdownItem(
                            text = stringResource(R.string.select_all),
                            onClick = {
                                menuExpanded = false
                                onSelectAll()
                            },
                        ),
                        AppDropdownItem(
                            text = stringResource(R.string.clear_selection),
                            onClick = {
                                menuExpanded = false
                                onClear()
                            },
                        ),
                    ),
                )
            }
        },
    )
}
