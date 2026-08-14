package ing.fuyaoskyrocket.applocale.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.FilterAltOff
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.model.BatchApplyState
import ing.fuyaoskyrocket.applocale.model.OperationMode
import ing.fuyaoskyrocket.applocale.ui.components.AppFilterBar
import ing.fuyaoskyrocket.applocale.ui.components.AppResultsList
import ing.fuyaoskyrocket.applocale.ui.components.AppNavigationBar
import ing.fuyaoskyrocket.applocale.ui.components.AppNavigationDestination
import ing.fuyaoskyrocket.applocale.ui.components.AppTopAppBarTitle
import ing.fuyaoskyrocket.applocale.ui.components.BatchBottomAppBar
import ing.fuyaoskyrocket.applocale.ui.components.SelectionTopAppBar
import ing.fuyaoskyrocket.applocale.ui.components.ShizukuConnectingState
import ing.fuyaoskyrocket.applocale.ui.components.ShizukuRequiredWarning
import ing.fuyaoskyrocket.applocale.ui.components.SystemDialogWarn
import ing.fuyaoskyrocket.applocale.ui.components.predictiveBackTransform
import ing.fuyaoskyrocket.applocale.ui.components.rememberPredictiveBackMotion
import ing.fuyaoskyrocket.applocale.ui.components.rememberSystemLocaleTag
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing
import ing.fuyaoskyrocket.applocale.ui.designsystem.readableContentWidth
import ing.fuyaoskyrocket.applocale.ui.languagepicker.BatchLanguageSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    navigateToAppScreen: (String) -> Unit,
    navigateToConfigurations: () -> Unit,
    navigateToAbout: () -> Unit,
    hasGrantedShizukuPermission: Boolean,
    onRequestShizukuPermission: () -> Unit,
    onOpenShizuku: () -> Unit,
    showBottomNavigation: Boolean = true,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val lazyListState = rememberLazyListState()
    val systemLocaleTag = rememberSystemLocaleTag()

    var showBatchSheet by remember { mutableStateOf(false) }
    var showSystemWarning by remember { mutableStateOf(false) }

    val predictiveBackMotion = rememberPredictiveBackMotion(
        enabled = uiState.isSelectionMode || uiState.query.isNotBlank(),
        onBack = {
            when {
                uiState.isSelectionMode -> viewModel.clearSelection()
                uiState.query.isNotBlank() -> viewModel.onQueryChange("")
            }
        },
    )

    val listPresentationKey = listOf(
        uiState.query,
        uiState.modifiedOnly,
        uiState.showSystemApps,
        uiState.sortOption,
        uiState.sortAscending,
    ).joinToString(separator = "|")
    var previousListPresentationKey by rememberSaveable {
        mutableStateOf(listPresentationKey)
    }
    LaunchedEffect(listPresentationKey) {
        if (previousListPresentationKey != listPresentationKey) {
            lazyListState.scrollToItem(0)
            previousListPresentationKey = listPresentationKey
        }
    }

    // Snackbar for batch results
    LaunchedEffect(uiState.batchState) {
        val batch = uiState.batchState
        if (batch is BatchApplyState.Done) {
            val msg = if (batch.result.isAllSuccess) {
                ctx.getString(R.string.batch_success, batch.result.successCount)
            } else {
                ctx.getString(
                    R.string.batch_partial,
                    batch.result.successCount,
                    batch.result.failedPackages.size,
                )
            }
            snackbarHostState.showSnackbar(msg)
            viewModel.consumeBatchResult()
        }
    }

    val selectedApps = remember(uiState.apps, uiState.selectedPackages) {
        uiState.apps.filter { it.packageName in uiState.selectedPackages }
    }
    val hasSystemAppInSelection = selectedApps.any { it.isSystemApp }
    val horizontalPagePadding = if (showBottomNavigation) {
        AppSpacing.screenCompact
    } else {
        AppSpacing.screenExpanded
    }

    Scaffold(
        modifier = Modifier.predictiveBackTransform(predictiveBackMotion),
        topBar = {
            if (uiState.isSelectionMode) {
                SelectionTopAppBar(
                    selectedCount = uiState.selectedPackages.size,
                    onClose = { viewModel.clearSelection() },
                    onSelectAll = { viewModel.selectAllVisible() },
                    onClear = { viewModel.clearSelection() },
                )
            } else {
                TopAppBar(
                    title = {
                        AppTopAppBarTitle(
                            title = stringResource(R.string.app_name),
                        )
                    },
                    actions = {
                        IconButton(onClick = viewModel::refreshApps) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = stringResource(R.string.refresh),
                            )
                        }
                        IconButton(onClick = viewModel::toggleShowSystemApps) {
                            Icon(
                                imageVector = Icons.Outlined.Apps,
                                contentDescription = stringResource(R.string.show_system_apps),
                                tint = if (uiState.showSystemApps) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        },
        bottomBar = {
            if (uiState.isSelectionMode) {
                BatchBottomAppBar(
                    hasSelection = uiState.selectedPackages.isNotEmpty(),
                    isApplying = uiState.batchState is BatchApplyState.Applying,
                    onClick = {
                        if (hasSystemAppInSelection) {
                            showSystemWarning = true
                        } else {
                            showBatchSheet = true
                        }
                    },
                )
            } else if (showBottomNavigation) {
                AppNavigationBar(
                    currentDestination = AppNavigationDestination.Home,
                    onHomeClick = {},
                    onConfigurationsClick = navigateToConfigurations,
                    onAboutClick = navigateToAbout,
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Search edits the single primary result list; no secondary result surface.
                    SearchBar(
                        inputField = {
                            SearchBarDefaults.InputField(
                                query = uiState.query,
                                onQueryChange = viewModel::onQueryChange,
                                onSearch = { viewModel.onQueryChange(it) },
                                expanded = false,
                                onExpandedChange = {},
                                enabled = true,
                                placeholder = { Text(stringResource(R.string.search)) },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null)
                                },
                                trailingIcon = {
                                    if (uiState.query.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = stringResource(R.string.clear),
                                            )
                                        }
                                    }
                                }
                            )
                        },
                        expanded = false,
                        onExpandedChange = {},
                        // Scaffold and TopAppBar own the system inset. Applying it again here
                        // creates the empty strip that used to appear above the search field.
                        windowInsets = WindowInsets(0, 0, 0, 0),
                        modifier = Modifier
                            .readableContentWidth()
                            .padding(horizontal = horizontalPagePadding),
                    ) {}

                    // A single horizontally scrollable row prevents long translated chip
                    // labels from wrapping or being clipped.
                    AppFilterBar(
                        modifiedOnly = uiState.modifiedOnly,
                        modifiedCount = uiState.modifiedCount,
                        sortOption = uiState.sortOption,
                        sortAscending = uiState.sortAscending,
                        onToggleModifiedOnly = { viewModel.toggleModifiedOnly() },
                        onSelectSortOption = viewModel::selectSortOption,
                        onToggleSortDirection = viewModel::toggleSortDirection,
                        horizontalPadding = horizontalPagePadding,
                        modifier = Modifier.readableContentWidth(),
                    )
                }
            }

            // Content
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.operationMode == OperationMode.NONE && !uiState.isLoading -> {
                    if (hasGrantedShizukuPermission) {
                        ShizukuConnectingState(onOpenShizuku = onOpenShizuku)
                    } else {
                        ShizukuRequiredWarning(
                            onRequestPermission = onRequestShizukuPermission,
                            onOpenShizuku = onOpenShizuku,
                        )
                    }
                }

                uiState.visibleApps.isEmpty() && !uiState.isLoading -> {
                    PullToRefreshBox(
                        isRefreshing = uiState.isRefreshingLocales,
                        onRefresh = viewModel::refreshApps,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        EmptyState(
                            onClearFilters = viewModel::clearFilters,
                        )
                    }
                }

                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        PullToRefreshBox(
                            isRefreshing = uiState.isRefreshingLocales,
                            onRefresh = viewModel::refreshApps,
                            modifier = Modifier
                                .fillMaxHeight()
                                .readableContentWidth(),
                        ) {
                            AppResultsList(
                                apps = uiState.visibleApps,
                                systemLocaleTag = systemLocaleTag,
                                iconLoader = viewModel.appIconLoader,
                                selectedPackages = uiState.selectedPackages,
                                isSelectionMode = uiState.isSelectionMode,
                                onAppClick = { app ->
                                    if (uiState.isSelectionMode) {
                                        viewModel.toggleSelection(app.packageName)
                                    } else {
                                        navigateToAppScreen(app.packageName)
                                    }
                                },
                                onAppLongClick = { app ->
                                    viewModel.toggleSelection(app.packageName)
                                },
                                state = lazyListState,
                                contentPadding = PaddingValues(
                                    start = horizontalPagePadding,
                                    top = AppSpacing.sm,
                                    end = horizontalPagePadding,
                                    bottom = AppSpacing.lg,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }

    // Batch language sheet
    if (showBatchSheet) {
        BatchLanguageSheet(
            onDismiss = { showBatchSheet = false },
            onLocaleSelected = { tag ->
                viewModel.applyBatchLocale(tag)
                showBatchSheet = false
            },
        )
    }

    // System app confirmation for batch
    if (showSystemWarning) {
        SystemDialogWarn(
            onClickContinue = {
                showSystemWarning = false
                showBatchSheet = true
            },
            onClickCancel = {
                showSystemWarning = false
            },
        )
    }
}

@Composable
private fun EmptyState(
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Outlined.FilterAltOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = AppSpacing.lg),
            )
            Text(
                text = stringResource(R.string.no_results),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.no_results_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(
                    horizontal = AppSpacing.xl,
                    vertical = AppSpacing.sm,
                ),
            )
            TextButton(onClick = onClearFilters) {
                Text(stringResource(R.string.clear_filters))
            }
        }
    }
}
