package ing.fuyaoskyrocket.applocale.ui.screen.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import dagger.hilt.android.EntryPointAccessors
import ing.fuyaoskyrocket.applocale.BuildConfig
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.di.AppEntryPoint
import ing.fuyaoskyrocket.applocale.ui.components.AppIcon
import ing.fuyaoskyrocket.applocale.ui.components.AppLanguagePreference
import ing.fuyaoskyrocket.applocale.ui.components.AppNavigationDestination
import ing.fuyaoskyrocket.applocale.ui.screen.TabScrollCoordinator
import ing.fuyaoskyrocket.applocale.ui.screen.TabScrollToTopConsumer
import ing.fuyaoskyrocket.applocale.ui.screen.tabScrollToTopKeyAction
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppComponentDefaults
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppLayout
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemePreferences
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemeStyle
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppearanceRequester
import ing.fuyaoskyrocket.applocale.ui.designsystem.LocalAppearanceFailures
import ing.fuyaoskyrocket.applocale.ui.designsystem.LocalAppearanceRequester
import ing.fuyaoskyrocket.applocale.ui.designsystem.isEffectRenderingSupported
import ing.fuyaoskyrocket.applocale.ui.designsystem.listBottomReserve
import ing.fuyaoskyrocket.applocale.ui.designsystem.listTopReserve
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppIcon as AppVectorIcon
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppDivider
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppRadioButton
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppScaffold
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSettingsRow
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSnackbarHost
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSurface
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSymbol
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSymbolVector
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSwitch
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppText
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppTopAppBar
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.rememberAppSnackbarHostState
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
    showBottomNavigation: Boolean,
    navigateToHome: () -> Unit,
    navigateToSystemLanguages: () -> Unit,
    navigateToConfigurations: () -> Unit,
    tabScrollCoordinator: TabScrollCoordinator? = null,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val requester = LocalAppearanceRequester.current
    val themeStyle = AppThemePreferences.style
    val glassNavigationBar = AppThemePreferences.liquidGlassNavigationBar
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

    // Appearance transactions start here, so this page owns their failure
    // feedback: a setter failure or timeout recovers to the real values and
    // pings once through the shared snackbar path.
    val snackbarHostState = rememberAppSnackbarHostState()
    val appearanceFailures = LocalAppearanceFailures.current
    LaunchedEffect(appearanceFailures) {
        appearanceFailures?.collect {
            snackbarHostState.showSnackbar(context.getString(R.string.appearance_change_failed))
        }
    }

    val moreBlur = AppThemePreferences.moreBlur

    AppScaffold(
        topBar = {
            AppTopAppBar(
                title = stringResource(R.string.about),
            )
        },
        bottomBar = {
            // The navigation dock is owned by AppChromeHost.
        },
        snackbarHost = {
            AppSnackbarHost(snackbarHostState)
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
                top = listTopReserve(innerPadding.calculateTopPadding()),
                // Keep the credits clear of the host dock's footprint.
                bottom = listBottomReserve(innerPadding.calculateBottomPadding()),
            ),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Round-8 034: the seven fixed major sections in order —
            // identity+summary, app language, theme, features (incl. usage
            // notes), regional display statement, this project, merged credits
            // and references. Only the major block boundaries keep a divider:
            // settings (identity/language/theme) → informational
            // (features/statement), informational → project credits.
            item(key = "hero") {
                // The brand block is a plain column on the page background — the
                // colourful mark is informational, never a selection-coloured
                // card. The identity row and the summary below each consume the
                // shared 16dp foreground edge once (AboutBody carries its own).
                Column(modifier = Modifier.readableContentWidth()) {
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

            item(key = "app_language") {
                // No wrapper: the settings row's own native 16dp inset is the
                // final foreground edge, flush with the brand block's frame.
                AppLanguagePreference(
                    modifier = Modifier.readableContentWidth(),
                )
            }

            item(key = "theme") {
                AboutSection(
                    title = stringResource(R.string.about_theme),
                    modifier = Modifier.readableContentWidth(),
                ) {
                    ThemeOptionRow(
                        title = stringResource(R.string.theme_material_you),
                        selected = themeStyle == AppThemeStyle.MATERIAL_YOU,
                        onRequest = { rowFocus ->
                            requester?.requestTheme(AppThemeStyle.MATERIAL_YOU, rowFocus)
                                ?: AppThemePreferences.setStyle(context, AppThemeStyle.MATERIAL_YOU)
                        },
                    )
                    ThemeOptionRow(
                        title = stringResource(R.string.theme_miuix),
                        selected = themeStyle == AppThemeStyle.MIUIX,
                        onRequest = { rowFocus ->
                            requester?.requestTheme(AppThemeStyle.MIUIX, rowFocus)
                                ?: AppThemePreferences.setStyle(context, AppThemeStyle.MIUIX)
                        },
                    )
                    // Both effect rows share the host's capability answer; the
                    // checked state always shows the saved preference itself,
                    // never "preference && supported" — an unsupported device
                    // keeps the value and falls back to native rendering.
                    val effectsSupported = isEffectRenderingSupported()
                    AppSettingsRow(
                        title = stringResource(R.string.more_blur),
                        summary = if (effectsSupported) {
                            stringResource(R.string.more_blur_summary)
                        } else {
                            stringResource(R.string.visual_effect_unavailable)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = moreBlur,
                                enabled = effectsSupported,
                                role = Role.Switch,
                                onValueChange = { enabled ->
                                    requester?.requestMoreBlur(enabled)
                                        ?: AppThemePreferences.setMoreBlur(context, enabled)
                                },
                            ),
                        onClick = null,
                        trailing = {
                            AppSwitch(
                                checked = moreBlur,
                                onCheckedChange = null,
                                enabled = effectsSupported,
                            )
                        },
                    )
                    AppSettingsRow(
                        title = stringResource(R.string.theme_liquid_glass_navigation_bar),
                        summary = if (effectsSupported) {
                            stringResource(R.string.theme_liquid_glass_navigation_bar_summary)
                        } else {
                            stringResource(R.string.visual_effect_unavailable)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = glassNavigationBar,
                                enabled = effectsSupported,
                                role = Role.Switch,
                                onValueChange = { enabled ->
                                    requester?.requestGlass(enabled)
                                        ?: AppThemePreferences.setLiquidGlassNavigationBar(context, enabled)
                                },
                            ),
                        onClick = null,
                        trailing = {
                            AppSwitch(
                                checked = glassNavigationBar,
                                onCheckedChange = null,
                                enabled = effectsSupported,
                            )
                        },
                    )
                }
            }

            // Settings block → informational block.
            item(key = "divider_settings") {
                AboutSectionDivider()
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
                        FeaturePill(stringResource(R.string.about_feature_system_languages))
                        FeaturePill(stringResource(R.string.about_feature_batch))
                        FeaturePill(stringResource(R.string.about_feature_filters))
                        FeaturePill(stringResource(R.string.about_feature_quick_settings))
                        FeaturePill(stringResource(R.string.about_feature_adaptive))
                    }
                    // Round-8 034: platform conditions and usage boundaries close
                    // the features section as plain body text — no separate
                    // "usage notes" section anymore, no fact dropped.
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
                    AboutBody(stringResource(R.string.about_credits))
                }
            }
        }
    }
}

@Composable
private fun AboutSectionDivider() {
    AppDivider(
        modifier = Modifier
            .readableContentWidth()
            .padding(horizontal = AppSpacing.lg),
    )
}

@Composable
private fun AboutSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    // Continuous section (round-7 031): a titled plain column on the page
    // background — no card shell; the heading weight and quiet colour carry the
    // hierarchy, and each child keeps its own 16dp foreground inset.
    Column(modifier = modifier) {
        AppText(
            text = title,
            style = AppUiTheme.textStyles.itemTitle,
            color = AppUiTheme.palette.muted,
            modifier = Modifier
                .padding(
                    start = AppSpacing.lg,
                    top = AppSpacing.lg,
                    end = AppSpacing.lg,
                    bottom = AppSpacing.md,
                )
                .semantics { heading() },
        )
        content()
    }
}

@Composable
private fun AboutBody(text: String) {
    AppText(
        text = text,
        style = AppUiTheme.textStyles.body,
        color = AppUiTheme.palette.muted,
        modifier = Modifier.padding(
            horizontal = AppSpacing.lg,
            vertical = AppSpacing.sm,
        ),
    )
}

@Composable
private fun FeaturePill(title: String) {
    // QuietBadge role (020): a short non-interactive feature label on the quiet
    // container — no selection colour, no large fill.
    AppSurface(
        shape = RoundedCornerShape(AppComponentDefaults.rowCornerRadius),
        color = AppUiTheme.palette.quietContainer,
        contentColor = AppUiTheme.palette.quietContent,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = AppSpacing.md,
                vertical = AppSpacing.sm,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            // No miuix check-circle glyph; the project vector is drawn by the
            // backend icon control (asset exception).
            AppVectorIcon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(AppSpacing.lg),
            )
            AppText(
                text = title,
                style = AppUiTheme.textStyles.label,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun ThemeOptionRow(
    title: String,
    selected: Boolean,
    onRequest: (FocusRequester) -> Unit,
) {
    // The row's own focus handle travels with the request so the transition
    // gate can restore keyboard focus here after the theme fades back in.
    val rowFocus = remember { FocusRequester() }
    AppSettingsRow(
        title = title,
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(rowFocus)
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = { onRequest(rowFocus) },
            ),
        onClick = null,
        trailing = {
            AppRadioButton(
                selected = selected,
                onClick = null,
            )
        },
    )
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
