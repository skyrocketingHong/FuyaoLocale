package ing.fuyaoskyrocket.applocale.ui.components

import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppFilterControls
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppFilterOption
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.model.AppListSortOption
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppDropdownItem
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSymbol
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSymbolVector

/**
 * Shared controls for all installed-app lists. Every sort option is a chip in one
 * horizontally scrolling row (no dropdown menu). Tapping a chip cycles its sort state:
 * unselected → ascending → descending → cancelled; the active direction rides in front
 * of the chip's label as an arrow, so there is no separate direction button. The chips
 * come from the theme-aware wrappers so every style renders its native control.
 */
@Composable
fun AppFilterBar(
    modifiedOnly: Boolean,
    modifiedCount: Int,
    sortOption: AppListSortOption?,
    sortAscending: Boolean,
    onToggleModifiedOnly: () -> Unit,
    onCycleSortOption: (AppListSortOption) -> Unit,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = AppSpacing.screenCompact,
    onSetSort: ((AppListSortOption?, Boolean) -> Unit)? = null,
) {
    val none = stringResource(R.string.sort_direction_none)
    val ascending = stringResource(R.string.sort_direction_ascending)
    val descending = stringResource(R.string.sort_direction_descending)
    val modifiedLabel = stringResource(R.string.modified_count, modifiedCount)
    val sortLabels = AppListSortOption.entries.associateWith { stringResource(it.labelRes()) }
    val current = sortOption?.let { sortLabels.getValue(it) + " · " + if (sortAscending) ascending else descending } ?: none
    AppFilterControls(
        currentLabel = current,
        modifier = modifier,
        horizontalPadding = horizontalPadding,
        options = listOf(AppFilterOption("modified", modifiedLabel, modifiedOnly, onToggleModifiedOnly,
            icon = if (modifiedOnly) AppSymbolVector(AppSymbol.Check) else null)) + AppListSortOption.entries.map { option ->
            val selected = sortOption == option
            AppFilterOption(option.name, sortLabels.getValue(option), selected, { onCycleSortOption(option) },
                icon = if (!selected) null else if (sortAscending) Icons.Outlined.ArrowUpward else Icons.Outlined.ArrowDownward,
                state = if (!selected) none else if (sortAscending) ascending else descending)
        },
        menuItems = if (onSetSort == null) emptyList() else buildList {
            add(AppDropdownItem(modifiedLabel, modifiedOnly, onClick = onToggleModifiedOnly))
            add(AppDropdownItem(none, sortOption == null, onClick = { onSetSort(null, true) }))
            AppListSortOption.entries.forEach { option ->
                add(AppDropdownItem(sortLabels.getValue(option), sortOption == option && sortAscending, onClick = { onSetSort(option, true) }))
                if (sortOption == option) add(AppDropdownItem(descending, !sortAscending, onClick = { onSetSort(option, false) }))
            }
        },
    )
}

@Composable
private fun AppListSortOption.labelRes(): Int = when (this) {
    AppListSortOption.AppName -> R.string.sort_by_app_name
    AppListSortOption.PackageName -> R.string.sort_by_package_name
    AppListSortOption.Locale -> R.string.sort_by_language
    AppListSortOption.Modified -> R.string.sort_by_modified
    AppListSortOption.AppType -> R.string.sort_by_app_type
}
