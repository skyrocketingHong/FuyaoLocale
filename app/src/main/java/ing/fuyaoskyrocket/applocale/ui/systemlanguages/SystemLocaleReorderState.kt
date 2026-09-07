package ing.fuyaoskyrocket.applocale.ui.systemlanguages

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.Density
import ing.fuyaoskyrocket.applocale.model.LocaleOption
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppMotion
import kotlin.math.abs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/** Lifecycle of the drag owned by the stable host Box; only the overlay translates while non-idle. */
enum class SystemLocaleReorderPhase { Idle, Dragging, Settling }

/**
 * One in-flight [SystemLocaleReorderState.onMove] dispatch awaiting confirmation:
 * the draft must show [expectedSourceIndex] and the row geometry must have changed
 * past [geometryRevision] before the next swap is judged, so a stale layout frame
 * cannot flip a row back and forth.
 */
internal data class SystemLocalePendingMove(
    val sourceTag: String,
    val targetTag: String,
    val expectedSourceIndex: Int,
    val geometryRevision: Long,
)

/**
 * Drag presentation state for the system-languages page. The locales draft and
 * onMove stay in the ViewModel; this class only tracks the pointer, the overlay
 * geometry, the visible-row map in host coordinates, and the settle/scroll jobs
 * — including the single edge auto-scroll job driven by the measured viewport.
 * It must stay composition-scoped and is not persisted anywhere.
 */
internal class SystemLocaleReorderState(
    private val scope: CoroutineScope,
    private val listState: LazyListState,
    private val localesProvider: () -> List<LocaleOption>,
    private val onMove: (String, String) -> Unit,
    private val densityProvider: () -> Density,
    private val bottomOcclusionPxProvider: () -> Float,
) {
    var phase by mutableStateOf(SystemLocaleReorderPhase.Idle)
        private set
    var generation by mutableLongStateOf(0L)
        private set
    var draggingTag by mutableStateOf<String?>(null)
        private set

    var pointerY by mutableFloatStateOf(0f)
        private set
    private var grabOffsetY by mutableFloatStateOf(0f)

    /** Horizontal pointer position in host coordinates; only feeds FAB occlusion. */
    private var pointerXValue = 0f

    var overlayTop by mutableFloatStateOf(0f)
        private set
    var overlayWidth by mutableFloatStateOf(0f)
        private set
    var overlayHeight by mutableFloatStateOf(0f)
        private set
    var overlaySnapshot by mutableStateOf<LocaleOption?>(null)
        private set

    /** Visible attached rows and their drag handles, in stable host Box coordinates. */
    var rowBounds by mutableStateOf<Map<String, Rect>>(emptyMap())
        private set
    var handleBounds by mutableStateOf<Map<String, Rect>>(emptyMap())
        private set

    var pendingMove by mutableStateOf<SystemLocalePendingMove?>(null)
        private set
    var geometryRevision by mutableLongStateOf(0L)
        private set

    var settleJob: Job? = null
        private set

    /**
     * The single edge auto-scroll job. It only runs while a drag is pinned to an
     * edge sensing zone; release, cancellation, page disposal and every new grab
     * cancel it.
     */
    var scrollJob: Job? = null

    private var hostCoordinates: LayoutCoordinates? = null
    private var viewportCoordinates: LayoutCoordinates? = null

    fun hostPositioned(coordinates: LayoutCoordinates) {
        hostCoordinates = coordinates
    }

    /** Reports the LazyColumn itself; its measured rect is the scroll-viewport base. */
    fun reportViewport(coordinates: LayoutCoordinates) {
        viewportCoordinates = coordinates
    }

    fun reportRow(tag: String, coordinates: LayoutCoordinates) {
        val host = hostCoordinates ?: return
        val bounds = host.localBoundingBoxOf(coordinates, clipBounds = false)
        if (rowBounds[tag] != bounds) {
            rowBounds = rowBounds + (tag to bounds)
            geometryRevision += 1L
        }
    }

    fun reportHandle(tag: String, coordinates: LayoutCoordinates) {
        val host = hostCoordinates ?: return
        val bounds = host.localBoundingBoxOf(coordinates, clipBounds = false)
        if (handleBounds[tag] != bounds) {
            handleBounds = handleBounds + (tag to bounds)
            geometryRevision += 1L
        }
    }

    fun removeRow(tag: String) {
        if (rowBounds.containsKey(tag) || handleBounds.containsKey(tag)) {
            rowBounds = rowBounds - tag
            handleBounds = handleBounds - tag
            geometryRevision += 1L
        }
    }

    fun removeHandle(tag: String) {
        if (handleBounds.containsKey(tag)) {
            handleBounds = handleBounds - tag
            geometryRevision += 1L
        }
    }

    fun hitTestHandle(position: Offset): String? =
        handleBounds.entries.firstOrNull { it.value.contains(position) }?.key

    /** Grabs [tag] at the long-press point; false leaves the event stream untouched. */
    fun startDrag(tag: String, position: Offset): Boolean {
        val sourceRow = rowBounds[tag] ?: return false
        settleJob?.cancel()
        scrollJob?.cancel()
        generation += 1L
        draggingTag = tag
        overlaySnapshot = localesProvider().firstOrNull { it.languageTag == tag }
        pointerXValue = position.x
        pointerY = position.y
        grabOffsetY = position.y - sourceRow.top
        overlayTop = sourceRow.top
        overlayWidth = sourceRow.width
        overlayHeight = sourceRow.height
        pendingMove = null
        phase = SystemLocaleReorderPhase.Dragging
        // The grab may already sit inside an edge sensing zone.
        ensureEdgeScroll()
        return true
    }

    fun updatePointer(position: Offset) {
        if (phase != SystemLocaleReorderPhase.Dragging) return
        pointerXValue = position.x
        pointerY = position.y
        overlayTop = position.y - grabOffsetY
        val tag = draggingTag ?: return
        if (localesProvider().none { it.languageTag == tag }) {
            // The dragged locale was removed from the draft: end the display at once.
            endDisplayImmediately(generation)
            return
        }
        maybeConfirmPendingMove()
        maybeSwap()
        ensureEdgeScroll()
    }

    fun releaseDrag() {
        if (phase != SystemLocaleReorderPhase.Dragging) return
        scrollJob?.cancel()
        phase = SystemLocaleReorderPhase.Settling
        val dragGeneration = generation
        settleJob?.cancel()
        settleJob = scope.launch { settle(dragGeneration) }
    }

    private suspend fun settle(dragGeneration: Long) {
        val tag = draggingTag ?: return
        val index = localesProvider().indexOfFirst { it.languageTag == tag }
        if (index == -1) {
            endDisplayImmediately(dragGeneration)
            return
        }
        if (rowBounds[tag] == null) {
            // The slot scrolled out: jump it back in, then wait for the real
            // geometry report rather than a fixed delay.
            listState.scrollToItem(index)
            val appeared = withTimeoutOrNull(1_000L) {
                snapshotFlow { rowBounds.containsKey(tag) }.first { it }
            }
            if (appeared != true) {
                endDisplayImmediately(dragGeneration)
                return
            }
        }
        val targetTop = rowBounds[tag]?.top ?: run {
            endDisplayImmediately(dragGeneration)
            return
        }
        val startTop = overlayTop
        if (abs(targetTop - startTop) > AppMotion.PixelThreshold) {
            val animator = Animatable(startTop)
            animator.animateTo(
                targetValue = targetTop,
                animationSpec = spring(
                    dampingRatio = AppMotion.ReturnDampingRatio,
                    stiffness = AppMotion.ReturnStiffness,
                    visibilityThreshold = AppMotion.PixelThreshold,
                ),
            ) {
                if (generation == dragGeneration) overlayTop = value
            }
        } else if (generation == dragGeneration) {
            overlayTop = targetTop
        }
        if (generation == dragGeneration) resetToIdle()
    }

    /** Ends the overlay without a settle spring; keeps the draft order as it is. */
    private fun endDisplayImmediately(currentGeneration: Long) {
        if (generation != currentGeneration) return
        generation += 1L
        resetToIdle()
    }

    private fun resetToIdle() {
        phase = SystemLocaleReorderPhase.Idle
        draggingTag = null
        overlaySnapshot = null
        pendingMove = null
        scrollJob?.cancel()
    }

    private fun maybeConfirmPendingMove() {
        val pending = pendingMove ?: return
        val sourceIndex = localesProvider().indexOfFirst { it.languageTag == pending.sourceTag }
        if (sourceIndex == pending.expectedSourceIndex && geometryRevision > pending.geometryRevision) {
            pendingMove = null
        }
    }

    /**
     * At most one swap per confirmed layout: the dragged center must cross the
     * measured center of the currently visible neighbour, never a fixed row pitch.
     */
    private fun maybeSwap() {
        if (pendingMove != null) return
        val tag = draggingTag ?: return
        val locales = localesProvider()
        val sourceIndex = locales.indexOfFirst { it.languageTag == tag }
        if (sourceIndex == -1) return
        val draggedCenter = overlayTop + overlayHeight / 2f
        if (sourceIndex < locales.lastIndex) {
            val nextTag = locales[sourceIndex + 1].languageTag
            val nextRow = rowBounds[nextTag]
            if (nextRow != null && draggedCenter > nextRow.center.y) {
                dispatchMove(sourceTag = tag, targetTag = nextTag, expectedSourceIndex = sourceIndex + 1)
                return
            }
        }
        if (sourceIndex > 0) {
            val previousTag = locales[sourceIndex - 1].languageTag
            val previousRow = rowBounds[previousTag]
            if (previousRow != null && draggedCenter < previousRow.center.y) {
                dispatchMove(sourceTag = tag, targetTag = previousTag, expectedSourceIndex = sourceIndex - 1)
            }
        }
    }

    private fun dispatchMove(sourceTag: String, targetTag: String, expectedSourceIndex: Int) {
        pendingMove = SystemLocalePendingMove(
            sourceTag = sourceTag,
            targetTag = targetTag,
            expectedSourceIndex = expectedSourceIndex,
            geometryRevision = geometryRevision,
        )
        onMove(sourceTag, targetTag)
    }

    /**
     * The usable vertical band in host coordinates for edge auto-scroll: the
     * LazyColumn's measured rect minus the floating glass bar footprint. A
     * native bottom slot is already outside the measured rect and is not
     * subtracted again (the old FAB occluder is gone with the FAB itself).
     */
    private fun availableViewport(): Rect? {
        val host = hostCoordinates ?: return null
        val viewportCoords = viewportCoordinates?.takeIf { it.isAttached } ?: return null
        val viewport = host.localBoundingBoxOf(viewportCoords, clipBounds = false)
        val bottom = viewport.bottom - bottomOcclusionPxProvider()
        return Rect(viewport.left, viewport.top, viewport.right, bottom)
    }

    /**
     * Edge sensing-zone velocity in px/s: full [AppMotion.ReorderMaxSpeed] at the
     * viewport border, easing to zero at the inner edge of a zone that is at most
     * [AppMotion.ReorderEdgeZone] (and never more than half the usable band).
     */
    private fun edgeVelocityPx(viewport: Rect): Float {
        val bandHeight = viewport.bottom - viewport.top
        if (bandHeight <= 0f) return 0f
        val density = densityProvider()
        val edgePx = with(density) { minOf(AppMotion.ReorderEdgeZone.toPx(), bandHeight / 2f) }
        if (edgePx <= 0f) return 0f
        val maxSpeedPx = with(density) { AppMotion.ReorderMaxSpeed.toPx() }
        val y = pointerY
        return when {
            y < viewport.top + edgePx ->
                -maxSpeedPx * ((viewport.top + edgePx - y) / edgePx).coerceIn(0f, 1f)
            y > viewport.bottom - edgePx ->
                maxSpeedPx * ((y - (viewport.bottom - edgePx)) / edgePx).coerceIn(0f, 1f)
            else -> 0f
        }
    }

    /**
     * Runs the single edge auto-scroll job while the drag stays pinned to a
     * sensing zone. Each frame it first re-judges swaps against the geometry
     * settled by the previous frame's scroll — so rows revealed under a
     * stationary pointer still swap in — then scrolls by [LazyListState.scrollBy]
     * only; no competing animated scroll is ever started.
     */
    private fun ensureEdgeScroll() {
        if (phase != SystemLocaleReorderPhase.Dragging) return
        if (scrollJob?.isActive == true) return
        val viewport = availableViewport() ?: return
        if (edgeVelocityPx(viewport) == 0f) return
        scrollJob = scope.launch {
            var lastFrameNanos = -1L
            while (isActive && phase == SystemLocaleReorderPhase.Dragging) {
                val frameNanos = withFrameNanos { it }
                // The first frame only records time; abnormal frame gaps are
                // clamped so resuming from the background does not jump the list.
                val deltaSeconds = if (lastFrameNanos < 0L) {
                    0f
                } else {
                    ((frameNanos - lastFrameNanos) / 1_000_000_000f)
                        .coerceIn(0f, MaxFrameDeltaSeconds)
                }
                lastFrameNanos = frameNanos
                maybeConfirmPendingMove()
                maybeSwap()
                val velocity = availableViewport()?.let { edgeVelocityPx(it) } ?: 0f
                if (velocity == 0f) break
                if (deltaSeconds == 0f) continue
                val consumed = listState.scrollBy(velocity * deltaSeconds)
                if (consumed == 0f) {
                    // At the list boundary scrolling reveals no new rows, so this
                    // loop stops dispatching reorder; pointer moves still judge.
                    break
                }
            }
        }
    }

    private companion object {
        /** Longest frame gap in seconds that may feed the edge scroll speed. */
        const val MaxFrameDeltaSeconds = 0.032f
    }
}

@Composable
internal fun rememberSystemLocaleReorderState(
    scope: CoroutineScope,
    listState: LazyListState,
    locales: () -> List<LocaleOption>,
    onMove: (String, String) -> Unit,
    density: () -> Density,
    bottomOcclusionPx: () -> Float,
): SystemLocaleReorderState = remember(scope, listState) {
    SystemLocaleReorderState(scope, listState, locales, onMove, density, bottomOcclusionPx)
}

/**
 * Long-press drag detection on the stable host Box. A miss on a handle leaves the
 * event untouched so list scrolling and short-press menus keep working; once the
 * long press fires, the drag loop intercepts on the Initial pass so the LazyColumn
 * and the row buttons never see the remaining events.
 */
internal suspend fun PointerInputScope.detectSystemLocaleReorder(
    state: SystemLocaleReorderState,
) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val hitTag = state.hitTestHandle(down.position)
        if (hitTag == null) return@awaitEachGesture
        // A short press falls through to the native button menu; a consumed event
        // (list scroll took over) cancels the wait.
        val longPress = awaitLongPressOrCancellation(down.id) ?: return@awaitEachGesture
        if (state.startDrag(hitTag, longPress.position)) {
            dragUntilRelease(down.id, state)
        }
    }
}

private suspend fun AwaitPointerEventScope.dragUntilRelease(
    pointerId: PointerId,
    state: SystemLocaleReorderState,
) {
    while (true) {
        val event = awaitPointerEvent(PointerEventPass.Initial)
        val change = event.changes.firstOrNull { it.id == pointerId }
        if (change == null || !change.pressed || change.isConsumed) {
            change?.consume()
            state.releaseDrag()
            break
        }
        state.updatePointer(change.position)
        event.changes.forEach { it.consume() }
    }
}
