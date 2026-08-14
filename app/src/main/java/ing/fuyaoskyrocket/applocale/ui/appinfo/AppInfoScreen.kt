package ing.fuyaoskyrocket.applocale.ui.appinfo

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.components.AppTopAppBarTitle
import ing.fuyaoskyrocket.applocale.ui.components.predictiveBackTransform
import ing.fuyaoskyrocket.applocale.ui.components.rememberPredictiveBackMotion
import ing.fuyaoskyrocket.applocale.ui.languagepicker.LocalePickerAction
import ing.fuyaoskyrocket.applocale.ui.languagepicker.LocalePickerEvent
import kotlinx.coroutines.flow.collect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppInfoScreen(
    appId: String,
    navigateBack: () -> Unit,
    viewModel: AppInfoViewModel = hiltViewModel(),
) {
    val appInfoState by viewModel.uiState.collectAsStateWithLifecycle()
    val pickerState by viewModel.pickerState.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    val displayLocaleTag = LocalConfiguration.current.locales[0].toLanguageTag()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(appId, displayLocaleTag) {
        viewModel.initFromPackage(appId)
    }

    // Snackbar events (pin/unpin messages)
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            val msg = when (event) {
                is LocalePickerEvent.Pinned ->
                    ctx.getString(R.string.pinned_ok, event.displayName)
                is LocalePickerEvent.Unpinned ->
                    ctx.getString(R.string.unpinned, event.displayName)
            }
            snackbarHostState.showSnackbar(msg)
        }
    }

    val hasPickerBackTarget = pickerState.isInGroup || pickerState.query.isNotBlank()
    val handlePickerBack: () -> Unit = {
        when {
            pickerState.isInGroup -> {
                viewModel.onPickerAction(LocalePickerAction.BackToGroups)
            }
            pickerState.query.isNotBlank() -> {
                viewModel.onPickerAction(LocalePickerAction.QueryChanged(""))
            }
        }
    }
    val predictiveBackMotion = rememberPredictiveBackMotion(
        enabled = hasPickerBackTarget,
        onBack = handlePickerBack,
    )
    val handleTopBarBack: () -> Unit = {
        if (hasPickerBackTarget) handlePickerBack() else navigateBack()
    }

    Scaffold(
        modifier = Modifier.predictiveBackTransform(predictiveBackMotion),
        topBar = {
            TopAppBar(
                title = {
                    AppTopAppBarTitle(title = stringResource(R.string.app_language))
                },
                navigationIcon = {
                    IconButton(onClick = handleTopBarBack) {
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
        if (appInfoState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        AppDetailContent(
            appInfoState = appInfoState,
            pickerState = pickerState,
            iconLoader = viewModel.appIconLoader,
            onPickerAction = viewModel::onPickerAction,
            onResetLocale = { viewModel.onResetLocale() },
            onSelectLocale = { option ->
                viewModel.onSelectLocale(option)
                viewModel.onPickerAction(
                    LocalePickerAction.BackToGroups
                )
            },
            onOpen = {
                viewModel.getOpenIntent()
                    ?.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                    ?.let { ctx.startActivity(it) }
            },
            onForceStop = { viewModel.forceStop() },
            onSettings = {
                ctx.startActivity(
                    viewModel.getSettingsIntent()
                        .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )
    }
}
