package ing.fuyaoskyrocket.applocale.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.appinfo.AppInfoScreen
import androidx.compose.runtime.key
import ing.fuyaoskyrocket.applocale.ui.appinfo.AppInfoViewModel
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppComponentDefaults
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppLayout
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppIcon
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppPanel
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppScaffold
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppText
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppTopAppBar

/**
 * Canonical list-detail layout for expanded window widths.
 * Left: app list (uses [MainScreen] with internal selection state).
 * Right: app detail content or empty state.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LargeHomeScreen(
    navigateToSystemLanguages: () -> Unit,
    navigateToConfigurations: () -> Unit,
    navigateToAbout: () -> Unit,
    hasGrantedShizukuPermission: Boolean,
    onRequestShizukuPermission: () -> Unit,
    onOpenShizuku: () -> Unit,
    mainViewModel: MainViewModel = hiltViewModel(),
    detailViewModel: AppInfoViewModel = hiltViewModel(),
) {
    var selectedApp by rememberSaveable { mutableStateOf<String?>(null) }
    val mainUiState by mainViewModel.uiState.collectAsStateWithLifecycle()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = AppLayout.contentFrameMargin,
                end = AppLayout.contentFrameMargin,
                bottom = AppSpacing.screenExpanded,
            ),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.paneGap),
    ) {
        // A stable-width list pane preserves scanning rhythm as the window grows.
        AppPanel(
            modifier = Modifier
                .width(AppLayout.listPaneWidth)
                .fillMaxHeight(),
            cornerRadius = AppComponentDefaults.sectionCornerRadius,
            color = AppUiTheme.palette.secondarySurface,
        ) {
            MainScreen(
                viewModel = mainViewModel,
                navigateToAppScreen = { selectedApp = it },
                navigateToSystemLanguages = navigateToSystemLanguages,
                navigateToConfigurations = navigateToConfigurations,
                navigateToAbout = navigateToAbout,
                hasGrantedShizukuPermission = hasGrantedShizukuPermission,
                onRequestShizukuPermission = onRequestShizukuPermission,
                onOpenShizuku = onOpenShizuku,
                showBottomNavigation = false,
            )
        }

        // Tonal separation replaces the old one-pixel divider and scales better
        // across tablets, foldables and desktop-style windows.
        AppPanel(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            cornerRadius = AppComponentDefaults.sectionCornerRadius,
            color = AppUiTheme.palette.surface,
        ) {
            val appId = selectedApp
            if (appId == null) {
                AppScaffold(
                    topBar = { AppTopAppBar(title = stringResource(R.string.app_language)) },
                    containerColor = AppUiTheme.palette.background,
                ) { innerPadding ->
                    Box(Modifier.fillMaxSize().padding(innerPadding)) { EmptyDetailPane() }
                }
            } else {
                key(appId) {
                    AppInfoScreen(
                        appId = appId,
                        navigateBack = { selectedApp = null },
                        viewModel = detailViewModel,
                        backEnabled = !mainUiState.isSelectionMode && !mainUiState.isSearchActive,
                        interceptNavigationBack = true,
                    )
                }
            }
        }
    }

}

@Composable
private fun EmptyDetailPane() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // No miuix glyph for the touch-to-select metaphor; the project vector
            // is drawn by the backend icon control (asset exception, see 012 record).
            AppIcon(
                imageVector = Icons.Outlined.TouchApp,
                contentDescription = null,
                tint = AppUiTheme.palette.muted,
                modifier = Modifier.padding(bottom = AppSpacing.lg),
            )
            AppText(
                text = stringResource(R.string.select_an_app),
                style = AppUiTheme.textStyles.itemTitle,
                textAlign = TextAlign.Center,
            )
            AppText(
                text = stringResource(R.string.select_an_app_description),
                style = AppUiTheme.textStyles.body,
                color = AppUiTheme.palette.muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(
                    horizontal = AppSpacing.xl,
                    vertical = AppSpacing.sm,
                ),
            )
        }
    }
}
