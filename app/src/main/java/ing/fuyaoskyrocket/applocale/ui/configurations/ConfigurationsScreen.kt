package ing.fuyaoskyrocket.applocale.ui.configurations

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Color
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.model.SavedLocaleConfiguration
import ing.fuyaoskyrocket.applocale.ui.components.AppNavigationBar
import ing.fuyaoskyrocket.applocale.ui.components.AppNavigationDestination
import ing.fuyaoskyrocket.applocale.ui.components.AppTopAppBarTitle
import ing.fuyaoskyrocket.applocale.ui.components.predictiveBackTransform
import ing.fuyaoskyrocket.applocale.ui.components.rememberPredictiveBackMotion
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppLayout
import ing.fuyaoskyrocket.applocale.ui.designsystem.readableContentWidth
import ing.fuyaoskyrocket.applocale.ui.designsystem.wideContentWidth
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurationsScreen(
    navigateToHome: () -> Unit,
    navigateToAbout: () -> Unit,
    onOpenConfiguration: (String) -> Unit,
    showBottomNavigation: Boolean,
    useWideLayout: Boolean,
    viewModel: ConfigurationsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val importScope = rememberCoroutineScope()
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
        }
    }

    val openImportPicker = {
        importLauncher.launch(arrayOf("application/json", "text/*"))
    }
    val exportConfiguration: (SavedLocaleConfiguration) -> Unit = { configuration ->
        pendingExportId = configuration.id
        exportLauncher.launch(configuration.exportFileName())
    }

    val predictiveBackMotion = rememberPredictiveBackMotion(
        enabled = useWideLayout && uiState.selectedConfigurationId != null,
        onBack = viewModel::clearSelection,
    )

    Scaffold(
        modifier = Modifier.predictiveBackTransform(predictiveBackMotion),
        topBar = {
            TopAppBar(
                title = {
                    AppTopAppBarTitle(
                        title = stringResource(R.string.configurations),
                    )
                },
                actions = {
                    IconButton(
                        onClick = viewModel::saveCurrentChanges,
                        enabled = !uiState.isSaving && !uiState.isImporting,
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(AppSpacing.lg),
                                strokeWidth = AppSpacing.xs / 2,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.BookmarkAdd,
                                contentDescription = stringResource(R.string.save_current_changes),
                            )
                        }
                    }
                    IconButton(
                        onClick = openImportPicker,
                        enabled = !uiState.isSaving && !uiState.isImporting,
                    ) {
                        if (uiState.isImporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(AppSpacing.lg),
                                strokeWidth = AppSpacing.xs / 2,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.FileOpen,
                                contentDescription = stringResource(R.string.import_configuration),
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (showBottomNavigation) {
                AppNavigationBar(
                    currentDestination = AppNavigationDestination.Configurations,
                    onHomeClick = navigateToHome,
                    onConfigurationsClick = {},
                    onAboutClick = navigateToAbout,
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPadding ->
        if (useWideLayout) {
            WideConfigurationsContent(
                uiState = uiState,
                onSelect = viewModel::selectConfiguration,
                onApply = viewModel::apply,
                onRefresh = viewModel::refreshSelectedComparison,
                onDelete = viewModel::delete,
                onExport = exportConfiguration,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        } else {
            CompactConfigurationsContent(
                configurations = uiState.configurations,
                onOpen = onOpenConfiguration,
                onDelete = viewModel::delete,
                onExport = exportConfiguration,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        }
    }
}

@Composable
private fun CompactConfigurationsContent(
    configurations: List<SavedLocaleConfiguration>,
    onOpen: (String) -> Unit,
    onDelete: (String) -> Unit,
    onExport: (SavedLocaleConfiguration) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            horizontal = AppSpacing.lg,
            vertical = AppSpacing.lg,
        ),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        configurationListItems(
            configurations = configurations,
            selectedConfigurationId = null,
            onOpen = onOpen,
            onDelete = onDelete,
            onExport = onExport,
            itemModifier = Modifier.readableContentWidth(),
        )
    }
}

@Composable
private fun WideConfigurationsContent(
    uiState: ConfigurationsUiState,
    onSelect: (String) -> Unit,
    onApply: (String) -> Unit,
    onRefresh: () -> Unit,
    onDelete: (String) -> Unit,
    onExport: (SavedLocaleConfiguration) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedConfiguration = uiState.configurations.firstOrNull {
        it.id == uiState.selectedConfigurationId
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter,
    ) {
        Row(
            modifier = Modifier
                .wideContentWidth()
                .fillMaxHeight()
                .padding(
                    horizontal = AppSpacing.screenExpanded,
                    vertical = AppSpacing.screenExpanded,
                ),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.paneGap),
        ) {
            Surface(
                modifier = Modifier
                    .width(AppLayout.listPaneWidth)
                    .fillMaxHeight(),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                ConfigurationList(
                    configurations = uiState.configurations,
                    selectedConfigurationId = uiState.selectedConfigurationId,
                    onOpen = onSelect,
                    onDelete = onDelete,
                    onExport = onExport,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
            ) {
                SavedConfigurationDetailContent(
                    configuration = selectedConfiguration,
                    comparison = uiState.comparison,
                    isComparing = uiState.isComparing,
                    isApplying = uiState.applyingConfigurationId == selectedConfiguration?.id,
                    onApply = { selectedConfiguration?.let { onApply(it.id) } },
                    onRefresh = onRefresh,
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
    itemModifier: Modifier = Modifier,
) {
    if (configurations.isEmpty()) {
        item(key = "empty") { EmptyConfigurationsState(modifier = itemModifier) }
    } else {
        items(configurations, key = { it.id }) { configuration ->
            ConfigurationListItem(
                configuration = configuration,
                isSelected = selectedConfigurationId == configuration.id,
                onOpen = { onOpen(configuration.id) },
                onDelete = { onDelete(configuration.id) },
                onExport = { onExport(configuration) },
                modifier = itemModifier,
            )
        }
    }
}

@Composable
private fun ConfigurationList(
    configurations: List<SavedLocaleConfiguration>,
    selectedConfigurationId: String?,
    onOpen: (String) -> Unit,
    onDelete: (String) -> Unit,
    onExport: (SavedLocaleConfiguration) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            horizontal = AppSpacing.lg,
            vertical = AppSpacing.lg,
        ),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        configurationListItems(
            configurations = configurations,
            selectedConfigurationId = selectedConfigurationId,
            onOpen = onOpen,
            onDelete = onDelete,
            onExport = onExport,
            itemModifier = Modifier.fillMaxWidth(),
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
    modifier: Modifier = Modifier,
) {
    val formattedDate = remember(configuration.createdAt) {
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
            .format(Date(configuration.createdAt))
    }
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerLowest
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = containerColor,
    ) {
        ListItem(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = AppLayout.appListItemMinHeight)
                .combinedClickable(
                    onClick = onOpen,
                    onLongClick = onExport,
                ),
            headlineContent = {
                Text(
                    text = stringResource(R.string.configuration_saved_at, formattedDate),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = contentColor,
                )
            },
            supportingContent = {
                Text(
                    text = stringResource(
                        R.string.configuration_app_count,
                        configuration.appCount,
                    ),
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.Bookmarks,
                    contentDescription = null,
                    tint = contentColor,
                )
            },
            trailingContent = {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = stringResource(R.string.delete_configuration),
                        tint = contentColor,
                    )
                }
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
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
        Icon(
            imageVector = Icons.Outlined.Bookmarks,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(AppSpacing.xl),
        )
        Text(
            text = stringResource(R.string.no_saved_configurations),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.no_saved_configurations_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
