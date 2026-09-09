package ing.fuyaoskyrocket.applocale.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import ing.fuyaoskyrocket.applocale.ui.designsystem.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import ing.fuyaoskyrocket.applocale.ui.screen.pageHiltViewModel as hiltViewModel
import ing.fuyaoskyrocket.applocale.ui.screen.collectPageUiState
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.appinfo.AppInfoScreen
import androidx.compose.runtime.key
import ing.fuyaoskyrocket.applocale.ui.appinfo.AppInfoViewModel
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppIcon
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppListDetailLayout
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
    selectedApp: String?,
    onSelectApp: (String) -> Unit,
    onCloseApp: () -> Unit,
    mainListState: androidx.compose.foundation.lazy.LazyListState,
    tabScrollCoordinator: ing.fuyaoskyrocket.applocale.ui.screen.TabScrollCoordinator,
    mainViewModel: MainViewModel = hiltViewModel(),
    detailViewModel: AppInfoViewModel = hiltViewModel(),
) {
    val mainUiState by mainViewModel.uiState.collectPageUiState()

    AppListDetailLayout(contextual = mainUiState.isSelectionMode, list = {
            MainScreen(
                viewModel = mainViewModel,
                navigateToAppScreen = onSelectApp,
                listState = mainListState,
                tabScrollCoordinator = tabScrollCoordinator,
                navigateToSystemLanguages = navigateToSystemLanguages,
                navigateToConfigurations = navigateToConfigurations,
                navigateToAbout = navigateToAbout,
                hasGrantedShizukuPermission = hasGrantedShizukuPermission,
                onRequestShizukuPermission = onRequestShizukuPermission,
                onOpenShizuku = onOpenShizuku,
                showBottomNavigation = false,
            )
    }, detail = {
            val appId = selectedApp
            if (appId == null) {
                AppScaffold(
                    topLevelNavigation = true,
                    topBar = { AppTopAppBar(title = stringResource(R.string.app_language)) },
                    containerColor = AppUiTheme.palette.background,
                ) { innerPadding ->
                    Box(Modifier.fillMaxSize().padding(innerPadding)) { EmptyDetailPane() }
                }
            } else {
                key(appId) {
                    AppInfoScreen(
                        appId = appId,
                        navigateBack = onCloseApp,
                        viewModel = detailViewModel,
                        backEnabled = !mainUiState.isSelectionMode && !mainUiState.isSearchActive,
                        interceptNavigationBack = true,
                    )
                }
            }
    })
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
