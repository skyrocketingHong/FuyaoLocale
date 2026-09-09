package ing.fuyaoskyrocket.applocale.ui.appinfo

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.ui.languagepicker.rememberLanguagePickerListStates
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import ing.fuyaoskyrocket.applocale.ui.screen.pageHiltViewModel as hiltViewModel
import ing.fuyaoskyrocket.applocale.ui.screen.collectPageUiState
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppCircularProgressIndicator
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppScaffold
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSnackbarHost
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.rememberAppSnackbarHostState
import ing.fuyaoskyrocket.applocale.ui.languagepicker.LocalePickerAction
import ing.fuyaoskyrocket.applocale.ui.languagepicker.LocalePickerEvent
import kotlinx.coroutines.flow.collect

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppInfoScreen(
    appId: String,
    navigateBack: () -> Unit,
    viewModel: AppInfoViewModel = hiltViewModel(),
    backEnabled: Boolean = true,
    interceptNavigationBack: Boolean = false,
) {
    val appInfoState by viewModel.uiState.collectPageUiState()
    val pickerState by viewModel.pickerState.collectPageUiState()
    val ctx = LocalContext.current
    val displayLocaleTag = LocalConfiguration.current.locales[0].toLanguageTag()
    val snackbarHostState = rememberAppSnackbarHostState()

    LaunchedEffect(appId, displayLocaleTag) {
        viewModel.initFromPackage(appId)
    }

    // Snackbar events (pin/unpin messages)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
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
    }

    var searchExpanded by rememberSaveable(appId) { mutableStateOf(false) }
    val searchActive = searchExpanded || pickerState.query.isNotBlank()
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val listStates = rememberLanguagePickerListStates(pickerState, contentKey = appId)
    val activeListState = listStates.forState(pickerState)
    var topBarHeight by remember { mutableIntStateOf(0) }
    // Raw distance of the identity header's bottom edge below the list top:
    // null while the bar is unmeasured or the list is empty (loading keeps the
    // page title), negative infinity once the header left the window entirely.
    val headerBottom by remember(activeListState) {
        derivedStateOf {
            val info = activeListState.layoutInfo
            val header = info.visibleItemsInfo.firstOrNull { it.key == "header" }
            when {
                topBarHeight <= 0 || info.totalItemsCount == 0 -> null
                header == null -> Float.NEGATIVE_INFINITY
                else -> (header.offset + header.size - info.viewportStartOffset).toFloat()
            }
        }
    }
    // 4dp hysteresis (in px) so the compact identity doesn't flicker when the
    // header bottom hovers right at the bar edge. The latch resets per app.
    val hysteresisPx = with(LocalDensity.current) { 4.dp.toPx() }
    var collapsed by remember(appId) { mutableStateOf(false) }
    LaunchedEffect(headerBottom, topBarHeight, hysteresisPx) {
        val bottom = headerBottom ?: return@LaunchedEffect
        collapsed = if (collapsed) {
            bottom <= topBarHeight + hysteresisPx
        } else {
            bottom <= topBarHeight
        }
    }
    val closeSearch = {
        searchExpanded = false
        viewModel.onPickerAction(LocalePickerAction.QueryChanged(""))
        keyboard?.hide()
        focusManager.clearFocus()
    }
    val handleTopBarBack: () -> Unit = {
        when {
            searchActive -> closeSearch()
            pickerState.isInGroup -> viewModel.onPickerAction(LocalePickerAction.BackToGroups)
            else -> navigateBack()
        }
    }
    // Group back is owned by the seekable child transition below. Search/IME
    // close independently; phone route back stays with the outer NavHost.
    val imeVisible = WindowInsets.isImeVisible
    BackHandler(
        enabled = backEnabled && !imeVisible &&
            (searchActive || (interceptNavigationBack && !pickerState.isInGroup)),
        onBack = handleTopBarBack,
    )
    AppScaffold(
        topBar = {
            AppDetailTopBar(
                state = appInfoState,
                iconLoader = viewModel.appIconLoader,
                // Loading or unmeasured states never show a stale app name.
                collapsed = collapsed && !appInfoState.isLoading,
                searchExpanded = searchExpanded,
                query = pickerState.query,
                onBack = handleTopBarBack,
                onOpenSearch = { searchExpanded = true },
                onCloseSearch = closeSearch,
                onQueryChange = { viewModel.onPickerAction(LocalePickerAction.QueryChanged(it)) },
                onOpen = {
                    viewModel.getOpenIntent()
                        ?.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                        ?.let { ctx.startActivity(it) }
                },
                onForceStop = { viewModel.forceStop() },
                onSettings = {
                    ctx.startActivity(viewModel.getSettingsIntent().apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                },
                modifier = Modifier.onSizeChanged { topBarHeight = it.height },
            )
        },
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = AppUiTheme.palette.background,
    ) { innerPadding ->
        if (appInfoState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                AppCircularProgressIndicator()
            }
            return@AppScaffold
        }

        AppDetailContent(
            appInfoState = appInfoState,
            pickerState = pickerState,
            iconLoader = viewModel.appIconLoader,
            listStates = listStates,
            backEnabled = backEnabled && !searchActive,
            onPickerAction = viewModel::onPickerAction,
            onResetLocale = { viewModel.onResetLocale() },
            onSelectLocale = { option ->
                // Business write first, then the search presentation collapses
                // (query, focus, IME), and only then the return-to-directory move.
                viewModel.onSelectLocale(option)
                if (searchActive) closeSearch()
                viewModel.onPickerAction(
                    LocalePickerAction.BackToGroups
                )
            },
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding,
        )
    }
}
