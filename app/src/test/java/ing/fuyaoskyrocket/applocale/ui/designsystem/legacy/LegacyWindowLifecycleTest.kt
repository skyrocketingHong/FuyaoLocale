package ing.fuyaoskyrocket.applocale.ui.designsystem.legacy

import androidx.compose.animation.core.LinearEasing
import androidx.compose.runtime.MonotonicFrameClock
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class LegacyWindowLifecycleTest {
    private val motion = LegacyDialogMotion(150, 150, LinearEasing, LinearEasing)

    @Test
    fun reducedMotionStillMountsClosesAndConsumesOneCompletedExit() = runTest {
        val transition = LegacyDialogTransition(motion)
        assertFalse(transition.mounted)
        assertFalse(transition.wasShown)
        transition.show(snap = true)
        assertTrue(transition.mounted)
        assertEquals(1f, transition.alpha.value)
        transition.hide(snap = true)
        assertFalse(transition.mounted)
        assertTrue(transition.wasShown)
        transition.consumeWasShown()
        assertFalse(transition.wasShown)
    }

    @Test
    fun cancelledExitKeepsTheWindowForReentry() = runTest {
        val frames = Channel<Long>()
        val clock = object : MonotonicFrameClock {
            override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R = onFrame(frames.receive())
        }
        val transition = LegacyDialogTransition(motion)
        transition.show(snap = true)
        val exit = launch(clock) { transition.hide(snap = false) }
        runCurrent()
        assertTrue(exit.isActive)
        exit.cancelAndJoin()
        assertTrue(transition.mounted)
        transition.show(snap = true)
        assertTrue(transition.wasShown)
        assertEquals(1f, transition.scale.value)
        assertEquals(1f, transition.alpha.value)
    }

    @Test
    fun releasingTheMessageHostUnblocksEveryPendingCallerAndQueueCanBeReused() = runTest {
        val queue = LegacyMessageQueue()
        val first = async { queue.show("first") }
        val second = async { queue.show("second") }
        runCurrent()
        assertFalse(first.isCompleted)
        queue.flush()
        runCurrent()
        assertTrue(first.isCompleted)
        assertTrue(second.isCompleted)
        val next = async { queue.show("next") }
        runCurrent()
        val entry = queue.channel.receive()
        assertEquals("next", entry.message)
        entry.done.complete(Unit)
        next.await()
    }
}
