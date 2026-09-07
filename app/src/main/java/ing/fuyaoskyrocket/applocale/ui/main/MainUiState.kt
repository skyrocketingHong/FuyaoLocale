package ing.fuyaoskyrocket.applocale.ui.main

import ing.fuyaoskyrocket.applocale.model.AppModel
import ing.fuyaoskyrocket.applocale.model.AppListQuery
import ing.fuyaoskyrocket.applocale.model.AppListSortOption
import ing.fuyaoskyrocket.applocale.model.BatchApplyState
import ing.fuyaoskyrocket.applocale.model.OperationMode

/**
 * Immutable home-screen UI state.
 * [selectedPackages] being non-empty implicitly means "selection mode" —
 * no separate `isSelectionMode` flag is needed.
 */
data class MainUiState(
    val apps: List<AppModel> = emptyList(),
    val visibleApps: List<AppModel> = emptyList(),
    val listQuery: AppListQuery = AppListQuery(),
    val modifiedCount: Int = 0,
    val selectedPackages: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val isRefreshingLocales: Boolean = false,
    val batchState: BatchApplyState = BatchApplyState.Idle,
    val operationMode: OperationMode = OperationMode.NONE,
    /** The search input is shown because the user opened it (query may still be blank). */
    val isSearchExpanded: Boolean = false,
) {
    val query: String
        get() = listQuery.query

    val modifiedOnly: Boolean
        get() = listQuery.modifiedOnly

    val showSystemApps: Boolean
        get() = listQuery.showSystemApps

    val sortOption: AppListSortOption?
        get() = listQuery.sortOption

    val sortAscending: Boolean
        get() = listQuery.sortAscending

    val isSelectionMode: Boolean
        get() = selectedPackages.isNotEmpty()

    /**
     * The search UI is active when opened, even with a blank query — but a
     * restored non-blank query must keep it visible with the input collapsed.
     */
    val isSearchActive: Boolean
        get() = isSearchExpanded || query.isNotBlank()

    val hasActiveFilters: Boolean
        get() = modifiedOnly || !showSystemApps || query.isNotBlank()
}
