package ing.fuyaoskyrocket.applocale.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppLayout
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.LocalAppPaneChrome
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppScaffold
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppTopAppBar

/** Whether this page, rather than an adjacent preview, owns input and foreground work. */
val LocalTopLevelPageActive = staticCompositionLocalOf { true }
internal val LocalPagerPageScaffold = staticCompositionLocalOf<PagerPageScaffoldScope?> { null }
internal val LocalPagerStateLifecycleOwner = staticCompositionLocalOf<LifecycleOwner?> { null }

internal class PagerPageScaffoldScope(
    val page: Int,
    val chrome: PagerChromeStore,
    val padding: PaddingValues,
)

internal class PagerChromeSlots(
    val topBar: State<@Composable () -> Unit>,
    val bottomBar: State<@Composable () -> Unit>,
    val snackbar: State<@Composable () -> Unit>,
    val floatingActionButton: State<@Composable () -> Unit>,
    val contextual: State<Boolean>,
)

internal class PagerChromeStore {
    private val slots = mutableStateMapOf<Pair<Int, Int>, PagerChromeSlots>()
    fun get(page: Int, pane: Int = 0): PagerChromeSlots? = slots[page to pane]
    fun publish(page: Int, pane: Int, value: PagerChromeSlots) {
        if (slots[page to pane] !== value) slots[page to pane] = value
    }
}

/**
 * Pages keep their own state and callbacks. Only their scaffold slots move to the fixed host;
 * the HorizontalPager contains the page bodies. Each page/pane has one stable slot holder.
 */
@Composable
internal fun PagerPageScaffold(
    scope: PagerPageScaffoldScope,
    modifier: Modifier,
    topBar: @Composable () -> Unit,
    bottomBar: @Composable () -> Unit,
    snackbarHost: @Composable () -> Unit,
    floatingActionButton: @Composable () -> Unit,
    contextual: Boolean,
    content: @Composable (PaddingValues) -> Unit,
) {
    val pane = LocalAppPaneChrome.current?.pane ?: 0
    val latestTopBar = rememberUpdatedState(topBar)
    val latestBottomBar = rememberUpdatedState(bottomBar)
    val latestSnackbar = rememberUpdatedState(snackbarHost)
    val latestFab = rememberUpdatedState(floatingActionButton)
    val latestContextual = rememberUpdatedState(contextual)
    val slots = remember {
        PagerChromeSlots(latestTopBar, latestBottomBar, latestSnackbar, latestFab, latestContextual)
    }
    // Retain the last header while a long tab jump pre-composes its destination.
    // The bounded store (five pages, at most two panes) is released with the host.
    SideEffect { scope.chrome.publish(scope.page, pane, slots) }
    Box(modifier.fillMaxSize()) { content(scope.padding) }
}

@Composable
internal fun TopLevelPagerHost(
    state: PagerState,
    swipeEnabled: Boolean,
    showNavigation: Boolean,
    useWideLayout: Boolean,
    pageContent: @Composable (Int) -> Unit,
) {
    val chrome = remember { PagerChromeStore() }
    val stateLifecycleOwner = LocalLifecycleOwner.current
    val activePage = state.settledPage
    val activeSlots = chrome.get(activePage)
    AppScaffold(
        topLevelNavigation = showNavigation,
        contextual = activeSlots?.contextual?.value == true,
        containerColor = AppUiTheme.palette.background,
        topBar = { PagerHeader(chrome, activePage, useWideLayout) },
        bottomBar = { chrome.get(activePage)?.bottomBar?.value?.invoke() },
        snackbarHost = {
            PagerPaneRow(chrome, activePage, useWideLayout) { slots -> slots?.snackbar?.value?.invoke() }
        },
        floatingActionButton = { chrome.get(activePage)?.floatingActionButton?.value?.invoke() },
    ) { padding ->
        HorizontalPager(
            state = state,
            modifier = Modifier.fillMaxSize().clipToBounds(),
            key = { topLevelRoutes[it] },
            beyondViewportPageCount = 1,
            userScrollEnabled = swipeEnabled,
            overscrollEffect = ing.fuyaoskyrocket.applocale.ui.designsystem.component.rememberAppPageOverscrollEffect(),
        ) { page ->
            PagerPageLifecycle(active = page == activePage) {
                CompositionLocalProvider(
                    LocalPagerPageScaffold provides PagerPageScaffoldScope(page, chrome, padding),
                    LocalTopLevelPageActive provides (page == activePage),
                    LocalPagerStateLifecycleOwner provides stateLifecycleOwner,
                ) {
                    pageContent(page)
                }
            }
        }
    }
}

@Composable
private fun PagerHeader(chrome: PagerChromeStore, page: Int, useWideLayout: Boolean) {
    PagerPaneRow(chrome, page, useWideLayout) { PagerHeaderSlot(it, page) }
}

@Composable
private fun PagerPaneRow(
    chrome: PagerChromeStore,
    page: Int,
    useWideLayout: Boolean,
    content: @Composable (PagerChromeSlots?) -> Unit,
) {
    if (page == 0 && useWideLayout) {
        Row(
            modifier = Modifier.padding(horizontal = AppUiTheme.spacing.paneOuterInset),
            horizontalArrangement = Arrangement.spacedBy(AppUiTheme.spacing.paneGap),
        ) {
            Box(Modifier.width(AppLayout.listPaneWidth)) { content(chrome.get(page, 0)) }
            Box(Modifier.weight(1f)) { content(chrome.get(page, 1)) }
        }
    } else {
        content(chrome.get(page))
    }
}

@Composable
private fun PagerHeaderSlot(slots: PagerChromeSlots?, page: Int) {
    if (slots != null) slots.topBar.value.invoke()
    else AppTopAppBar(title = stringResource(when (page) {
        0 -> R.string.app_name
        1 -> R.string.system_languages
        2 -> R.string.configurations
        3 -> R.string.settings_category
        else -> R.string.about
    }))
}

private class PagerLifecycleOwner : LifecycleOwner {
    val registry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = registry
}

/** Adjacent pages may render previews, but cannot own back handling or foreground collectors. */
@Composable
private fun PagerPageLifecycle(active: Boolean, content: @Composable () -> Unit) {
    val parent = LocalLifecycleOwner.current
    val owner = remember(parent) { PagerLifecycleOwner() }
    val latestActive by rememberUpdatedState(active)
    fun update() {
        val parentState = parent.lifecycle.currentState
        owner.registry.currentState = pageInteractionLifecycle(parentState, latestActive)
    }
    DisposableEffect(parent, owner) {
        val observer = LifecycleEventObserver { _, _ -> update() }
        parent.lifecycle.addObserver(observer)
        update()
        onDispose {
            parent.lifecycle.removeObserver(observer)
            owner.registry.currentState = Lifecycle.State.DESTROYED
        }
    }
    SideEffect { update() }
    CompositionLocalProvider(LocalLifecycleOwner provides owner, content = content)
}

internal fun pageInteractionLifecycle(parent: Lifecycle.State, active: Boolean): Lifecycle.State = when {
    parent == Lifecycle.State.DESTROYED -> Lifecycle.State.DESTROYED
    active -> parent
    parent.isAtLeast(Lifecycle.State.CREATED) -> Lifecycle.State.CREATED
    else -> parent
}
