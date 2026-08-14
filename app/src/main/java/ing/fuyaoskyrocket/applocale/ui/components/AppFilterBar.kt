package ing.fuyaoskyrocket.applocale.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.Dp
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.model.AppListSortOption
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing

/**
 * Shared controls for all installed-app lists. They keep filter and sort state
 * separate from list rendering so the same query model can be reused elsewhere.
 */
@Composable
fun AppFilterBar(
    modifiedOnly: Boolean,
    modifiedCount: Int,
    sortOption: AppListSortOption,
    sortAscending: Boolean,
    onToggleModifiedOnly: () -> Unit,
    onSelectSortOption: (AppListSortOption) -> Unit,
    onToggleSortDirection: () -> Unit,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = AppSpacing.screenCompact,
) {
    var showSortMenu by rememberSaveable { mutableStateOf(false) }

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(
            horizontal = horizontalPadding,
            vertical = AppSpacing.xs,
        ),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        item(key = "modified_filter") {
            FilterChip(
                selected = modifiedOnly,
                onClick = onToggleModifiedOnly,
                label = { Text(stringResource(R.string.modified_count, modifiedCount)) },
                leadingIcon = if (modifiedOnly) {
                    {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                        )
                    }
                } else {
                    null
                },
            )
        }
        item(key = "sort_menu") {
            Box {
                AssistChip(
                    onClick = { showSortMenu = true },
                    label = { Text(stringResource(sortOption.labelRes())) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Sort,
                            contentDescription = null,
                        )
                    },
                )
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false },
                ) {
                    AppListSortOption.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(stringResource(option.labelRes())) },
                            onClick = {
                                onSelectSortOption(option)
                                showSortMenu = false
                            },
                            trailingIcon = {
                                if (option == sortOption) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = null,
                                    )
                                }
                            },
                        )
                    }
                }
            }
        }
        item(key = "sort_direction") {
            IconButton(onClick = onToggleSortDirection) {
                Icon(
                    imageVector = if (sortAscending) {
                        Icons.Outlined.ArrowUpward
                    } else {
                        Icons.Outlined.ArrowDownward
                    },
                    contentDescription = stringResource(R.string.toggle_sort_direction),
                )
            }
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
