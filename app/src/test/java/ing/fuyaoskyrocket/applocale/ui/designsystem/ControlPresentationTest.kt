package ing.fuyaoskyrocket.applocale.ui.designsystem

import ing.fuyaoskyrocket.applocale.ui.designsystem.component.switchDragFraction
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.holoSwitchGeometry
import org.junit.Assert.*
import org.junit.Test

class ControlPresentationTest {
    @Test fun glassRendererStaysMountedUntilItsExitCompletes() {
        val form = BottomDockFormState(true)
        form.request(false)
        assertTrue(form.changing)
        assertTrue(form.displayedGlass)
        form.finishExit()
        assertFalse(form.displayedGlass)
        assertFalse(form.changing)
    }

    @Test fun rapidReversalKeepsTheCurrentRendererAndLatestChoiceWins() {
        val form = BottomDockFormState(false)
        form.request(true)
        form.request(false)
        assertFalse(form.changing)
        form.finishExit()
        assertFalse(form.displayedGlass)
        form.request(true)
        form.finishExit()
        assertTrue(form.displayedGlass)
    }

    @Test fun holoThumbFitsTheTrackAtEveryPositionAndFontScale() {
        for (nativeThumb in listOf(48f, 96f)) for (label in listOf(24f, 48f, 96f)) {
            val geometry = holoSwitchGeometry(96f, label, 12f, nativeThumb, 8f, 12f)
            assertTrue(geometry.thumbWidth >= label + 24f + 8f)
            for (rtl in listOf(false, true)) for (fraction in listOf(-.1f, 0f, .25f, .5f, .75f, 1f, 1.1f)) {
                val left = geometry.thumbLeft(fraction, rtl)
                assertTrue(left >= 0f)
                assertTrue(left + geometry.thumbWidth <= geometry.width + .001f)
            }
            assertEquals(0f, geometry.thumbLeft(0f, false), .001f)
            assertEquals(geometry.width, geometry.thumbLeft(1f, false) + geometry.thumbWidth, .001f)
        }
    }

    @Test fun switchMovementTracksPixelsWithoutDependingOnDensityOrDirection() {
        for (density in listOf(1f, 2f, 3.5f)) {
            assertEquals(.6f, switchDragFraction(0f, 12f * density, 20f * density, false), .001f)
            assertEquals(.4f, switchDragFraction(1f, 12f * density, 20f * density, true), .001f)
        }
        assertEquals(1f, switchDragFraction(.5f, 100f, 20f, false), .001f)
        assertEquals(0f, switchDragFraction(.5f, -100f, 20f, false), .001f)
        assertEquals(.5f, switchDragFraction(.5f, 100f, 0f, false), .001f)
    }
}
