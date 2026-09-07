package ing.fuyaoskyrocket.applocale.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pure double-tap pairing checks for the tab bar (round-8 035-F). */
class TabDoubleTapDetectorTest {
    private object Home
    private object About

    private fun detector(clock: () -> Long) = TabDoubleTapDetector(
        minIntervalMillis = 40L,
        timeoutMillis = 300L,
        uptimeMillis = clock,
    )

    @Test
    fun twoTapsInsideWindowOnSameTargetPairOnce() {
        var now = 0L
        val detector = detector { now }
        assertFalse(detector.recordTap(Home))
        now = 100L
        assertTrue(detector.recordTap(Home))
    }

    @Test
    fun secondTapAfterTimeoutStartsANewCandidate() {
        var now = 0L
        val detector = detector { now }
        assertFalse(detector.recordTap(Home))
        now = 301L
        assertFalse("a tap past the timeout must not close the pair", detector.recordTap(Home))
        now = 350L
        assertTrue(detector.recordTap(Home))
    }

    @Test
    fun tapsFasterThanMinIntervalDoNotPair() {
        var now = 0L
        val detector = detector { now }
        assertFalse(detector.recordTap(Home))
        now = 10L
        assertFalse("below doubleTapMinTime the pair must not fire", detector.recordTap(Home))
    }

    @Test
    fun differentTargetsNeverPair() {
        var now = 0L
        val detector = detector { now }
        assertFalse(detector.recordTap(Home))
        now = 100L
        assertFalse(detector.recordTap(About))
        now = 200L
        assertTrue(detector.recordTap(About))
    }

    @Test
    fun tripleTapFiresOnceAndLeavesThirdTapAsCandidate() {
        var now = 0L
        val detector = detector { now }
        assertFalse(detector.recordTap(Home))
        now = 100L
        assertTrue("the first two taps form the pair", detector.recordTap(Home))
        now = 200L
        assertFalse("the third tap starts a new candidate", detector.recordTap(Home))
        assertTrue(detector.hasPendingTap)
    }

    @Test
    fun clearDropsTheCandidate() {
        var now = 0L
        val detector = detector { now }
        detector.recordTap(Home)
        now = 100L
        detector.clear()
        assertFalse(detector.recordTap(Home))
    }
}
