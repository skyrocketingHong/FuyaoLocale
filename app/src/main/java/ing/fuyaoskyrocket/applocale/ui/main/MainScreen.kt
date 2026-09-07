package ing.fuyaoskyrocket.applocale.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterAltOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.model.BatchApplyState
import ing.fuyaoskyrocket.applocale.model.OperationMode
import ing.fuyaoskyrocket.applocale.ui.components.AppFilterBar
import ing.fuyaoskyrocket.applocale.ui.components.AppNavigationDestination
import ing.fuyaoskyrocket.applocale.ui.components.AppResultsList
import ing.fuyaoskyrocket.applocale.ui.components.BatchBottomAppBar
import ing.fuyaoskyrocket.applocale.ui.components.SelectionTopAppBar
import ing.fuyaoskyrocket.applocale.ui.components.ShizukuConnectingState
import ing.fuyaoskyrocket.applocale.ui.components.ShizukuRequiredWarning
import ing.fuyaoskyrocket.applocale.ui.components.SystemDialogWarn
import ing.fuyaoskyrocket.applocale.ui.components.rememberSystemLocaleTag
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppLayout
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.listBottomReserve
import ing.fuyaoskyrocket.applocale.ui.designsystem.listTopReserve
import ing.fuyaoskyrocket.applocale.ui.designsystem.LocalGlassNavigationBarVisibility
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppCircularProgressIndicator
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppHomeTopAppBar
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppIcon
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppPullToRefresh
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppScaffold
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSnackbarHost
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppText
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppTextButton
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.rememberAppSnackbarHostState
import ing.fuyaoskyrocket.applocale.ui.designsystem.readableContentWidth
import ing.fuyaoskyrocket.applocale.ui.designsystem.shouldShowBatchBar
import ing.fuyaoskyrocket.applocale.ui.languagepicker.BatchLanguageSheet
import ing.fuyaoskyrocket.applocale.ui.screen.TabScrollCoordinator
import ing.fuyaoskyrocket.applocale.ui.screen.TabScrollToTopConsumer
import ing.fuyaoskyrocket.applocale.ui.screen.tabScrollToTopKeyAction

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    navigateToAppScreen: (String) -> Unit,
    navigateToSystemLanguages: () -> Unit,
    navigateToConfigurations: () -> Unit,
    navigateToAbout: () -> Unit,
    hasGrantedShizukuPermission: Boolean,
    onRequestShizukuPermission: () -> Unit,
    onOpenShizuku: () -> Unit,
    showBottomNavigation: Boolean = true,
    tabScrollCoordinator: TabScrollCoordinator? = null,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = rememberAppSnackbarHostState()
    val lazyListState = rememberLazyListState()
    val systemLocaleTag = rememberSystemLocaleTag()

    // Round-8 035: a tab double tap only scrolls the current filtered list back
    // to its top — never a refresh, never touching query/filters/selection.
    if (tabScrollCoordinator != null) {
        TabScrollToTopConsumer(
            coordinator = tabScrollCoordinator,
            destination = AppNavigationDestination.Home,
            listState = lazyListState,
        )
    }

    var showBatchSheet by remember { mutableStateOf(false) }
    var showSystemWarning by remember { mutableStateOf(false) }
    // Hands the batch sheet over only after the warning window has closed, so the
    // two windows never overlap.
    var openBatchAfterWarning by remember { mutableStateOf(false) }

    // Exiting selection or closing the search is in-page state, not navigation: the
    // back event is consumed without deforming the page. While a sheet/dialog window
    // or the IME owns the back gesture, this handler stands down so the window goes
    // first (IME dismiss, then the in-page state on the next back).
    val imeVisible = WindowInsets.isImeVisible
    BackHandler(
        enabled = !imeVisible && !showBatchSheet && !showSystemWarning &&
            (uiState.isSelectionMode || uiState.isSearchActive),
    ) {
        when {
            uiState.isSelectionMode -> viewModel.clearSelection()
            else -> viewModel.closeSearch()
        }
    }

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
    // Selection mode hands the bottom edge to the batch bar; the floating glass tab
    // bar must get out of the way while it is shown.
    val glassNavigationBarVisibility = LocalGlassNavigationBarVisibility.current
    DisposableEffect(glassNavigationBarVisibility, uiState.isSelectionMode) {
        glassNavigationBarVisibility?.isSuppressed = uiState.isSelectionMode
        onDispose { glassNavigationBarVisibility?.isSuppressed = false }
    }
    // Measured batch-slot height: the snackbar host only needs to dodge the part
    // of the glass footprint that this slot has not already reserved.
    val density = LocalDensity.current
    var batchSlotHeight by remember { mutableStateOf(0.dp) }
    val horizontalPagePadding = AppLayout.contentFrameMargin

    // The single presentation source for the batch bar (021): the target is the
    // real selection state gated by the dock hand-off, the transition is created
    // once here — never inside the bottomBar branch — and the slot stays
    // composed until the exit animation has actually finished.
    val batchPresentation = remember { MutableTransitionState(false) }
    val batchTarget = uiState.isSelectionMode && shouldShowBatchBar()
    LaunchedEffect(batchTarget) {
        batchPresentation.targetState = batchTarget
    }
    val batchTransition = updateTransition(
        transitionState = batchPresentation,
        label = "BatchPresentation",
    )
    val batchPresentationProgress by batchTransition.animateFloat(
        targetValueByState = { presented -> if (presented) 1f else 0f },
        transitionSpec = { tween(durationMillis = 180, easing = FastOutSlowInEasing) },
        label = "batchPresentationProgress",
    )
    val keepBatchSlot = uiState.isSelectionMode || batchPresentation.currentState ||
        batchPresentation.targetState || !batchPresentation.isIdle

    AppScaffold(
        topBar = {
            if (uiState.isSelectionMode) {
                SelectionTopAppBar(
                    selectedCount = uiState.selectedPackages.size,
                    onClose = { viewModel.clearSelection() },
                    onSelectAll = { viewModel.selectAllVisible() },
                    onClear = { viewModel.clearSelection() },
                )
            } else {
                AppHomeTopAppBar(
                    title = stringResource(R.string.app_name),
                    searchExpanded = uiState.isSearchExpanded,
                    query = uiState.query,
                    showSystemApps = uiState.showSystemApps,
                    onOpenSearch = viewModel::openSearch,
                    onCloseSearch = viewModel::closeSearch,
                    onQueryChange = viewModel::onQueryChange,
                    onRefresh = viewModel::refreshApps,
                    onToggleSystemApps = viewModel::toggleShowSystemApps,
                )
            }
        },
        bottomBar = {
            // The navigation dock is owned by AppChromeHost; this slot only
            // hosts the selection-mode batch bar, kept while its exit runs.
            if (keepBatchSlot) {
                Box(
                    modifier = Modifier.onSizeChanged { size ->
                        batchSlotHeight = with(density) { size.height.toDp() }
                    },
                ) {
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
                        // The lambda defers the animation read to the bar's
                        // graphics layer; interactivity is the real business
                        // gate (selection + dock vacated), never the alpha.
                        presentationProgress = { batchPresentationProgress },
                        interactive = uiState.isSelectionMode && shouldShowBatchBar(),
                    )
                }
            }
        },
        snackbarHost = {
            AppSnackbarHost(
                snackbarHostState,
                hostSlotBottom = if (keepBatchSlot) batchSlotHeight else 0.dp,
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = AppUiTheme.palette.background,
    ) { innerPadding ->
        // The scaffold padding is consumed exactly once (round-5 018-A): the
        // scrollable list turns it into contentPadding so it starts below the
        // real bar but scrolls behind it; fixed states use plain padding. No
        // estimated top-bar heights anywhere.
        val listBottomPadding = listBottomReserve(innerPadding.calculateBottomPadding())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(innerPadding)
                .then(
                    if (tabScrollCoordinator != null) {
                        Modifier.tabScrollToTopKeyAction(
                            coordinator = tabScrollCoordinator,
                            destination = AppNavigationDestination.Home,
                        )
                    } else {
                        Modifier
                    },
                ),
        ) {
            // Content
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center,
                    ) {
                        AppCircularProgressIndicator()
                    }
                }

                uiState.operationMode == OperationMode.NONE && !uiState.isLoading -> {
                    Box(modifier = Modifier.padding(innerPadding)) {
                        if (hasGrantedShizukuPermission) {
                            ShizukuConnectingState(onOpenShizuku = onOpenShizuku)
                        } else {
                            ShizukuRequiredWarning(
                                onRequestPermission = onRequestShizukuPermission,
                                onOpenShizuku = onOpenShizuku,
                            )
                        }
                    }
                }

                uiState.visibleApps.isEmpty() && !uiState.isLoading -> {
                    AppPullToRefresh(
                        isRefreshing = uiState.isRefreshingLocales,
                        onRefresh = viewModel::refreshApps,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        EmptyState(
                            onClearFilters = viewModel::clearFilters,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                        )
                    }
                }

                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        AppPullToRefresh(
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
                                    // Entering selection drops the search focus and IME
                                    // in the user event itself; the query and filtered
                                    // results stay untouched. Toggling items while
                                    // already selecting does not re-run the hide.
                                    if (!uiState.isSelectionMode) {
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                    }
                                    viewModel.toggleSelection(app.packageName)
                                },
                                query = uiState.query,
                                state = lazyListState,
                                // The chips row is the list's first item so the whole
                                // directory scrolls under the top chrome (more blur).
                                header = {
                                    AppFilterBar(
                                        modifiedOnly = uiState.modifiedOnly,
                                        modifiedCount = uiState.modifiedCount,
                                        sortOption = uiState.sortOption,
                                        sortAscending = uiState.sortAscending,
                                        onToggleModifiedOnly = { viewModel.toggleModifiedOnly() },
                                        onCycleSortOption = viewModel::cycleSortOption,
                                        horizontalPadding = horizontalPagePadding,
                                    )
                                },
                                contentPadding = PaddingValues(
                                    top = listTopReserve(innerPadding.calculateTopPadding()),
                                    bottom = listBottomPadding,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }

    // Batch language sheet (always in composition; the wrapper owns its exit).
    BatchLanguageSheet(
        visible = showBatchSheet,
        onDismiss = { showBatchSheet = false },
        onLocaleSelected = { tag ->
            if (showBatchSheet) {
                showBatchSheet = false
                viewModel.applyBatchLocale(tag)
            }
        },
    )

    // System app confirmation for batch (always in composition; the wrapper owns
    // its exit). Confirming only arms the handoff; the sheet opens from the
    // finished callback once the warning window has actually closed.
    SystemDialogWarn(
        visible = showSystemWarning,
        onClickContinue = {
            if (showSystemWarning) {
                openBatchAfterWarning = true
                showSystemWarning = false
            }
        },
        onClickCancel = {
            if (showSystemWarning) {
                openBatchAfterWarning = false
                showSystemWarning = false
            }
        },
        onDismissFinished = {
            if (openBatchAfterWarning) {
                openBatchAfterWarning = false
                showBatchSheet = true
            }
        },
    )
}

@Composable
private fun EmptyState(
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = AppUiTheme.palette
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // No miuix glyph for the filter-off metaphor; the project vector is
            // drawn by the backend icon control (asset exception, see 012 record).
            AppIcon(
                imageVector = Icons.Outlined.FilterAltOff,
                contentDescription = null,
                tint = palette.muted,
                modifier = Modifier.padding(bottom = AppSpacing.lg),
            )
            AppText(
                text = stringResource(R.string.no_results),
                style = AppUiTheme.textStyles.itemTitle,
                textAlign = TextAlign.Center,
            )
            AppText(
                text = stringResource(R.string.no_results_description),
                style = AppUiTheme.textStyles.body,
                color = palette.muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(
                    horizontal = AppSpacing.xl,
                    vertical = AppSpacing.sm,
                ),
            )
            AppTextButton(
                text = stringResource(R.string.clear_filters),
                onClick = onClearFilters,
            )
        }
    }
}
