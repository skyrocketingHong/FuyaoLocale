package ing.fuyaoskyrocket.applocale.ui.screen.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.components.AppNavigationDestination
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.LocalAppearanceFailures
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppScaffold
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSnackbarHost
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppTopAppBar
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.rememberAppSnackbarHostState
import ing.fuyaoskyrocket.applocale.ui.designsystem.listBottomReserve
import ing.fuyaoskyrocket.applocale.ui.designsystem.readableContentWidth
import ing.fuyaoskyrocket.applocale.ui.screen.TabScrollCoordinator
import ing.fuyaoskyrocket.applocale.ui.screen.TabScrollToTopConsumer
import ing.fuyaoskyrocket.applocale.ui.screen.tabScrollToTopKeyAction

/** Preferences own their scroll position and appearance failure feedback. */
@Composable
fun SettingsScreen(tabScrollCoordinator: TabScrollCoordinator? = null) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val snackbarHostState = rememberAppSnackbarHostState()
    val appearanceFailures = LocalAppearanceFailures.current
    LaunchedEffect(appearanceFailures) {
        appearanceFailures?.collect {
            snackbarHostState.showSnackbar(context.getString(R.string.appearance_change_failed))
        }
    }
    if (tabScrollCoordinator != null) {
        TabScrollToTopConsumer(
            coordinator = tabScrollCoordinator,
            destination = AppNavigationDestination.Settings,
            listState = listState,
        )
    }
    AppScaffold(
        topLevelNavigation = true,
        topBar = { AppTopAppBar(title = stringResource(R.string.settings_category)) },
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = AppUiTheme.palette.background,
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(innerPadding)
                .then(
                    if (tabScrollCoordinator != null) {
                        Modifier.tabScrollToTopKeyAction(
                            coordinator = tabScrollCoordinator,
                            destination = AppNavigationDestination.Settings,
                        )
                    } else {
                        Modifier
                    },
                ),
            contentPadding = PaddingValues(
                top = ing.fuyaoskyrocket.applocale.ui.designsystem.component.appInfoTopInset(innerPadding.calculateTopPadding()),
                bottom = listBottomReserve(innerPadding.calculateBottomPadding()),
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item(key = "settings") {
                Column(Modifier.readableContentWidth()) { SettingsSection() }
            }
        }
    }
}
