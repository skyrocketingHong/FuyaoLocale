package ing.fuyaoskyrocket.applocale.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ing.fuyaoskyrocket.applocale.data.repository.AppRepository
import ing.fuyaoskyrocket.applocale.data.repository.ApplyLocaleToAppsUseCase
import ing.fuyaoskyrocket.applocale.data.repository.LocaleChangeNotifier
import ing.fuyaoskyrocket.applocale.data.system.AppIconLoader
import ing.fuyaoskyrocket.applocale.model.AppModel
import ing.fuyaoskyrocket.applocale.model.AppListSortOption
import ing.fuyaoskyrocket.applocale.model.BatchApplyState
import ing.fuyaoskyrocket.applocale.model.OperationMode
import ing.fuyaoskyrocket.applocale.model.applyQuery
import ing.fuyaoskyrocket.applocale.service.PrivilegedServiceClient
import javax.inject.Inject

@HiltViewModel
@OptIn(kotlinx.coroutines.FlowPreview::class)
class MainViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val applyLocaleToApps: ApplyLocaleToAppsUseCase,
    private val localeChangeNotifier: LocaleChangeNotifier,
    private val serviceClient: PrivilegedServiceClient,
    val appIconLoader: AppIconLoader,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    /**
     * Pre-computed visible apps — filtering and search happen here (in Flow),
     * never inside `items()` via `return@items`.
     */
    private val visibleAppsFlow = combine(
        _uiState.map { it.apps }.distinctUntilChanged(),
        _uiState.map { it.listQuery.copy(query = "") }.distinctUntilChanged(),
        _uiState.map { it.query }.debounce(200).distinctUntilChanged(),
    ) { apps, queryOptions, query ->
        apps.applyQuery(queryOptions.copy(query = query))
    }

    init {
        val cachedApps = appRepository.getCachedApps()
        if (cachedApps != null) {
            _uiState.update {
                it.withApps(cachedApps).copy(
                    isLoading = false,
                    isRefreshingLocales = false,
                )
            }
        }

        // Reactive: keep visibleApps in sync
        viewModelScope.launch {
            visibleAppsFlow.collect { visible ->
                _uiState.update { it.copy(visibleApps = visible) }
            }
        }
        // Reactive: keep operationMode in sync
        viewModelScope.launch {
            serviceClient.operationModeFlow.collect { mode ->
                _uiState.update { it.copy(operationMode = mode) }
            }
        }
        // Detail pages and saved configurations can change locales while this screen is
        // off-screen. Refresh only the affected rows instead of rebuilding the full list.
        viewModelScope.launch {
            localeChangeNotifier.changes.collect(::refreshLocaleTags)
        }

        // Activity recreation (rotation or app-language change) reuses the application-scoped
        // snapshot. Package and Binder scans happen only on first load or explicit refresh.
        if (cachedApps == null) loadApps()
    }

    // ----------------------------------------------------------------- Loading

    fun loadApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isRefreshingLocales = false) }

            // 1. Publish basic app list immediately for scrollable UI
            val apps = appRepository.loadApps()
            _uiState.update { state ->
                state.withApps(apps).copy(
                    isLoading = false,
                    isRefreshingLocales = true,
                )
            }

            // 2. Batch query locale tags (single Binder round-trip)
            val tags = appRepository.fetchLocaleTags(apps.map { it.packageName })
            val completeApps = apps.withLocaleTags(tags)
            appRepository.replaceCachedApps(completeApps)
            _uiState.update {
                it.withApps(completeApps).copy(isRefreshingLocales = false)
            }
        }
    }

    /** User-initiated pull-to-refresh. Keeps existing rows visible while data reloads. */
    fun refreshApps() {
        if (_uiState.value.isLoading || _uiState.value.isRefreshingLocales) return
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshingLocales = true) }
            val apps = appRepository.loadApps()
            val tags = appRepository.fetchLocaleTags(apps.map { it.packageName })
            val completeApps = apps.withLocaleTags(tags)
            appRepository.replaceCachedApps(completeApps)
            _uiState.update {
                it.withApps(completeApps).copy(isRefreshingLocales = false)
            }
        }
    }

    private fun refreshLocaleTags(packages: Set<String>) {
        val trackedPackages = packages.intersect(_uiState.value.apps.map { it.packageName }.toSet())
        if (trackedPackages.isEmpty()) return

        viewModelScope.launch {
            val tags = appRepository.fetchLocaleTags(trackedPackages.toList())
            appRepository.updateCachedLocaleTags(tags)
            _uiState.update { state ->
                state.withApps(
                    state.apps.map { app ->
                        if (app.packageName in tags) {
                            app.copy(localeTag = tags[app.packageName]?.takeIf { it.isNotBlank() })
                        } else {
                            app
                        }
                    },
                )
            }
        }
    }

    // ------------------------------------------------------------- Search bar

    fun onQueryChange(text: String) {
        _uiState.update { state ->
            state.copy(listQuery = state.listQuery.copy(query = text))
        }
    }

    /** Shows the search input; the query, filters and list stay untouched. */
    fun openSearch() {
        _uiState.update { it.copy(isSearchExpanded = true) }
    }

    /**
     * Exits the search input and clears the query in one update; filters (modified
     * only, system apps) and sorting keep their current values.
     */
    fun closeSearch() {
        _uiState.update { state ->
            state.copy(
                isSearchExpanded = false,
                listQuery = state.listQuery.copy(query = ""),
            )
        }
    }

    // -------------------------------------------------------------- Filter chips

    fun toggleModifiedOnly() {
        _uiState.update { state ->
            state.copy(listQuery = state.listQuery.copy(modifiedOnly = !state.modifiedOnly))
        }
    }

    fun toggleShowSystemApps() {
        _uiState.update { state ->
            state.copy(listQuery = state.listQuery.copy(showSystemApps = !state.showSystemApps))
        }
    }

    /**
     * Tri-state sort cycle driven by tapping a sort chip: an unselected option starts
     * ascending, the selected option flips to descending, and tapping it again cancels
     * the sort (null option → natural repository order).
     */
    fun cycleSortOption(option: AppListSortOption) {
        _uiState.update { state ->
            val query = state.listQuery
            val newQuery = when {
                query.sortOption != option -> query.copy(sortOption = option, sortAscending = true)
                query.sortAscending -> query.copy(sortAscending = false)
                else -> query.copy(sortOption = null, sortAscending = true)
            }
            state.copy(listQuery = newQuery)
        }
    }

    fun clearFilters() {
        _uiState.update { state ->
            state.copy(
                listQuery = state.listQuery.copy(
                    query = "",
                    modifiedOnly = false,
                    showSystemApps = true,
                ),
            )
        }
    }

    // ----------------------------------------------------------- Selection mode

    fun toggleSelection(packageName: String) {
        _uiState.update { state ->
            val newSelection = if (packageName in state.selectedPackages) {
                state.selectedPackages - packageName
            } else {
                state.selectedPackages + packageName
            }
            state.copy(selectedPackages = newSelection)
        }
    }

    /** Select everything currently visible — avoids silently selecting hidden system apps. */
    fun selectAllVisible() {
        _uiState.update { state ->
            state.copy(
                selectedPackages = state.selectedPackages +
                    state.visibleApps.map { it.packageName }.toSet()
            )
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedPackages = emptySet()) }
    }

    // ---------------------------------------------------------- Batch operations

    /**
     * Apply [localeTag] (or system default if null/blank) to all selected packages.
     * After completion, refreshes locale tags and exits selection mode on full success.
     */
    fun applyBatchLocale(localeTag: String?) {
        val packages = _uiState.value.selectedPackages.toList()
        if (packages.isEmpty()) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(batchState = BatchApplyState.Applying(packages.size, 0))
            }

            val result = applyLocaleToApps(packages, localeTag)

            // Refresh locale tags for affected packages
            val tags = appRepository.fetchLocaleTags(packages)
            appRepository.updateCachedLocaleTags(tags)
            val failedSet = result.failedPackages.toSet()
            _uiState.update { state ->
                val updatedApps = state.apps.map { app ->
                    if (app.packageName in tags) {
                        app.copy(localeTag = tags[app.packageName]?.takeIf { it.isNotBlank() })
                    } else {
                        app
                    }
                }
                state.withApps(updatedApps).copy(
                    // Keep only failed packages selected
                    selectedPackages = if (result.isAllSuccess) emptySet() else failedSet,
                    batchState = BatchApplyState.Done(result),
                )
            }
        }
    }

    fun consumeBatchResult() {
        _uiState.update { it.copy(batchState = BatchApplyState.Idle) }
    }

}

private fun List<AppModel>.withLocaleTags(tags: Map<String, String>): List<AppModel> =
    map { app ->
        app.copy(localeTag = tags[app.packageName]?.takeIf { it.isNotBlank() })
    }

private fun MainUiState.withApps(apps: List<AppModel>): MainUiState =
    copy(
        apps = apps,
        modifiedCount = apps.count { it.isModified },
    )
