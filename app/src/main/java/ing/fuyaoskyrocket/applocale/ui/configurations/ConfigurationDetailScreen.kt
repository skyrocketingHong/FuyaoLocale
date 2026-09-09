package ing.fuyaoskyrocket.applocale.ui.configurations

import ing.fuyaoskyrocket.applocale.ui.designsystem.component.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.ui.screen.pageHiltViewModel as hiltViewModel
import ing.fuyaoskyrocket.applocale.ui.screen.collectPageUiState
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.data.system.AppIconLoader
import ing.fuyaoskyrocket.applocale.model.ConfigurationAppProjection
import ing.fuyaoskyrocket.applocale.model.ConfigurationAppSection
import ing.fuyaoskyrocket.applocale.model.SavedLocaleConfiguration
import ing.fuyaoskyrocket.applocale.ui.components.rememberSystemLocaleTag
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppCircularProgressIndicator
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppDropdownItem
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppFilledTonalButton
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppIconButton
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppScaffold
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSearchField
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSettingsRow
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSnackbarHost
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSymbol
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSymbolIcon
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppText
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppTextButton
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppTopAppBar
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.rememberAppSnackbarHostState
import ing.fuyaoskyrocket.applocale.ui.designsystem.listBottomReserve
import ing.fuyaoskyrocket.applocale.ui.designsystem.listTopReserve
import ing.fuyaoskyrocket.applocale.ui.designsystem.readableContentWidth
import java.text.DateFormat
import java.util.Date

@Composable
fun ConfigurationDetailScreen(
    configurationId: String,
    navigateBack: () -> Unit,
    onConfigurationReplaced: (newConfigurationId: String, anchorPackage: String) -> Unit,
    anchorPackage: String?,
    onAnchorConsumed: () -> Unit,
    viewModel: ConfigurationsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectPageUiState()
    val context = LocalContext.current
    val snackbarHostState = rememberAppSnackbarHostState()
    // The single-app chooser session (round-8 038); the sheet stays in
    // composition so its exit animation survives a selection.
    var editingPackage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(configurationId) {
        viewModel.selectConfiguration(configurationId)
    }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            snackbarHostState.showSnackbar(context.configurationEventMessage(event))
            // The derived configuration replaces THIS route in place — no
            // "new detail → old detail" chain. Only a completion for the
            // configuration this page still shows navigates; a user who left
            // never gets pulled back.
            if (event is ConfigurationsEvent.EditCompleted &&
                event.newConfigurationId != null &&
                event.sourceConfigurationId == configurationId
            ) {
                onConfigurationReplaced(event.newConfigurationId, event.packageName)
            }
        }
    }

    AppScaffold(
        topBar = {
            AppTopAppBar(
                title = stringResource(R.string.configuration_details),
                navigationIcon = {
                    AppIconButton(onClick = navigateBack) {
                        AppSymbolIcon(
                            symbol = AppSymbol.Back,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                // Round-8 037: the refresh action moved to the phone top bar —
                // the same callback the wide tool row uses, never a second path.
                actions = {
                    AppIconButton(
                        onClick = viewModel::refreshSelectedComparison,
                        enabled = !uiState.isComparing && uiState.applyingConfigurationId == null,
                    ) {
                        AppSymbolIcon(
                            symbol = AppSymbol.Refresh,
                            contentDescription = stringResource(R.string.refresh),
                        )
                    }
                },
            )
        },
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = AppUiTheme.palette.background,
    ) { innerPadding ->
        val configuration = uiState.configurations.firstOrNull { it.id == configurationId }
        SavedConfigurationDetailContent(
            configuration = configuration,
            detailRows = uiState.detailRows
                ?.takeIf { uiState.selectedConfigurationId == configurationId },
            isComparing = uiState.isComparing,
            isApplying = uiState.applyingConfigurationId == configurationId,
            iconLoader = viewModel.appIconLoader,
            onApply = { viewModel.apply(configurationId) },
            onRefresh = viewModel::refreshSelectedComparison,
            // Editing opens the shared single-app chooser; rows stand down
            // while any command runs or a pending record is unresolved.
            onEditApp = if (uiState.isEditBusy) null else ({ packageName ->
                editingPackage = packageName
            }),
            editingPackageName = uiState.editingPackageName,
            editBusy = uiState.isEditBusy,
            showRefreshAction = false,
            anchorPackage = anchorPackage,
            onAnchorConsumed = onAnchorConsumed,
            contentPadding = innerPadding,
            modifier = Modifier.fillMaxSize(),
        )
    }

    ConfigurationLanguageSheet(
        visible = editingPackage != null,
        packageName = editingPackage,
        // The explicit configured target wins; otherwise the app's real
        // current language seeds the check.
        selectedLanguageTag = uiState.detailRows
            ?.firstOrNull { it.packageName == editingPackage }
            ?.let { row -> row.savedLocaleTag ?: row.currentLocaleTag },
        onDismiss = { editingPackage = null },
        onLocaleSelected = { tag ->
            val packageName = editingPackage
            if (packageName != null) {
                editingPackage = null
                viewModel.submitEdit(
                    sourceConfigurationId = configurationId,
                    packageName = packageName,
                    targetLocaleTag = tag,
                )
            }
        },
    )
}

/** The detail's tri-state filter chips (round-8 037). */
internal enum class ConfigurationDetailFilter { All, Differences }

/**
 * Reused by the compact detail route and the expanded configuration master-detail
 * layout (round-8 037): a compact header meta block with one primary action, a
 * single-row filter chip bar, then CONTINUOUS app rows — no nested comparison
 * cards. Row identity is the packageName so a re-classified row keeps its place.
 *
 * [contentPadding] is consumed exactly once through the list reserves so the
 * background scrolls under the bars while text stays clear (phone passes the
 * scaffold insets; the wide pane passes zero — its parent already consumed them).
 */
@Composable
internal fun SavedConfigurationDetailContent(
    configuration: SavedLocaleConfiguration?,
    detailRows: List<ConfigurationAppProjection>?,
    isComparing: Boolean,
    isApplying: Boolean,
    iconLoader: AppIconLoader,
    onApply: () -> Unit,
    onRefresh: () -> Unit,
    onEditApp: ((packageName: String) -> Unit)?,
    editingPackageName: String?,
    editBusy: Boolean,
    showRefreshAction: Boolean,
    anchorPackage: String?,
    onAnchorConsumed: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    if (configuration == null) {
        EmptyConfigurationSelection(modifier)
        return
    }
    val listState = rememberLazyListState()
    val systemLocaleTag = rememberSystemLocaleTag()
    var detailFilter by rememberSaveable { mutableStateOf(ConfigurationDetailFilter.All) }
    var othersExpanded by rememberSaveable { mutableStateOf(false) }
    var othersQuery by rememberSaveable { mutableStateOf("") }

    // Only a real user-driven switch to another configuration scrolls to the
    // top (the wide pane keeps this composable alive). A derived auto-save
    // (038) keeps the edited row's anchor instead: the anchor survives until
    // the rows arrive, then scrolls once to the SAME package row whose key
    // never changed, and never scrolls to the top for that switch.
    var lastSeenConfigurationId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(configuration.id) {
        if (anchorPackage == null &&
            lastSeenConfigurationId != null &&
            lastSeenConfigurationId != configuration.id
        ) {
            listState.scrollToItem(0)
        }
        lastSeenConfigurationId = configuration.id
    }
    var anchorApplied by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(detailRows, anchorPackage) {
        if (anchorApplied || anchorPackage == null || detailRows == null) return@LaunchedEffect
        val managed = detailRows.filter { it.section != ConfigurationAppSection.OtherApps }
        val headerItems = 1 + // summary
            (if (managed.isNotEmpty() || isComparing) 1 else 0) // filter chips
        val index = headerItems + managed.indexOfFirst { it.packageName == anchorPackage }
        if (index >= headerItems) {
            listState.scrollToItem(index)
            anchorApplied = true
            onAnchorConsumed()
        }
    }

    val managedRows = remember(detailRows) {
        detailRows.orEmpty().filter { it.section != ConfigurationAppSection.OtherApps }
    }
    val visibleRows = when (detailFilter) {
        ConfigurationDetailFilter.All -> managedRows
        ConfigurationDetailFilter.Differences -> managedRows.filter {
            it.needsAttention || it.section == ConfigurationAppSection.ExternalModified
        }
    }
    val otherRows = remember(detailRows, othersQuery) {
        val others = detailRows.orEmpty().filter { it.section == ConfigurationAppSection.OtherApps }
        val query = othersQuery.trim()
        if (query.isEmpty()) {
            others
        } else {
            others.filter {
                it.label.contains(query, ignoreCase = true) ||
                    it.packageName.contains(query, ignoreCase = true)
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(
            top = listTopReserve(contentPadding.calculateTopPadding()),
            bottom = listBottomReserve(contentPadding.calculateBottomPadding()),
        ),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item(key = "summary") {
            ConfigurationSummaryHeader(
                configuration = configuration,
                // A live edit or unresolved pending record stands the whole
                // configuration's actions down, not just the edited row.
                isApplying = isApplying || editBusy,
                isComparing = isComparing,
                onApply = onApply,
                onRefresh = onRefresh,
                showRefreshAction = showRefreshAction,
                modifier = Modifier
                    .readableContentWidth()
                    .padding(horizontal = AppUiTheme.spacing.contentInset),
            )
        }

        if (managedRows.isNotEmpty() || isComparing) {
            item(key = "filter_chips") {
                ConfigurationFilterChipRow(
                    selected = detailFilter,
                    onSelect = { detailFilter = it },
                    modifier = Modifier
                        .readableContentWidth()
                        .padding(horizontal = AppUiTheme.spacing.contentInset),
                )
            }
        }

        when {
            isComparing -> item(key = "loading") {
                Box(
                    modifier = Modifier
                        .readableContentWidth()
                        .fillMaxWidth()
                        .padding(AppSpacing.xl),
                    contentAlignment = Alignment.Center,
                ) {
                    AppCircularProgressIndicator()
                }
            }

            detailRows == null -> item(key = "unavailable") {
                Column(
                    modifier = Modifier
                        .readableContentWidth()
                        .padding(horizontal = AppUiTheme.spacing.contentInset),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    AppText(
                        text = stringResource(R.string.configuration_operation_failed),
                        style = AppUiTheme.textStyles.body,
                        color = AppUiTheme.palette.muted,
                    )
                    AppTextButton(
                        text = stringResource(R.string.refresh),
                        onClick = onRefresh,
                    )
                }
            }

            visibleRows.isEmpty() -> item(key = "no_visible_rows") {
                AppText(
                    text = stringResource(R.string.configuration_filter_empty),
                    style = AppUiTheme.textStyles.body,
                    color = AppUiTheme.palette.muted,
                    modifier = Modifier
                        .readableContentWidth()
                        .padding(horizontal = AppUiTheme.spacing.contentInset),
                )
            }

            else -> items(
                items = visibleRows,
                key = { it.packageName },
                contentType = { "configuration_app" },
            ) { row ->
                ConfigurationAppRow(
                    row = row,
                    systemLocaleTag = systemLocaleTag,
                    iconLoader = iconLoader,
                    onEdit = onEditApp?.let { edit -> if (row.isInstalled) ({ edit(row.packageName) }) else null },
                    isProcessing = editingPackageName == row.packageName,
                    modifier = Modifier
                        .readableContentWidth()
                        .padding(horizontal = AppUiTheme.spacing.rowOuterInset),
                )
            }
        }

        if (!isComparing && detailRows != null) {
            item(key = "others_header") {
                OtherAppsHeader(
                    count = detailRows.count { it.section == ConfigurationAppSection.OtherApps },
                    expanded = othersExpanded,
                    onToggle = { othersExpanded = !othersExpanded },
                    modifier = Modifier
                        .readableContentWidth(),
                )
            }
            if (othersExpanded) {
                item(key = "others_search") {
                    AppSearchField(
                        query = othersQuery,
                        onQueryChange = { othersQuery = it },
                        placeholder = stringResource(R.string.configuration_other_apps_search_hint),
                        modifier = Modifier
                            .readableContentWidth()
                            .padding(horizontal = AppUiTheme.spacing.contentInset),
                    )
                }
                if (otherRows.isEmpty()) {
                    item(key = "others_empty") {
                        AppText(
                            text = stringResource(R.string.configuration_other_apps_empty),
                            style = AppUiTheme.textStyles.body,
                            color = AppUiTheme.palette.muted,
                            modifier = Modifier
                                .readableContentWidth()
                                .padding(horizontal = AppUiTheme.spacing.contentInset),
                        )
                    }
                } else {
                    items(
                        items = otherRows,
                        key = { it.packageName },
                        contentType = { "configuration_app" },
                    ) { row ->
                        ConfigurationAppRow(
                            row = row,
                            systemLocaleTag = systemLocaleTag,
                            iconLoader = iconLoader,
                            onEdit = onEditApp?.let { edit -> if (row.isInstalled) ({ edit(row.packageName) }) else null },
                            isProcessing = editingPackageName == row.packageName,
                            modifier = Modifier
                                .readableContentWidth()
                                .padding(horizontal = AppUiTheme.spacing.rowOuterInset),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfigurationSummaryHeader(
    configuration: SavedLocaleConfiguration,
    isApplying: Boolean,
    isComparing: Boolean,
    onApply: () -> Unit,
    onRefresh: () -> Unit,
    showRefreshAction: Boolean,
    modifier: Modifier = Modifier,
) {
    val formattedDate = remember(configuration.createdAt) {
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
            .format(Date(configuration.createdAt))
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        // One or two quiet meta lines — no panel, no two big paragraphs.
        AppText(
            text = stringResource(R.string.configuration_saved_at, formattedDate),
            style = AppUiTheme.textStyles.metadata,
            color = AppUiTheme.palette.muted,
        )
        AppText(
            text = stringResource(R.string.configuration_app_count, configuration.appCount),
            style = AppUiTheme.textStyles.metadata,
            color = AppUiTheme.palette.muted,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            // The single primary action; 48dp minimum touch height. The wide
            // pane pairs it with the refresh icon, the phone keeps refresh in
            // its top bar — both call the same callbacks.
            AppFilledTonalButton(
                text = stringResource(R.string.apply_configuration),
                onClick = onApply,
                enabled = !isApplying,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp),
            )
            if (showRefreshAction) {
                AppIconButton(
                    onClick = onRefresh,
                    enabled = !isComparing && !isApplying,
                    modifier = Modifier.size(48.dp),
                ) {
                    AppSymbolIcon(
                        symbol = AppSymbol.Refresh,
                        contentDescription = stringResource(R.string.refresh),
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfigurationFilterChipRow(
    selected: ConfigurationDetailFilter,
    onSelect: (ConfigurationDetailFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val labels = listOf(
        ConfigurationDetailFilter.All to stringResource(R.string.configuration_filter_all),
        ConfigurationDetailFilter.Differences to stringResource(R.string.configuration_filter_differences),
    )
    AppFilterControls(
        currentLabel = labels.first { it.first == selected }.second,
        options = labels.map { (filter, label) -> AppFilterOption(filter.name, label, selected == filter, { onSelect(filter) }) },
        menuItems = labels.map { (filter, label) -> AppDropdownItem(label, selected == filter, onClick = { onSelect(filter) }) },
        modifier = modifier,
    )
}

@Composable
private fun OtherAppsHeader(
    count: Int,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // A quiet expandable section header following the settings-row anatomy;
    // the chevron rotates in place to carry the expand/collapse state.
    AppSettingsRow(
        title = otherAppsSectionTitle(count),
        onClick = onToggle,
        modifier = modifier,
        trailing = { AppRowAffordance(expanded = expanded) },
    )
}

@Composable
private fun otherAppsSectionTitle(count: Int): String =
    stringResource(R.string.configuration_section_other_apps, count)

@Composable
private fun EmptyConfigurationSelection(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            AppText(
                text = stringResource(R.string.no_configuration_selected),
                style = AppUiTheme.textStyles.itemTitle,
            )
            AppText(
                text = stringResource(R.string.no_configuration_selected_description),
                style = AppUiTheme.textStyles.body,
                color = AppUiTheme.palette.muted,
            )
        }
    }
}
