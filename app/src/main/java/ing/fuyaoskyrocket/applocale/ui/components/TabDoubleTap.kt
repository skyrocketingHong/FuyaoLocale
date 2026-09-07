package ing.fuyaoskyrocket.applocale.ui.components

import android.os.SystemClock
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalViewConfiguration
import kotlin.math.hypot

/**
 * Pure double-tap pairing for navigation tabs (round-8 035).
 *
 * The detector only pairs two taps on the SAME target inside the system
 * double-tap window; targets are compared by identity so switching tabs never
 * fires. A completed pair consumes the candidate, so a triple tap leaves the
 * third press as a fresh candidate instead of firing twice. The clock is the
 * monotonic uptime — never wall time.
 */
class TabDoubleTapDetector(
    private val minIntervalMillis: Long,
    private val timeoutMillis: Long,
    private val uptimeMillis: () -> Long,
) {
    private var pendingTarget: Any? = null
    private var pendingUptimeMillis = 0L

    val hasPendingTap: Boolean
        get() = pendingTarget != null

    /** Records one completed touch tap; true when it closes a double tap. */
    fun recordTap(target: Any): Boolean {
        val now = uptimeMillis()
        val interval = now - pendingUptimeMillis
        return if (target === pendingTarget &&
            interval >= minIntervalMillis &&
            interval <= timeoutMillis
        ) {
            pendingTarget = null
            true
        } else {
            pendingTarget = target
            pendingUptimeMillis = now
            false
        }
    }

    /** Drops any unpaired candidate (gate closing, focus loss, tab change). */
    fun clear() {
        pendingTarget = null
    }
}

@Composable
fun rememberTabDoubleTapDetector(): TabDoubleTapDetector {
    // The compose ViewConfiguration carries the platform's real double-tap
    // window (min interval and timeout), so no hardcoded timing constants.
    val viewConfiguration = LocalViewConfiguration.current
    return remember(viewConfiguration) {
        TabDoubleTapDetector(
            minIntervalMillis = viewConfiguration.doubleTapMinTimeMillis,
            timeoutMillis = viewConfiguration.doubleTapTimeoutMillis,
            uptimeMillis = SystemClock::uptimeMillis,
        )
    }
}

/**
 * Observes real touch taps on a native tab WITHOUT consuming or competing with
 * its own click handling (round-8 035): events are read on the Initial pass,
 * nothing is consumed, and an up consumed by the native control still counts.
 *
 * A tap is a complete single-pointer press whose MAXIMUM displacement stayed
 * within touch slop (never the net movement) and whose duration stayed under
 * the system long-press timeout; multi-pointer gestures and cancellations are
 * not taps. The pointerInput key stays [Unit] so a navigation-triggered
 * recomposition cannot drop a pairing mid-gesture; the latest target, gate and
 * callback arrive through [rememberUpdatedState].
 */
@Composable
fun Modifier.tabTouchTapObserver(
    target: Any,
    enabled: () -> Boolean,
    onTap: (Any) -> Unit,
): Modifier {
    val latestTarget by rememberUpdatedState(target)
    val latestEnabled by rememberUpdatedState(enabled)
    val latestOnTap by rememberUpdatedState(onTap)
    return pointerInput(Unit) {
        awaitEachGesture {
            val down = awaitFirstDown(
                requireUnconsumed = false,
                pass = PointerEventPass.Initial,
            ) ?: return@awaitEachGesture
            val downPosition = down.position
            var maxDistance = 0f
            var multiPointer = false
            var cancelled = false
            var released = false
            var upTimeMillis = 0L
            while (!released) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                if (event.changes.count { it.pressed } > 1) multiPointer = true
                val tracked = event.changes.firstOrNull { change -> change.id == down.id }
                if (tracked == null) {
                    // The tracked pointer vanished without a normal up — the
                    // stream was cancelled, so this press is not a tap.
                    cancelled = true
                    break
                }
                maxDistance = maxOf(
                    maxDistance,
                    hypot(
                        tracked.position.x - downPosition.x,
                        tracked.position.y - downPosition.y,
                    ),
                )
                if (tracked.changedToUpIgnoreConsumed()) {
                    upTimeMillis = tracked.uptimeMillis
                    released = true
                }
            }
            val isTap = !cancelled &&
                !multiPointer &&
                maxDistance <= viewConfiguration.touchSlop &&
                upTimeMillis - down.uptimeMillis <= viewConfiguration.longPressTimeoutMillis
            if (isTap && latestEnabled()) {
                latestOnTap(latestTarget)
            }
        }
    }
}
