package ing.fuyaoskyrocket.applocale.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.model.AppListSortOption
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppFilterChip
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppIconButton
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
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        item(key = "modified_filter") {
            AppFilterChip(
                selected = modifiedOnly,
                onClick = onToggleModifiedOnly,
                label = stringResource(R.string.modified_count, modifiedCount),
                leadingIcon = if (modifiedOnly) AppSymbolVector(AppSymbol.Check) else null,
            )
        }
        items(
            count = AppListSortOption.entries.size,
            key = { index -> AppListSortOption.entries[index] },
        ) { index ->
            val option = AppListSortOption.entries[index]
            val selected = option == sortOption
            // Announce the tri-state cycle's current direction; the selected flag
            // alone cannot tell ascending from descending.
            val directionDescription = when {
                !selected -> stringResource(R.string.sort_direction_none)
                sortAscending -> stringResource(R.string.sort_direction_ascending)
                else -> stringResource(R.string.sort_direction_descending)
            }
            // miuix has no single-direction arrow glyphs; the sort direction
            // vectors stay project assets drawn by the chip's backend icon
            // control (asset exception, see the 012 execution record).
            AppFilterChip(
                selected = selected,
                onClick = { onCycleSortOption(option) },
                label = stringResource(option.labelRes()),
                leadingIcon = if (selected) {
                    if (sortAscending) {
                        Icons.Outlined.ArrowUpward
                    } else {
                        Icons.Outlined.ArrowDownward
                    }
                } else {
                    null
                },
                modifier = Modifier.semantics {
                    stateDescription = directionDescription
                },
            )
        }
    }
}

@Composable
private fun AppListSortOption.labelRes(): Int = when (this) {
    AppListSortOption.AppName -> R.string.sort_by_app_name
    AppListSortOption.PackageName -> R.string.sort_by_package_name
    AppListSortOption.Locale -> R.string.sort_by_language
    AppListSortOption.Modified -> R.string.sort_by_modified
    AppListSortOption.AppType -> R.string.sort_by_app_type
}
