package ing.fuyaoskyrocket.applocale.ui.systemlanguages

import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.Save
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.model.LocaleOption
import ing.fuyaoskyrocket.applocale.ui.components.AppNavigationDestination
import ing.fuyaoskyrocket.applocale.ui.components.LocaleBadge
import ing.fuyaoskyrocket.applocale.ui.components.ShizukuRequiredWarning
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppComponentDefaults
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppLayout
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppMotion
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemePreferences
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemeStyle
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.LocalBottomDockMetrics
import ing.fuyaoskyrocket.applocale.ui.designsystem.listBottomReserve
import ing.fuyaoskyrocket.applocale.ui.designsystem.listTopReserve
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppCircularProgressIndicator
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppDropdownItem
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppDropdownMenu
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppIcon
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppIconButton
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppPullToRefresh
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppScaffold
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSnackbarHost
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSurface
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSymbol
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSymbolVector
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppText
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppTopAppBar
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.rememberAppSnackbarHostState
import ing.fuyaoskyrocket.applocale.ui.designsystem.readableContentWidth
import ing.fuyaoskyrocket.applocale.ui.screen.TabScrollCoordinator
import ing.fuyaoskyrocket.applocale.ui.screen.TabScrollToTopConsumer
import ing.fuyaoskyrocket.applocale.ui.screen.tabScrollToTopKeyAction

/** Elevation of the floating drag overlay; matches the lifted-row look of the old in-row drag. */
private val DragOverlayShadowElevation = 6.dp

/**
 * Borderless AppIconButton footprint (48dp Material, 40dp miuix), mirrored by the
 * static overlay icons so the floating row wraps text exactly like the real one.
 */
private val overlayIconButtonSize: Dp
    @Composable get() = if (AppThemePreferences.style == AppThemeStyle.MIUIX) 40.dp else 48.dp

/**
 * Global Android language-order editor.
 *
 * The list comes from the system Configuration rather than Locale.getAvailableLocales(), so
 * OEM-provided entries such as zh-CN remain available and stay first in the shared picker.
 */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun SystemLanguagesScreen(
    navigateToHome: () -> Unit,
    navigateToConfigurations: () -> Unit,
    navigateToAbout: () -> Unit,
    hasGrantedShizukuPermission: Boolean,
    onRequestShizukuPermission: () -> Unit,
    onOpenShizuku: () -> Unit,
    showBottomNavigation: Boolean,
    viewModel: SystemLanguagesViewModel = hiltViewModel(),
    tabScrollCoordinator: TabScrollCoordinator? = null,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = rememberAppSnackbarHostState()
    val context = LocalContext.current
    var showLanguagePicker by rememberSaveable { mutableStateOf(false) }

    // The reorder state is hoisted to this screen so the Scaffold's floating FAB —
    // an occluder of the edge auto-scroll sensing zone — can report its measured
    // bounds into the same host coordinate system the list rows use.
    val listState = rememberLazyListState()
    val reorderScope = rememberCoroutineScope()
    val latestLocales by rememberUpdatedState(uiState.locales)
    val density = LocalDensity.current
    val latestDensity by rememberUpdatedState(density)
    // While the host's bottom dock is present its footprint occludes the list
    // bottom; a native bottom slot is already outside the list rect and is not
    // subtracted here.
    val dockMetrics = LocalBottomDockMetrics.current
    val glassOcclusionPx = if (dockMetrics.isPresent) {
        with(density) { dockMetrics.occupiedHeight.toPx() }
    } else {
        0f
    }
    val latestGlassOcclusionPx by rememberUpdatedState(glassOcclusionPx)
    val reorderState = rememberSystemLocaleReorderState(
        scope = reorderScope,
        listState = listState,
        locales = { latestLocales },
        onMove = viewModel::moveLocaleByTag,
        density = { latestDensity },
        bottomOcclusionPx = { latestGlassOcclusionPx },
    )

    // Round-8 035: a tab double tap scrolls to the top but stands down while a
    // reorder drag is settling — the request stays pending until the list is
    // free again. It never refreshes, saves, or drops the draft order.
    if (tabScrollCoordinator != null) {
        TabScrollToTopConsumer(
            coordinator = tabScrollCoordinator,
            destination = AppNavigationDestination.SystemLanguages,
            listState = listState,
            isReady = { reorderState.phase == SystemLocaleReorderPhase.Idle },
        )
    }

    LaunchedEffect(hasGrantedShizukuPermission) {
        if (hasGrantedShizukuPermission) viewModel.load()
    }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            val message = when (event) {
                SystemLanguagesEvent.Saved -> R.string.system_languages_saved
                SystemLanguagesEvent.LoadFailed -> R.string.system_languages_load_failed
                SystemLanguagesEvent.SaveFailed -> R.string.system_languages_save_failed
                SystemLanguagesEvent.CannotRemoveLastLocale -> R.string.system_languages_cannot_remove_last
                SystemLanguagesEvent.LocaleAlreadyAdded -> R.string.system_languages_already_added
                SystemLanguagesEvent.RefreshBlockedByDraft -> R.string.save_languages_before_refresh
            }
            snackbarHostState.showSnackbar(message = context.getString(message))
        }
    }

    AppScaffold(
        topBar = {
            AppTopAppBar(
                title = stringResource(R.string.system_languages),
                actions = {
                    // Add sits on the top-right (round-5 019-B); refreshing moved
                    // to the pull gesture, the a11y action and Ctrl+R.
                    AppIconButton(
                        onClick = { showLanguagePicker = true },
                        enabled = hasGrantedShizukuPermission && !uiState.isLoading &&
                            !uiState.isSaving && !uiState.isRefreshing,
                    ) {
                        AppIcon(
                            imageVector = AppSymbolVector(AppSymbol.Add),
                            contentDescription = stringResource(R.string.add_system_language),
                        )
                    }
                    AppIconButton(
                        onClick = viewModel::save,
                        enabled = hasGrantedShizukuPermission &&
                            uiState.hasUnsavedChanges &&
                            !uiState.isSaving &&
                            !uiState.isRefreshing,
                    ) {
                        if (uiState.isSaving) {
                            AppCircularProgressIndicator(
                                modifier = Modifier.size(AppSpacing.lg),
                                strokeWidth = AppSpacing.xs / 2,
                            )
                        } else {
                            // No miuix save glyph; the project vector is drawn by
                            // the backend icon control (asset exception).
                            AppIcon(
                                imageVector = Icons.Outlined.Save,
                                contentDescription = stringResource(R.string.save_system_languages),
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            // The navigation dock is owned by AppChromeHost.
        },
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = AppUiTheme.palette.background,
    ) { innerPadding ->
        when {
            !hasGrantedShizukuPermission -> {
                ShizukuRequiredWarning(
                    onRequestPermission = onRequestShizukuPermission,
                    onOpenShizuku = onOpenShizuku,
                    modifier = Modifier.padding(innerPadding),
                )
            }
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
            else -> {
                val imeVisible = WindowInsets.isImeVisible
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .consumeWindowInsets(innerPadding)
                        // Ctrl+R (initial KeyDown only): a page-local keyboard
                        // equivalent of the pull gesture, standing down while a
                        // sheet, dialog or the IME owns the window above.
                        .onPreviewKeyEvent { event ->
                            val invokesRefresh = event.type == KeyEventType.KeyDown &&
                                event.nativeKeyEvent.repeatCount == 0 &&
                                (event.isCtrlPressed || event.isMetaPressed) &&
                                event.key == Key.R &&
                                !showLanguagePicker &&
                                !imeVisible
                            if (invokesRefresh) {
                                viewModel.refresh()
                                true
                            } else {
                                false
                            }
                        }
                        .then(
                            if (tabScrollCoordinator != null) {
                                Modifier.tabScrollToTopKeyAction(
                                    coordinator = tabScrollCoordinator,
                                    destination = AppNavigationDestination.SystemLanguages,
                                )
                            } else {
                                Modifier
                            },
                        ),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    AppPullToRefresh(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = viewModel::refresh,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        SystemLanguagesList(
                            locales = uiState.locales,
                            hasUnsavedChanges = uiState.hasUnsavedChanges,
                            dragEnabled = !uiState.isRefreshing,
                            onMove = viewModel::moveLocaleByTag,
                            onRemove = viewModel::removeLocale,
                            onRefresh = viewModel::refresh,
                            listState = listState,
                            reorderState = reorderState,
                            contentPadding = PaddingValues(
                                top = listTopReserve(innerPadding.calculateTopPadding()),
                                bottom = listBottomReserve(innerPadding.calculateBottomPadding()),
                            ),
                            modifier = Modifier.readableContentWidth(),
                        )
                    }
                }
            }
        }
    }

    // Always in composition; the wrapper owns the picker window's exit.
    SystemLanguagePickerSheet(
        visible = showLanguagePicker,
        existingLocales = uiState.locales,
        onDismiss = { showLanguagePicker = false },
        onLocaleSelected = { locale ->
            if (showLanguagePicker) {
                showLanguagePicker = false
                viewModel.addLocale(locale)
            }
        },
    )
}

@Composable
private fun SystemLanguagesList(
    locales: List<LocaleOption>,
    hasUnsavedChanges: Boolean,
    dragEnabled: Boolean,
    onMove: (String, String) -> Unit,
    onRemove: (LocaleOption) -> Unit,
    onRefresh: () -> Unit,
    listState: LazyListState,
    reorderState: SystemLocaleReorderState,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val latestLocales by rememberUpdatedState(locales)
    val density = LocalDensity.current
    // The complete refresh affordance description — gesture AND keyboard path —
    // as one whole sentence for assistive tech (never spliced fragments).
    val refreshActionLabel = stringResource(R.string.system_languages_refresh_hint_keyboard)

    // Stable host Box: the single coordinate origin for pointer events, row and
    // handle rectangles, and the drag overlay. Keeping the gesture here means the
    // source item can be disposed by auto-scroll without cancelling the drag.
    // The horizontal list inset lives on this Box, not in the LazyColumn content
    // padding, so the overlay's x origin lines up with every row's left edge.
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = AppLayout.contentFrameMargin)
            .onGloballyPositioned(reorderState::hostPositioned)
            .semantics {
                customActions = listOf(
                    CustomAccessibilityAction(refreshActionLabel) {
                        onRefresh()
                        true
                    },
                )
            }
            .pointerInput(dragEnabled) {
                if (dragEnabled) {
                    detectSystemLocaleReorder(reorderState)
                }
            },
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                // The measured rect of the list itself is the base of the edge
                // auto-scroll viewport.
                .onGloballyPositioned(reorderState::reportViewport),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            item(key = "system_languages_summary") {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    AppText(
                        text = stringResource(R.string.system_languages_summary),
                        style = AppUiTheme.textStyles.body,
                        color = AppUiTheme.palette.muted,
                    )
                    if (hasUnsavedChanges) {
                        AppText(
                            text = stringResource(R.string.system_languages_unsaved),
                            style = AppUiTheme.textStyles.label,
                            color = AppUiTheme.palette.accent,
                        )
                    }
                    // One short line documenting the refresh affordance; the
                    // keyboard path stays with the assistive action label.
                    AppText(
                        text = stringResource(R.string.system_languages_refresh_hint),
                        style = AppUiTheme.textStyles.metadata,
                        color = AppUiTheme.palette.muted,
                    )
                }
            }
            item(key = "system_languages_header") {
                AppText(
                    text = stringResource(R.string.system_language_order),
                    style = AppUiTheme.textStyles.itemTitle,
                    color = AppUiTheme.palette.accent,
                    modifier = Modifier.padding(top = AppSpacing.sm),
                )
            }
            // Non-touch moves resolve their neighbours from the latest draft so menu
            // commands and drag reorder always agree on the current order.
            val moveUpByTag: (String) -> Unit = { tag ->
                val index = locales.indexOfFirst { it.languageTag == tag }
                if (index > 0) onMove(tag, locales[index - 1].languageTag)
            }
            val moveDownByTag: (String) -> Unit = { tag ->
                val index = locales.indexOfFirst { it.languageTag == tag }
                if (index >= 0 && index < locales.lastIndex) {
                    onMove(tag, locales[index + 1].languageTag)
                }
            }
            itemsIndexed(
                items = locales,
                key = { _, locale -> systemLocaleItemKey(locale.languageTag) },
                contentType = { _, _ -> "system_locale" },
            ) { index, locale ->
                val tag = locale.languageTag
                val isDragSource = reorderState.draggingTag == tag &&
                    reorderState.phase != SystemLocaleReorderPhase.Idle
                DisposableEffect(tag) {
                    onDispose { reorderState.removeRow(tag) }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { reorderState.reportRow(tag, it) }
                        .then(
                            if (isDragSource) {
                                // The source slot freezes at its measured height; only
                                // the overlay translates, never the placeholder row.
                                Modifier
                            } else {
                                Modifier.animateItem(
                                    fadeInSpec = null,
                                    fadeOutSpec = null,
                                    placementSpec = spring(
                                        dampingRatio = AppMotion.ReturnDampingRatio,
                                        stiffness = AppMotion.ReturnStiffness,
                                    ),
                                )
                            },
                        ),
                ) {
                    if (isDragSource) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(with(density) { reorderState.overlayHeight.toDp() }),
                        )
                    } else {
                        SystemLocaleRow(
                            locale = locale,
                            canMoveUp = index > 0,
                            canMoveDown = index < locales.lastIndex,
                            canRemove = locales.size > 1,
                            reorderState = reorderState,
                            onMoveUp = { moveUpByTag(tag) },
                            onMoveDown = { moveDownByTag(tag) },
                            onRemove = { onRemove(locale) },
                        )
                    }
                }
            }
        }

        if (reorderState.phase != SystemLocaleReorderPhase.Idle) {
            SystemLocaleDragOverlay(
                state = reorderState,
                locales = latestLocales,
            )
        }
    }
}

@Composable
private fun SystemLocaleRow(
    locale: LocaleOption,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    canRemove: Boolean,
    reorderState: SystemLocaleReorderState,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
) {
    val moveUpLabel = stringResource(R.string.move_language_up)
    val moveDownLabel = stringResource(R.string.move_language_down)
    var showMoveMenu by remember { mutableStateOf(false) }
    // Long-press drag and the short-press menu are mutually exclusive; starting any
    // drag closes row menus.
    val dragActive = reorderState.phase != SystemLocaleReorderPhase.Idle
    LaunchedEffect(dragActive) {
        if (dragActive) showMoveMenu = false
    }

    // Normal list rows stay transparent like every other long list (020); only
    // the floating drag overlay is allowed its temporarily elevated container.
    AppSurface(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                customActions = buildList {
                    if (canMoveUp) add(CustomAccessibilityAction(moveUpLabel) { onMoveUp(); true })
                    if (canMoveDown) add(CustomAccessibilityAction(moveDownLabel) { onMoveDown(); true })
                }
            },
        shape = RoundedCornerShape(AppComponentDefaults.rowCornerRadius),
        color = androidx.compose.ui.graphics.Color.Transparent,
    ) {
        SystemLocaleRowContent(
            locale = locale,
            trailing = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        // Short press opens the move menu; the long-press drag is
                        // detected on the stable host Box through this 56dp target.
                        AppIconButton(
                            onClick = { showMoveMenu = true },
                            modifier = Modifier
                                .size(AppLayout.standardListItemMinHeight)
                                .onGloballyPositioned {
                                    reorderState.reportHandle(locale.languageTag, it)
                                },
                        ) {
                            AppIcon(
                                imageVector = Icons.Outlined.DragHandle,
                                contentDescription = stringResource(R.string.reorder_system_language),
                            )
                        }
                        DisposableEffect(locale.languageTag) {
                            onDispose { reorderState.removeHandle(locale.languageTag) }
                        }
                        AppDropdownMenu(
                            expanded = showMoveMenu,
                            onDismiss = { showMoveMenu = false },
                            items = buildList {
                                if (canMoveUp) {
                                    add(AppDropdownItem(text = moveUpLabel) {
                                        onMoveUp()
                                        showMoveMenu = false
                                    })
                                }
                                if (canMoveDown) {
                                    add(AppDropdownItem(text = moveDownLabel) {
                                        onMoveDown()
                                        showMoveMenu = false
                                    })
                                }
                            },
                        )
                    }
                    AppIconButton(
                        onClick = onRemove,
                        enabled = canRemove,
                    ) {
                        AppIcon(
                            imageVector = AppSymbolVector(AppSymbol.Delete),
                            contentDescription = stringResource(R.string.remove_system_language),
                        )
                    }
                }
            },
        )
    }
}

/** Layout shared by the interactive row and the drag overlay so both measure alike. */
@Composable
private fun SystemLocaleRowContent(
    locale: LocaleOption,
    trailing: @Composable () -> Unit,
) {
    Row(
        // No horizontal padding here: the host Box already applies the shared
        // 16dp frame and the drag overlay measures through this same content.
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = AppLayout.appListItemMinHeight),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        LocaleBadge(languageTag = locale.languageTag, preferRegion = true)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            AppText(
                text = locale.localizedDisplayName,
                style = AppUiTheme.textStyles.itemTitle,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            AppText(
                text = "${locale.languageTag} · ${locale.displayName}",
                style = AppUiTheme.textStyles.metadata,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = AppUiTheme.palette.muted,
            )
        }
        trailing()
    }
}

/**
 * The single floating row shown while Dragging or Settling. It draws at the size
 * captured at grab time, is purely visual (no buttons, no focusable or duplicate
 * accessibility nodes), and never registers itself as a reorder target.
 */
@Composable
private fun SystemLocaleDragOverlay(
    state: SystemLocaleReorderState,
    locales: List<LocaleOption>,
) {
    val tag = state.draggingTag ?: return
    // Live draft content while the tag exists; the grab snapshot only covers the
    // release tail if the entry disappears mid-settle.
    val locale = locales.firstOrNull { it.languageTag == tag } ?: state.overlaySnapshot ?: return
    val density = LocalDensity.current
    val width = with(density) { state.overlayWidth.toDp() }
    val height = with(density) { state.overlayHeight.toDp() }
    val rowShape = RoundedCornerShape(AppComponentDefaults.rowCornerRadius)
    Box(
        modifier = Modifier
            .zIndex(1f)
            .graphicsLayer {
                translationY = state.overlayTop
                shadowElevation = DragOverlayShadowElevation.toPx()
                shape = rowShape
            }
            .size(width, height),
    ) {
        AppSurface(
            modifier = Modifier
                .fillMaxSize()
                .clearAndSetSemantics { },
            shape = rowShape,
            color = AppUiTheme.palette.secondarySurface,
        ) {
            SystemLocaleRowContent(
                locale = locale,
                trailing = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Static icon footprints mirror the interactive trailing row so
                        // the overlay wraps text exactly like the row it replaced.
                        Box(
                            modifier = Modifier.size(AppLayout.standardListItemMinHeight),
                            contentAlignment = Alignment.Center,
                        ) {
                            AppIcon(
                                imageVector = Icons.Outlined.DragHandle,
                                contentDescription = null,
                            )
                        }
                        Box(
                            modifier = Modifier.size(overlayIconButtonSize),
                            contentAlignment = Alignment.Center,
                        ) {
                            AppIcon(
                                imageVector = AppSymbolVector(AppSymbol.Delete),
                                contentDescription = null,
                            )
                        }
                    }
                },
            )
        }
    }
}

private fun systemLocaleItemKey(languageTag: String): String = "system_locale_$languageTag"
