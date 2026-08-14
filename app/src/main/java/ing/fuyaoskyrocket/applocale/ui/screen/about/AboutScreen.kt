package ing.fuyaoskyrocket.applocale.ui.screen.about

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import dagger.hilt.android.EntryPointAccessors
import ing.fuyaoskyrocket.applocale.BuildConfig
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.di.AppEntryPoint
import ing.fuyaoskyrocket.applocale.ui.components.AppIcon
import ing.fuyaoskyrocket.applocale.ui.components.AppLanguagePreference
import ing.fuyaoskyrocket.applocale.ui.components.AppNavigationBar
import ing.fuyaoskyrocket.applocale.ui.components.AppNavigationDestination
import ing.fuyaoskyrocket.applocale.ui.components.AppTopAppBarTitle
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppLayout
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing
import ing.fuyaoskyrocket.applocale.ui.designsystem.readableContentWidth

private const val PROJECT_REPOSITORY_URL =
    "https://github.com/skyrocketingHong/FuyaoLocale"
private const val ORIGINAL_PROJECT_URL =
    "https://github.com/VegaBobo/Language-Selector"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    showBottomNavigation: Boolean,
    navigateToHome: () -> Unit,
    navigateToConfigurations: () -> Unit,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val appIconLoader = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            AppEntryPoint::class.java,
        ).appIconLoader()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AppTopAppBarTitle(
                        title = stringResource(R.string.about),
                    )
                },
            )
        },
        bottomBar = {
            if (showBottomNavigation) {
                AppNavigationBar(
                    currentDestination = AppNavigationDestination.About,
                    onHomeClick = navigateToHome,
                    onConfigurationsClick = navigateToConfigurations,
                    onAboutClick = {},
                )
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                horizontal = if (showBottomNavigation) {
                    AppSpacing.screenCompact
                } else {
                    AppSpacing.screenExpanded
                },
                vertical = AppSpacing.lg,
            ),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item(key = "hero") {
                Surface(
                    modifier = Modifier.readableContentWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Row(
                        modifier = Modifier.padding(AppSpacing.lg),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AppIcon(
                            packageName = context.packageName,
                            iconLoader = appIconLoader,
                            modifier = Modifier.size(AppLayout.appHeroIconSize),
                        )
                        Column(
                            modifier = Modifier
                                .padding(start = AppSpacing.lg)
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        ) {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Text(
                                text = stringResource(
                                    R.string.version,
                                    BuildConfig.VERSION_NAME,
                                    BuildConfig.BUILD_NUMBER,
                                ),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            )
                        }
                    }
                }
            }

            item(key = "app_language") {
                Surface(
                    modifier = Modifier.readableContentWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                ) {
                    AppLanguagePreference(modifier = Modifier.fillMaxWidth())
                }
            }

            item(key = "features") {
                AboutSection(
                    title = stringResource(R.string.about_features),
                    modifier = Modifier.readableContentWidth(),
                ) {
                    FlowRow(
                        modifier = Modifier.padding(
                            start = AppSpacing.lg,
                            end = AppSpacing.lg,
                            bottom = AppSpacing.lg,
                        ),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    ) {
                        FeaturePill(stringResource(R.string.about_feature_per_app))
                        FeaturePill(stringResource(R.string.about_feature_batch))
                        FeaturePill(stringResource(R.string.about_feature_filters))
                        FeaturePill(stringResource(R.string.about_feature_quick_settings))
                        FeaturePill(stringResource(R.string.about_feature_adaptive))
                    }
                }
            }

            item(key = "usage_notes") {
                AboutSection(
                    title = stringResource(R.string.about_usage_notes),
                    modifier = Modifier.readableContentWidth(),
                ) {
                    AboutBody(
                        buildString {
                            append("• ")
                            append(stringResource(R.string.about_requirement_android))
                            append("\n• ")
                            append(stringResource(R.string.about_requirement_shizuku))
                            append("\n\n")
                            append(stringResource(R.string.about_usage_note))
                        },
                    )
                }
            }

            item(key = "open_source") {
                AboutSection(
                    title = stringResource(R.string.about_open_source),
                    modifier = Modifier.readableContentWidth(),
                ) {
                    RepositoryListItem(
                        title = stringResource(R.string.project_repository),
                        summary = stringResource(R.string.project_repository_summary),
                        onClick = { uriHandler.openUri(PROJECT_REPOSITORY_URL) },
                    )
                    RepositoryListItem(
                        title = stringResource(R.string.original_project),
                        summary = stringResource(R.string.original_project_summary),
                        onClick = { uriHandler.openUri(ORIGINAL_PROJECT_URL) },
                    )
                    AboutBody(stringResource(R.string.about_license))
                }
            }
        }
    }
}

@Composable
private fun AboutSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(vertical = AppSpacing.sm),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(
                    start = AppSpacing.lg,
                    top = AppSpacing.md,
                    end = AppSpacing.lg,
                    bottom = AppSpacing.sm,
                ),
            )
            content()
        }
    }
}

@Composable
private fun AboutBody(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(
            horizontal = AppSpacing.lg,
            vertical = AppSpacing.sm,
        ),
    )
}

@Composable
private fun FeaturePill(title: String) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = AppSpacing.md,
                vertical = AppSpacing.sm,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(AppSpacing.lg),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun RepositoryListItem(
    title: String,
    summary: String,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}
