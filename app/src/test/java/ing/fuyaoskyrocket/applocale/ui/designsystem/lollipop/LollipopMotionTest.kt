package ing.fuyaoskyrocket.applocale.ui.designsystem.lollipop

import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.ui.geometry.Offset
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.LegacyDialogTransition
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class LollipopMotionTest {
    @Test
    fun releasingDuringEnterFinishesTheWaveWithoutAStuckPressedLayer() = runTest {
        val clock = object : MonotonicFrameClock {
            override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R {
                delay(16); return onFrame(testScheduler.currentTime * 1_000_000)
            }
        }
        val ripple = LollipopRippleState(Offset(4f, 10f))
        val enter = launch(clock) { ripple.enter(120f, 1f) }
        advanceTimeBy(150); runCurrent()
        assertTrue(ripple.gravity.value > 0f && ripple.gravity.value < 1f)
        enter.cancelAndJoin()
        val from = ripple.gravity.value
        val exit = launch(clock) { ripple.exit(120f, 1f) }
        runCurrent()
        assertEquals(from, ripple.gravity.value, .001f)
        advanceUntilIdle(); exit.join()
        assertEquals(1f, ripple.gravity.value, .001f)
        assertEquals(0f, ripple.opacity.value, .001f)
    }

    @Test
    fun quickTapBeforeTheDelayStillExitsAndDoesNotMutateTheNextRipple() = runTest {
        val clock = object : MonotonicFrameClock {
            override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R {
                delay(16); return onFrame(testScheduler.currentTime * 1_000_000)
            }
        }
        val first = LollipopRippleState(Offset.Zero)
        val next = LollipopRippleState(Offset(12f, 12f))
        val enter = launch(clock) { first.enter(60f, 2f) }
        advanceTimeBy(40); runCurrent()
        assertEquals(0f, first.gravity.value, .001f)
        enter.cancelAndJoin()
        launch(clock) { first.exit(60f, 2f) }
        advanceUntilIdle()
        assertEquals(0f, first.opacity.value, .001f)
        assertEquals(1f, next.opacity.value, .001f)
        assertEquals(0f, next.gravity.value, .001f)
    }

    @Test
    fun material2014WindowDoesNotAcquireTheEarlierScaleAnimation() = runTest {
        val dialog = LegacyDialogTransition(LollipopDialogMotion)
        assertEquals(1f, dialog.scale.value)
        dialog.show(snap = true)
        dialog.hide(snap = true)
        assertFalse(dialog.mounted)
        assertEquals(1f, dialog.scale.value)
    }
}
