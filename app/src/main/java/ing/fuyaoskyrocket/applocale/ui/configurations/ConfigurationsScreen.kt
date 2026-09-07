package ing.fuyaoskyrocket.applocale.ui.configurations

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.data.system.AppIconLoader
import ing.fuyaoskyrocket.applocale.model.ConfigurationEditCommand
import ing.fuyaoskyrocket.applocale.model.SavedLocaleConfiguration
import ing.fuyaoskyrocket.applocale.ui.components.AppNavigationDestination
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppLayout
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppComponentDefaults
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.listBottomReserve
import ing.fuyaoskyrocket.applocale.ui.designsystem.listTopReserve
import ing.fuyaoskyrocket.applocale.ui.screen.TabScrollCoordinator
import ing.fuyaoskyrocket.applocale.ui.screen.TabScrollToTopConsumer
import ing.fuyaoskyrocket.applocale.ui.screen.tabScrollToTopKeyAction
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppCircularProgressIndicator
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppIcon
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppIconButton
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppScaffold
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSnackbarHost
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppTextButton
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSymbol
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSymbolVector
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppText
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppTopAppBar
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.rememberAppSnackbarHostState
import ing.fuyaoskyrocket.applocale.ui.designsystem.readableContentWidth
import ing.fuyaoskyrocket.applocale.ui.designsystem.wideContentWidth
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Saved locale presets. Compact layouts open a dedicated difference screen;
 * expanded layouts keep the preset list and the difference detail side by side.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConfigurationsScreen(
    navigateToHome: () -> Unit,
    navigateToSystemLanguages: () -> Unit,
    navigateToAbout: () -> Unit,
    onOpenConfiguration: (String) -> Unit,
    showBottomNavigation: Boolean,
    useWideLayout: Boolean,
    viewModel: ConfigurationsViewModel = hiltViewModel(),
    tabScrollCoordinator: TabScrollCoordinator? = null,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = rememberAppSnackbarHostState()
    val importScope = rememberCoroutineScope()
    // Round-8 038: the wide pane's single-app chooser and the package anchor
    // kept across an auto-saved derived configuration.
    var editingPackage by remember { mutableStateOf<String?>(null) }
    var detailAnchorPackage by remember { mutableStateOf<String?>(null) }
    // One list state for whichever configuration list is currently shown
    // (round-8 035): the compact route and the wide master pane are never both
    // mounted, so only the visible layout ever consumes scroll requests.
    val configurationsListState = rememberLazyListState()
    if (tabScrollCoordinator != null) {
        TabScrollToTopConsumer(
            coordinator = tabScrollCoordinator,
            destination = AppNavigationDestination.Configurations,
            listState = configurationsListState,
        )
    }
    var pendingExportId by rememberSaveable { mutableStateOf<String?>(null) }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let {
            importScope.launch {
                val serialized = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)
                        ?.bufferedReader()
                        ?.use { reader -> reader.readText() }
                }
                if (serialized == null) {
                    viewModel.reportImportReadFailure()
                } else {
                    viewModel.importConfiguration(serialized)
                }
            }
        }
    }
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        val configurationId = pendingExportId
        pendingExportId = null
        if (uri != null && configurationId != null) {
            val serialized = viewModel.serializeConfiguration(configurationId)
            importScope.launch {
                val success = serialized != null && withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openOutputStream(uri, "wt")
                            ?.bufferedWriter()
                            ?.use { writer -> writer.write(serialized) } != null
                    }.getOrDefault(false)
                }
                viewModel.reportExportResult(success)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            snackbarHostState.showSnackbar(context.configurationEventMessage(event))
            // The ViewModel has already switched the selection to the derived
            // configuration; the anchor keeps the edited row's scroll position.
            if (event is ConfigurationsEvent.EditCompleted && event.newConfigurationId != null) {
                detailAnchorPackage = event.packageName
            }
        }
    }

    val openImportPicker = {
        importLauncher.launch(arrayOf("application/json", "text/*"))
    }
    val exportConfiguration: (SavedLocaleConfiguration) -> Unit = { configuration ->
        pendingExportId = configuration.id
        exportLauncher.launch(configuration.exportFileName())
    }

    // Wide-screen selection is in-page state: back clears it without page motion.
    // The IME goes first; compact layouts navigate via the NavHost route instead.
    val imeVisible = WindowInsets.isImeVisible
    BackHandler(
        enabled = useWideLayout && uiState.selectedConfigurationId != null && !imeVisible,
        onBack = viewModel::clearSelection,
    )

    AppScaffold(
        topBar = {
            AppTopAppBar(
                title = stringResource(R.string.configurations),
                actions = {
                    AppIconButton(
                        onClick = viewModel::saveCurrentChanges,
                        enabled = !uiState.isSaving && !uiState.isImporting,
                    ) {
                        if (uiState.isSaving) {
                            AppCircularProgressIndicator(
                                modifier = Modifier.size(AppSpacing.lg),
                                strokeWidth = AppSpacing.xs / 2,
                            )
                        } else {
                            AppIcon(
                                imageVector = Icons.Outlined.BookmarkAdd,
                                contentDescription = stringResource(R.string.save_current_changes),
                            )
                        }
                    }
                    AppIconButton(
                        onClick = openImportPicker,
                        enabled = !uiState.isSaving && !uiState.isImporting,
                    ) {
                        if (uiState.isImporting) {
                            AppCircularProgressIndicator(
                                modifier = Modifier.size(AppSpacing.lg),
                                strokeWidth = AppSpacing.xs / 2,
                            )
                        } else {
                            AppIcon(
                                imageVector = Icons.Outlined.FileOpen,
                                contentDescription = stringResource(R.string.import_configuration),
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (tabScrollCoordinator != null) {
                        Modifier.tabScrollToTopKeyAction(
                            coordinator = tabScrollCoordinator,
                            destination = AppNavigationDestination.Configurations,
                        )
                    } else {
                        Modifier
                    },
                ),
        ) {
            if (useWideLayout) {
                WideConfigurationsContent(
                    uiState = uiState,
                    listState = configurationsListState,
                    iconLoader = viewModel.appIconLoader,
                    onSelect = viewModel::selectConfiguration,
                    onApply = viewModel::apply,
                    onRefresh = viewModel::refreshSelectedComparison,
                    onDelete = viewModel::delete,
                    onExport = exportConfiguration,
                    onRetryPendingSave = viewModel::retryPendingSave,
                    onRecheckPendingEdit = viewModel::recheckPendingEdit,
                    onDiscardPendingEdit = viewModel::discardPendingEdit,
                    onEditApp = if (uiState.isEditBusy) null else ({ packageName ->
                        editingPackage = packageName
                    }),
                    anchorPackage = detailAnchorPackage,
                    onAnchorConsumed = { detailAnchorPackage = null },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
            } else {
                CompactConfigurationsContent(
                    configurations = uiState.configurations,
                    pendingEdit = uiState.pendingEdit,
                    pendingEditOutcomeUnknown = uiState.pendingEditOutcomeUnknown,
                    onRetryPendingSave = viewModel::retryPendingSave,
                    onRecheckPendingEdit = viewModel::recheckPendingEdit,
                    onDiscardPendingEdit = viewModel::discardPendingEdit,
                    deleteEnabled = !uiState.isEditBusy,
                    listState = configurationsListState,
                    onOpen = onOpenConfiguration,
                    onDelete = viewModel::delete,
                    onExport = exportConfiguration,
                    modifier = Modifier
                        .fillMaxSize()
                        .consumeWindowInsets(innerPadding),
                    contentPadding = PaddingValues(
                        start = AppLayout.contentFrameMargin,
                        end = AppLayout.contentFrameMargin,
                        top = listTopReserve(innerPadding.calculateTopPadding()),
                        bottom = listBottomReserve(innerPadding.calculateBottomPadding()),
                    ),
                )
            }
        }
    }

    // The wide pane's single-app chooser (round-8 038), same contract as the
    // phone detail's sheet.
    ConfigurationLanguageSheet(
        visible = editingPackage != null,
        packageName = editingPackage,
        selectedLanguageTag = uiState.detailRows
            ?.firstOrNull { it.packageName == editingPackage }
            ?.let { row -> row.savedLocaleTag ?: row.currentLocaleTag },
        onDismiss = { editingPackage = null },
        onLocaleSelected = { tag ->
            val packageName = editingPackage
            val sourceId = uiState.selectedConfigurationId
            if (packageName != null && sourceId != null) {
                editingPackage = null
                viewModel.submitEdit(
                    sourceConfigurationId = sourceId,
                    packageName = packageName,
                    targetLocaleTag = tag,
                )
            }
        },
    )
}

@Composable
private fun CompactConfigurationsContent(
    configurations: List<SavedLocaleConfiguration>,
    pendingEdit: ConfigurationEditCommand?,
    pendingEditOutcomeUnknown: Boolean,
    onRetryPendingSave: () -> Unit,
    onRecheckPendingEdit: () -> Unit,
    onDiscardPendingEdit: () -> Unit,
    deleteEnabled: Boolean,
    listState: LazyListState,
    onOpen: (String) -> Unit,
    onDelete: (String) -> Unit,
    onExport: (SavedLocaleConfiguration) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    // Round-8 037: the host list keeps NO horizontal inset — rows frame
    // themselves with the shared 4dp outer margin (16dp foreground, selected
    // background 4dp from the frame edge), and the empty state uses the 16dp
    // content frame.
    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (pendingEdit != null) {
            item(key = "pending_edit_banner") {
                PendingEditBanner(
                    outcomeUnknown = pendingEditOutcomeUnknown,
                    onRetrySave = onRetryPendingSave,
                    onRecheck = onRecheckPendingEdit,
                    onDiscard = onDiscardPendingEdit,
                    modifier = Modifier.readableContentWidth()
                        .padding(horizontal = AppLayout.contentFrameMargin),
                )
            }
        }
        configurationListItems(
            configurations = configurations,
            selectedConfigurationId = null,
            onOpen = onOpen,
            onDelete = onDelete,
            onExport = onExport,
            deleteEnabled = deleteEnabled,
            rowModifier = Modifier.readableContentWidth()
                .padding(horizontal = AppLayout.localeRowOuterMargin),
            emptyStateModifier = Modifier.readableContentWidth()
                .padding(horizontal = AppLayout.contentFrameMargin),
        )
    }
}

@Composable
private fun WideConfigurationsContent(
    uiState: ConfigurationsUiState,
    listState: LazyListState,
    iconLoader: AppIconLoader,
    onSelect: (String) -> Unit,
    onApply: (String) -> Unit,
    onRefresh: () -> Unit,
    onDelete: (String) -> Unit,
    onExport: (SavedLocaleConfiguration) -> Unit,
    onRetryPendingSave: () -> Unit,
    onRecheckPendingEdit: () -> Unit,
    onDiscardPendingEdit: () -> Unit,
    onEditApp: ((packageName: String) -> Unit)?,
    anchorPackage: String?,
    onAnchorConsumed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedConfiguration = uiState.configurations.firstOrNull {
        it.id == uiState.selectedConfigurationId
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter,
    ) {
        // Round-8 037: no rounded card shells around the panes — a plain row,
        // one quiet vertical divider, and each pane framing its own content
        // (16dp foreground / 4dp selected-background edge). The row itself
        // contributes no horizontal padding.
        Row(
            modifier = Modifier
                .wideContentWidth()
                .fillMaxHeight()
                .padding(vertical = AppSpacing.screenExpanded),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.paneGap),
        ) {
            Column(
                modifier = Modifier
                    .width(AppLayout.listPaneWidth)
                    .fillMaxHeight(),
            ) {
                if (uiState.pendingEdit != null) {
                    PendingEditBanner(
                        outcomeUnknown = uiState.pendingEditOutcomeUnknown,
                        onRetrySave = onRetryPendingSave,
                        onRecheck = onRecheckPendingEdit,
                        onDiscard = onDiscardPendingEdit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = AppLayout.contentFrameMargin,
                                vertical = AppSpacing.sm,
                            ),
                    )
                }
                ConfigurationList(
                    configurations = uiState.configurations,
                    listState = listState,
                    selectedConfigurationId = uiState.selectedConfigurationId,
                    onOpen = onSelect,
                    onDelete = onDelete,
                    onExport = onExport,
                    deleteEnabled = !uiState.isEditBusy,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(AppUiTheme.palette.divider),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                SavedConfigurationDetailContent(
                    configuration = selectedConfiguration,
                    detailRows = uiState.detailRows,
                    isComparing = uiState.isComparing,
                    isApplying = uiState.applyingConfigurationId == selectedConfiguration?.id,
                    iconLoader = iconLoader,
                    onApply = { selectedConfiguration?.let { onApply(it.id) } },
                    onRefresh = onRefresh,
                    onEditApp = onEditApp,
                    editingPackageName = uiState.editingPackageName,
                    editBusy = uiState.isEditBusy,
                    showRefreshAction = true,
                    anchorPackage = anchorPackage,
                    onAnchorConsumed = onAnchorConsumed,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

private fun LazyListScope.configurationListItems(
    configurations: List<SavedLocaleConfiguration>,
    selectedConfigurationId: String?,
    onOpen: (String) -> Unit,
    onDelete: (String) -> Unit,
    onExport: (SavedLocaleConfiguration) -> Unit,
    deleteEnabled: Boolean,
    rowModifier: Modifier = Modifier,
    emptyStateModifier: Modifier = Modifier,
) {
    if (configurations.isEmpty()) {
        // The empty state frames itself on the 16dp content edge, never on the
        // 4dp row margin.
        item(key = "empty") {
            EmptyConfigurationsState(modifier = emptyStateModifier)
        }
    } else {
        items(configurations, key = { it.id }) { configuration ->
            ConfigurationListItem(
                configuration = configuration,
                isSelected = selectedConfigurationId == configuration.id,
                onOpen = { onOpen(configuration.id) },
                onDelete = { onDelete(configuration.id) },
                onExport = { onExport(configuration) },
                deleteEnabled = deleteEnabled,
                modifier = rowModifier,
            )
        }
    }
}

@Composable
private fun ConfigurationList(
    configurations: List<SavedLocaleConfiguration>,
    listState: LazyListState,
    selectedConfigurationId: String?,
    onOpen: (String) -> Unit,
    onDelete: (String) -> Unit,
    onExport: (SavedLocaleConfiguration) -> Unit,
    deleteEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    // Round-8 037: the 320dp pane keeps the rows full-width with their own 4dp
    // outer margin — no 840dp box inside a constrained pane, no host inset.
    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(vertical = AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        configurationListItems(
            configurations = configurations,
            selectedConfigurationId = selectedConfigurationId,
            onOpen = onOpen,
            onDelete = onDelete,
            onExport = onExport,
            deleteEnabled = deleteEnabled,
            rowModifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppLayout.localeRowOuterMargin),
            emptyStateModifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppLayout.contentFrameMargin),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ConfigurationListItem(
    configuration: SavedLocaleConfiguration,
    isSelected: Boolean,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit,
    deleteEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val formattedDate = remember(configuration.createdAt) {
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
            .format(Date(configuration.createdAt))
    }
    val palette = AppUiTheme.palette
    // Round-8 037: same anatomy as the locale rows — normal rows transparent on
    // the page background, only a real selection paints its container (4dp from
    // the frame edge via the caller's outer margin); no card shell or shadow.
    val contentColor = if (isSelected) palette.onSelected else palette.surfaceContent
    val containerColor = if (isSelected) palette.selected else Color.Transparent

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = AppLayout.localeChoiceRowMinHeight)
            .clip(RoundedCornerShape(AppComponentDefaults.rowCornerRadius))
            .background(containerColor)
            .combinedClickable(
                onClick = onOpen,
                onLongClick = onExport,
            )
            .padding(
                start = AppSpacing.md,
                top = AppSpacing.md,
                end = 0.dp,
                bottom = AppSpacing.md,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        // No miuix bookmark glyph; the project vector is drawn by the
        // backend icon control (asset exception). Explicit 24dp glyph.
        AppIcon(
            imageVector = Icons.Outlined.Bookmarks,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(24.dp),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            AppText(
                text = stringResource(R.string.configuration_saved_at, formattedDate),
                style = AppComponentDefaults.titleStyle(),
                color = contentColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            AppText(
                text = stringResource(
                    R.string.configuration_app_count,
                    configuration.appCount,
                ),
                style = AppComponentDefaults.metadataStyle(),
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        // 48dp touch slot with the 24dp glyph centered: the glyph's right edge
        // lands 16dp from the readable frame, matching the leading foreground.
        AppIconButton(
            onClick = onDelete,
            enabled = deleteEnabled,
            modifier = Modifier.size(AppLayout.appListSelectionSlotWidth),
        ) {
            AppIcon(
                imageVector = AppSymbolVector(AppSymbol.Delete),
                contentDescription = stringResource(R.string.delete_configuration),
                tint = contentColor,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

private fun SavedLocaleConfiguration.exportFileName(): String {
    val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US)
        .format(Date(createdAt))
    return "FuyaoLocale-$timestamp.json"
}

@Composable
private fun EmptyConfigurationsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(vertical = AppSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        // Bookmark motif keeps its project vector (asset exception).
        AppIcon(
            imageVector = Icons.Outlined.Bookmarks,
            contentDescription = null,
            tint = AppUiTheme.palette.muted,
            modifier = Modifier.size(AppSpacing.xl),
        )
        AppText(
            text = stringResource(R.string.no_saved_configurations),
            style = AppUiTheme.textStyles.itemTitle,
        )
        AppText(
            text = stringResource(R.string.no_saved_configurations_description),
            style = AppUiTheme.textStyles.body,
            color = AppUiTheme.palette.muted,
        )
    }
}

/**
 * The unresolved-edit banner (round-8 038): a save that can be retried
 * WITHOUT another Binder call, or an unclear outcome that can only be
 * rechecked or explicitly discarded. Both keep new edits locked until
 * resolved; nothing here rolls the app language back.
 */
@Composable
private fun PendingEditBanner(
    outcomeUnknown: Boolean,
    onRetrySave: () -> Unit,
    onRecheck: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        AppText(
            text = stringResource(
                if (outcomeUnknown) {
                    R.string.configuration_edit_outcome_unknown
                } else {
                    R.string.configuration_edit_pending_banner
                },
            ),
            style = AppUiTheme.textStyles.body,
            color = if (outcomeUnknown) {
                AppUiTheme.palette.surfaceContent
            } else {
                AppUiTheme.palette.muted
            },
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (outcomeUnknown) {
                AppTextButton(
                    text = stringResource(R.string.configuration_edit_recheck),
                    onClick = onRecheck,
                )
            } else {
                AppTextButton(
                    text = stringResource(R.string.configuration_edit_retry_save),
                    onClick = onRetrySave,
                )
            }
            AppTextButton(
                text = stringResource(R.string.configuration_edit_discard),
                onClick = onDiscard,
            )
        }
    }
}
