package ing.fuyaoskyrocket.applocale.ui.screen.about

import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppInfoSection
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppInfoSectionDivider
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.appInfoTopInset
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import dagger.hilt.android.EntryPointAccessors
import ing.fuyaoskyrocket.applocale.BuildConfig
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.di.AppEntryPoint
import ing.fuyaoskyrocket.applocale.ui.components.AppIcon
import ing.fuyaoskyrocket.applocale.ui.components.AppNavigationDestination
import ing.fuyaoskyrocket.applocale.ui.screen.TabScrollCoordinator
import ing.fuyaoskyrocket.applocale.ui.screen.TabScrollToTopConsumer
import ing.fuyaoskyrocket.applocale.ui.screen.tabScrollToTopKeyAction
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.listBottomReserve
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppIcon as AppVectorIcon
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppScaffold
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSettingsRow
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppText
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppTopAppBar
import ing.fuyaoskyrocket.applocale.ui.designsystem.readableContentWidth

private const val PROJECT_REPOSITORY_URL =
    "https://github.com/skyrocketingHong/FuyaoLocale"
private const val ORIGINAL_PROJECT_URL =
    "https://github.com/VegaBobo/Language-Selector"
private const val MULTILOCALE_URL =
    "https://github.com/Nightdavisao/MultiLocale"
private const val LIQUID_GLASS_URL =
    "https://github.com/Kyant0/AndroidLiquidGlass"
private const val MIUIX_URL =
    "https://github.com/compose-miuix-ui/miuix"

@Composable
fun AboutScreen(
    tabScrollCoordinator: TabScrollCoordinator? = null,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val appIconLoader = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            AppEntryPoint::class.java,
        ).appIconLoader()
    }
    // Round-8 035: an explicit state so a tab double tap can scroll the credits
    // back to the top without touching any preference.
    val listState = rememberLazyListState()
    if (tabScrollCoordinator != null) {
        TabScrollToTopConsumer(
            coordinator = tabScrollCoordinator,
            destination = AppNavigationDestination.About,
            listState = listState,
        )
    }


    AppScaffold(
        topLevelNavigation = true,
        topBar = {
            AppTopAppBar(
                title = stringResource(R.string.about),
            )
        },
        bottomBar = {
            // The navigation dock is owned by AppChromeHost.
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = AppUiTheme.palette.background,
    ) { innerPadding ->
        // Round-7 031: continuous sections — the list keeps no horizontal inset
        // and no per-section card shells; every item frames itself in the shared
        // 840dp box with its own 16dp foreground edge, so nothing stacks insets.
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(innerPadding)
                .then(
                    if (tabScrollCoordinator != null) {
                        Modifier.tabScrollToTopKeyAction(
                            coordinator = tabScrollCoordinator,
                            destination = AppNavigationDestination.About,
                        )
                    } else {
                        Modifier
                    },
                ),
            contentPadding = PaddingValues(
                top = appInfoTopInset(innerPadding.calculateTopPadding()),
                // Keep the credits clear of the host dock's footprint.
                bottom = listBottomReserve(innerPadding.calculateBottomPadding()),
            ),
            verticalArrangement = Arrangement.spacedBy(AppUiTheme.spacing.sectionGap),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item(key = "hero") {
                // The brand block is a plain column on the page background — the
                // colourful mark is informational, never a selection-coloured
                // card. The identity row and the summary below each consume the
                // shared 16dp foreground edge once (AboutBody carries its own).
                AboutSection(
                    title = null,
                    modifier = Modifier.readableContentWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = AppUiTheme.spacing.contentInset, vertical = AppUiTheme.spacing.bodyVerticalPadding),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AppIcon(
                            packageName = context.packageName,
                            iconLoader = appIconLoader,
                            modifier = Modifier.size(AppUiTheme.icons.heroSize),
                        )
                        Column(
                            modifier = Modifier
                                .padding(start = AppUiTheme.spacing.heroPadding)
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        ) {
                            AppText(
                                text = stringResource(R.string.app_name),
                                style = AppUiTheme.textStyles.pageTitle,
                            )
                            AppText(
                                text = stringResource(
                                    R.string.version,
                                    BuildConfig.MARKETING_VERSION,
                                    BuildConfig.BUILD_NUMBER,
                                ),
                                style = AppUiTheme.textStyles.label,
                                color = AppUiTheme.palette.muted,
                            )
                        }
                    }
                    AboutBody(stringResource(R.string.about_summary))
                }
            }

            item(key = "features") {
                AboutSection(
                    title = stringResource(R.string.about_features),
                    modifier = Modifier.readableContentWidth(),
                ) {
                    AboutFeature(R.string.about_feature_per_app, R.string.about_feature_per_app_summary)
                    AboutFeature(R.string.about_feature_system_languages, R.string.about_feature_system_languages_summary)
                    AboutFeature(R.string.about_feature_batch, R.string.about_feature_batch_summary)
                    AboutBody(
                        listOf(R.string.about_requirement_android, R.string.about_requirement_shizuku, R.string.about_usage_note)
                            .joinToString(" ") { context.getString(it) },
                    )
                }
            }

            item(key = "regional_display_statement") {
                AboutSection(
                    title = stringResource(R.string.about_regional_display_statement),
                    modifier = Modifier.readableContentWidth(),
                ) {
                    AboutBody(stringResource(R.string.about_regional_display_statement_body))
                }
            }

            // Informational block → project credits.
            item(key = "divider_projects") {
                AboutSectionDivider()
            }

            item(key = "this_project") {
                AboutSection(
                    title = stringResource(R.string.about_this_project),
                    modifier = Modifier.readableContentWidth(),
                ) {
                    RepositoryListItem(
                        title = stringResource(R.string.project_repository),
                        summary = stringResource(R.string.project_repository_summary),
                        onClick = { uriHandler.openUri(PROJECT_REPOSITORY_URL) },
                    )
                    AboutBody(stringResource(R.string.about_license))
                }
            }

            item(key = "credits_and_references") {
                // Round-8 034: cited and reference projects merge into one
                // section; all four URLs are distinct and stay, in the order
                // Liquid Glass, miuix, original project, MultiLocale. The
                // credits paragraph appears exactly once.
                AboutSection(
                    title = stringResource(R.string.about_credits_and_references),
                    modifier = Modifier.readableContentWidth(),
                ) {
                    RepositoryListItem(
                        title = stringResource(R.string.credit_liquidglass),
                        summary = stringResource(R.string.credit_liquidglass_summary),
                        onClick = { uriHandler.openUri(LIQUID_GLASS_URL) },
                    )
                    RepositoryListItem(
                        title = stringResource(R.string.credit_miuix),
                        summary = stringResource(R.string.credit_miuix_summary),
                        onClick = { uriHandler.openUri(MIUIX_URL) },
                    )
                    RepositoryListItem(
                        title = stringResource(R.string.original_project),
                        summary = stringResource(R.string.original_project_summary),
                        onClick = { uriHandler.openUri(ORIGINAL_PROJECT_URL) },
                    )
                    RepositoryListItem(
                        title = stringResource(R.string.credit_multilocale),
                        summary = stringResource(R.string.credit_multilocale_summary),
                        onClick = { uriHandler.openUri(MULTILOCALE_URL) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutSectionDivider() = AppInfoSectionDivider(Modifier.readableContentWidth())

@Composable
private fun AboutSection(title: String?, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) =
    AppInfoSection(title, modifier, content)

@Composable
private fun AboutBody(text: String) {
    AppText(
        text = text,
        style = AppUiTheme.textStyles.body,
        color = AppUiTheme.palette.muted,
        modifier = Modifier.padding(
            horizontal = AppUiTheme.spacing.contentInset,
            vertical = AppSpacing.sm,
        ),
    )
}

@Composable
private fun AboutFeature(title: Int, summary: Int) {
    Column(
        modifier = Modifier.padding(
            horizontal = AppUiTheme.spacing.contentInset,
            vertical = AppSpacing.sm,
        ),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        AppText(
            text = stringResource(title),
            style = AppUiTheme.textStyles.itemTitle,
        )
        AppText(
            text = stringResource(summary),
            style = AppUiTheme.textStyles.body,
            color = AppUiTheme.palette.muted,
        )
    }
}

@Composable
private fun RepositoryListItem(
    title: String,
    summary: String,
    onClick: () -> Unit,
) {
    AppSettingsRow(
        title = title,
        summary = summary,
        summaryMaxLines = 2,
        onClick = onClick,
        trailing = {
            // No miuix open-externally glyph; the project vector is drawn by the
            // backend icon control (asset exception).
            AppVectorIcon(
                imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                contentDescription = null,
                tint = AppUiTheme.palette.muted,
            )
        },
    )
}
