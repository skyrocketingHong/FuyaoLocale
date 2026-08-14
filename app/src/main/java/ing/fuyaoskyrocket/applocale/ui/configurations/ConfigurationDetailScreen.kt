package ing.fuyaoskyrocket.applocale.ui.configurations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Color
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.model.SavedLocaleConfiguration
import ing.fuyaoskyrocket.applocale.model.SavedLocaleConfigurationComparison
import ing.fuyaoskyrocket.applocale.model.SavedLocaleDifference
import ing.fuyaoskyrocket.applocale.model.SavedLocaleDifferenceKind
import ing.fuyaoskyrocket.applocale.ui.components.AppTopAppBarTitle
import ing.fuyaoskyrocket.applocale.ui.components.rememberSystemLocaleTag
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing
import ing.fuyaoskyrocket.applocale.ui.designsystem.readableContentWidth
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurationDetailScreen(
    configurationId: String,
    navigateBack: () -> Unit,
    viewModel: ConfigurationsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(configurationId) {
        viewModel.selectConfiguration(configurationId)
    }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            snackbarHostState.showSnackbar(context.configurationEventMessage(event))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AppTopAppBarTitle(title = stringResource(R.string.configuration_details))
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPadding ->
        val configuration = uiState.configurations.firstOrNull { it.id == configurationId }
        SavedConfigurationDetailContent(
            configuration = configuration,
            comparison = uiState.comparison?.takeIf { it.configuration.id == configurationId },
            isComparing = uiState.isComparing,
            isApplying = uiState.applyingConfigurationId == configurationId,
            onApply = { viewModel.apply(configurationId) },
            onRefresh = viewModel::refreshSelectedComparison,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )
    }
}

/** Reused by the compact detail route and the expanded configuration master-detail layout. */
@Composable
internal fun SavedConfigurationDetailContent(
    configuration: SavedLocaleConfiguration?,
    comparison: SavedLocaleConfigurationComparison?,
    isComparing: Boolean,
    isApplying: Boolean,
    onApply: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (configuration == null) {
        EmptyConfigurationSelection(modifier)
        return
    }
    val listState = rememberLazyListState()
    val systemLocaleTag = rememberSystemLocaleTag()

    LaunchedEffect(configuration.id) {
        listState.scrollToItem(0)
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item(key = "summary") {
            ConfigurationSummaryCard(
                configuration = configuration,
                isComparing = isComparing,
                isApplying = isApplying,
                onApply = onApply,
                onRefresh = onRefresh,
                modifier = Modifier.readableContentWidth(),
            )
        }
        when {
            isComparing -> item(key = "loading") {
                Box(
                    modifier = Modifier
                        .readableContentWidth()
                        .fillMaxWidth()
                        .padding(AppSpacing.xxl),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            comparison == null -> item(key = "unavailable") {
                ConfigurationComparisonUnavailable(
                    modifier = Modifier.readableContentWidth(),
                )
            }

            comparison.differences.isEmpty() -> item(key = "no_differences") {
                NoDifferencesState(modifier = Modifier.readableContentWidth())
            }

            else -> {
                item(key = "differences_title") {
                    Column(
                        modifier = Modifier.readableContentWidth(),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    ) {
                        Text(
                            text = stringResource(
                                R.string.configuration_differences,
                                comparison.differences.size,
                            ),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = stringResource(R.string.configuration_difference_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                items(
                    items = comparison.differences,
                    key = { "${it.kind}:${it.packageName}" },
                    contentType = { "difference" },
                ) { difference ->
                    ConfigurationDifferenceItem(
                        difference = difference,
                        systemLocaleTag = systemLocaleTag,
                        modifier = Modifier.readableContentWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfigurationSummaryCard(
    configuration: SavedLocaleConfiguration,
    isComparing: Boolean,
    isApplying: Boolean,
    onApply: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val formattedDate = remember(configuration.createdAt) {
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
            .format(Date(configuration.createdAt))
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(
                text = stringResource(R.string.configuration_saved_at, formattedDate),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.configuration_app_count, configuration.appCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                FilledTonalButton(
                    onClick = onApply,
                    enabled = !isApplying,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (isApplying) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(AppSpacing.lg),
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Sync,
                            contentDescription = null,
                            modifier = Modifier.padding(end = AppSpacing.xs),
                        )
                    }
                    Text(stringResource(R.string.apply_configuration))
                }
                OutlinedButton(
                    onClick = onRefresh,
                    enabled = !isComparing && !isApplying,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = null,
                        modifier = Modifier.padding(end = AppSpacing.xs),
                    )
                    Text(stringResource(R.string.refresh))
                }
            }
        }
    }
}

@Composable
private fun ConfigurationDifferenceItem(
    difference: SavedLocaleDifference,
    systemLocaleTag: String,
    modifier: Modifier = Modifier,
) {
    val typeText = when (difference.kind) {
        SavedLocaleDifferenceKind.NeedsApply -> stringResource(R.string.difference_needs_apply)
        SavedLocaleDifferenceKind.MissingApp -> stringResource(R.string.difference_missing_app)
        SavedLocaleDifferenceKind.CurrentOnly -> stringResource(R.string.difference_current_only)
    }
    val icon = when (difference.kind) {
        SavedLocaleDifferenceKind.NeedsApply -> Icons.Outlined.Sync
        SavedLocaleDifferenceKind.MissingApp -> Icons.Outlined.ErrorOutline
        SavedLocaleDifferenceKind.CurrentOnly -> Icons.Outlined.Info
    }
    val containerColor = when (difference.kind) {
        SavedLocaleDifferenceKind.NeedsApply -> MaterialTheme.colorScheme.primaryContainer
        SavedLocaleDifferenceKind.MissingApp -> MaterialTheme.colorScheme.errorContainer
        SavedLocaleDifferenceKind.CurrentOnly -> MaterialTheme.colorScheme.surfaceContainerLow
    }
    val contentColor = when (difference.kind) {
        SavedLocaleDifferenceKind.NeedsApply -> MaterialTheme.colorScheme.onPrimaryContainer
        SavedLocaleDifferenceKind.MissingApp -> MaterialTheme.colorScheme.onErrorContainer
        SavedLocaleDifferenceKind.CurrentOnly -> MaterialTheme.colorScheme.onSurface
    }
    val systemDefaultLocale = if (systemLocaleTag.isBlank()) {
        stringResource(R.string.system_default)
    } else {
        stringResource(R.string.system_default_with_locale, systemLocaleTag)
    }
    val currentLocale = when (difference.kind) {
        SavedLocaleDifferenceKind.MissingApp -> stringResource(R.string.unavailable)
        else -> difference.currentLocaleTag ?: systemDefaultLocale
    }
    val savedLocale = when (difference.kind) {
        SavedLocaleDifferenceKind.CurrentOnly -> stringResource(R.string.unavailable)
        else -> difference.savedLocaleTag ?: systemDefaultLocale
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = containerColor,
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = difference.label,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = contentColor,
                )
            },
            overlineContent = { Text(typeText, color = contentColor) },
            supportingContent = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    Text(
                        text = difference.packageName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = contentColor,
                    )
                    LocaleChangeRow(
                        currentLocale = currentLocale,
                        savedLocale = savedLocale,
                        contentColor = contentColor,
                    )
                }
            },
            leadingContent = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}

@Composable
private fun LocaleChangeRow(
    currentLocale: String,
    savedLocale: String,
    contentColor: Color,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = contentColor.copy(alpha = 0.10f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = AppSpacing.lg,
                vertical = AppSpacing.sm,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.difference_current_short),
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor,
                )
                Text(
                    text = currentLocale,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                tint = contentColor,
            )
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = stringResource(R.string.difference_saved_short),
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor,
                )
                Text(
                    text = savedLocale,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

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
            Text(
                text = stringResource(R.string.no_configuration_selected),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.no_configuration_selected_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun NoDifferencesState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(AppSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Icon(
            imageVector = Icons.Outlined.Sync,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.configuration_no_differences),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.configuration_no_differences_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ConfigurationComparisonUnavailable(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.configuration_operation_failed),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(AppSpacing.lg),
    )
}
