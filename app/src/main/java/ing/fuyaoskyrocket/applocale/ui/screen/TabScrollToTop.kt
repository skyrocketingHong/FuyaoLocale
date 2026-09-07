package ing.fuyaoskyrocket.applocale.ui.screen

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import ing.fuyaoskyrocket.applocale.ui.components.AppNavigationDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import java.util.concurrent.atomic.AtomicLong

/**
 * One pending "scroll this top-level page back to its top" request issued by a
 * navigation tab double tap (round-8 035). [id] makes repeated requests to the
 * same destination distinguishable so a late acknowledgement cannot clear a
 * newer request.
 */
data class TabScrollRequest(
    val id: Long,
    val destination: AppNavigationDestination,
)

/**
 * Page-level coordinator owned by [Navigation] — not a singleton, not a
 * business ViewModel. At most one request is retained at a time; a page that
 * has not mounted yet still finds it through the StateFlow replay, while a
 * route change away from the target destination drops it so a later return to
 * that page never scrolls unexpectedly.
 */
class TabScrollCoordinator {
    internal val pendingRequest = MutableStateFlow<TabScrollRequest?>(null)
    private val nextId = AtomicLong(0L)

    fun request(destination: AppNavigationDestination) {
        pendingRequest.value = TabScrollRequest(
            id = nextId.incrementAndGet(),
            destination = destination,
        )
    }

    internal fun acknowledge(request: TabScrollRequest) {
        if (pendingRequest.value?.id == request.id) {
            pendingRequest.value = null
        }
    }

    /** Keeps a pending request only while the visible top-level route still matches it. */
    fun onTopLevelRouteChanged(destination: AppNavigationDestination) {
        val pending = pendingRequest.value ?: return
        if (pending.destination != destination) {
            pendingRequest.value = null
        }
    }

    /** Entering a detail route ends any pending tab request. */
    fun onLeftTopLevelRoutes() {
        pendingRequest.value = null
    }
}

@Composable
fun rememberTabScrollCoordinator(): TabScrollCoordinator = remember { TabScrollCoordinator() }

/**
 * The mounted page's consumer: filters requests for its own destination, waits
 * for [isReady] (the system-language page holds the scroll while a reorder
 * drag is in flight), then scrolls to the very top. Scrolling only — never a
 * refresh, never clearing search, filters, selection or drafts. An empty list
 * acknowledges immediately; a newer request cancels the running animation.
 */
@Composable
fun TabScrollToTopConsumer(
    coordinator: TabScrollCoordinator,
    destination: AppNavigationDestination,
    listState: LazyListState,
    isReady: () -> Boolean = { true },
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(coordinator, destination, listState) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            coordinator.pendingRequest.collectLatest { request ->
                if (request == null || request.destination != destination) return@collectLatest
                snapshotFlow { isReady() }.first { it }
                try {
                    if (listState.layoutInfo.totalItemsCount > 0) {
                        listState.animateScrollToItem(0, 0)
                    }
                } finally {
                    coordinator.acknowledge(request)
                }
            }
        }
    }
}

/**
 * Ctrl+Home (or Meta+Home) as the keyboard equivalent of the tab double tap
 * (round-8 035). Attached to the page content, it only consumes that one
 * chord on the initial key-down; every other key falls through untouched.
 */
fun Modifier.tabScrollToTopKeyAction(
    coordinator: TabScrollCoordinator,
    destination: AppNavigationDestination,
): Modifier = onPreviewKeyEvent { event ->
    val invokesScrollToTop = event.type == KeyEventType.KeyDown &&
        (event.isCtrlPressed || event.isMetaPressed) &&
        event.key == Key.MoveHome
    if (invokesScrollToTop) {
        coordinator.request(destination)
        true
    } else {
        false
    }
}
