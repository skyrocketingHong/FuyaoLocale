package ing.fuyaoskyrocket.applocale.ui.main

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.appinfo.AppDetailContent
import ing.fuyaoskyrocket.applocale.ui.appinfo.AppInfoViewModel
import ing.fuyaoskyrocket.applocale.ui.components.AppTopAppBarTitle
import ing.fuyaoskyrocket.applocale.ui.components.predictiveBackTransform
import ing.fuyaoskyrocket.applocale.ui.components.rememberPredictiveBackMotion
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppLayout
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing
import ing.fuyaoskyrocket.applocale.ui.languagepicker.LocalePickerAction

/**
 * Canonical list-detail layout for expanded window widths.
 * Left: app list (uses [MainScreen] with internal selection state).
 * Right: app detail content or empty state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LargeHomeScreen(
    navigateToConfigurations: () -> Unit,
    navigateToAbout: () -> Unit,
    hasGrantedShizukuPermission: Boolean,
    onRequestShizukuPermission: () -> Unit,
    onOpenShizuku: () -> Unit,
    mainViewModel: MainViewModel = hiltViewModel(),
    detailViewModel: AppInfoViewModel = hiltViewModel(),
) {
    var selectedApp by rememberSaveable { mutableStateOf<String?>(null) }
    val appInfoState by detailViewModel.uiState.collectAsStateWithLifecycle()
    val pickerState by detailViewModel.pickerState.collectAsStateWithLifecycle()
    val displayLocaleTag = LocalConfiguration.current.locales[0].toLanguageTag()

    LaunchedEffect(selectedApp, displayLocaleTag) {
        selectedApp?.let { detailViewModel.initFromPackage(it) }
    }

    val predictiveBackMotion = rememberPredictiveBackMotion(
        enabled = selectedApp != null,
        onBack = {
            when {
                pickerState.isInGroup -> {
                    detailViewModel.onPickerAction(LocalePickerAction.BackToGroups)
                }
                pickerState.query.isNotBlank() -> {
                    detailViewModel.onPickerAction(LocalePickerAction.QueryChanged(""))
                }
                else -> selectedApp = null
            }
        },
    )

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = AppSpacing.screenExpanded,
                end = AppSpacing.screenExpanded,
                bottom = AppSpacing.screenExpanded,
            ),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.paneGap),
    ) {
        // A stable-width list pane preserves scanning rhythm as the window grows.
        Surface(
            modifier = Modifier
                .width(AppLayout.listPaneWidth)
                .fillMaxHeight(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            MainScreen(
                viewModel = mainViewModel,
                navigateToAppScreen = { selectedApp = it },
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
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .predictiveBackTransform(predictiveBackMotion),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            AppTopAppBarTitle(
                                title = appInfoState.label.ifBlank {
                                    stringResource(R.string.app_language)
                                },
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        ),
                    )
                },
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    if (selectedApp == null) {
                        EmptyDetailPane()
                    } else if (appInfoState.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            androidx.compose.material3.CircularProgressIndicator()
                        }
                    } else {
                        val ctx = LocalContext.current
                        AppDetailContent(
                            appInfoState = appInfoState,
                            pickerState = pickerState,
                            iconLoader = detailViewModel.appIconLoader,
                            onPickerAction = detailViewModel::onPickerAction,
                            onResetLocale = { detailViewModel.onResetLocale() },
                            onSelectLocale = { option ->
                                detailViewModel.onSelectLocale(option)
                                detailViewModel.onPickerAction(
                                    LocalePickerAction.BackToGroups
                                )
                            },
                            onOpen = {
                                detailViewModel.getOpenIntent()
                                    ?.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                                    ?.let { ctx.startActivity(it) }
                            },
                            onForceStop = { detailViewModel.forceStop() },
                            onSettings = {
                                ctx.startActivity(
                                    detailViewModel.getSettingsIntent()
                                        .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                                )
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
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
            Icon(
                imageVector = Icons.Outlined.TouchApp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = AppSpacing.lg),
            )
            Text(
                text = stringResource(R.string.select_an_app),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.select_an_app_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(
                    horizontal = AppSpacing.xl,
                    vertical = AppSpacing.sm,
                ),
            )
        }
    }
}
