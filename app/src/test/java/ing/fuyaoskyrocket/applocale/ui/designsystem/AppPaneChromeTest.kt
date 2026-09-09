package ing.fuyaoskyrocket.applocale.ui.designsystem

import org.junit.Assert.assertEquals
import org.junit.Test

class AppPaneChromeTest {
    @Test
    fun unequalPaneTitlesReserveOneAlignedNavigationStrip() {
        val chrome = AppPaneChromeState(48, 56)
        chrome.reportTitle(0, 48)
        chrome.reportTitle(1, 80)
        assertEquals(80, chrome.maxTitleHeightPx)
        assertEquals(88, chrome.contentGapPx(0, true))
        assertEquals(56, chrome.contentGapPx(1, true))
        chrome.measuredTabHeightPx = 72
        assertEquals(104, chrome.contentGapPx(0, true))
        assertEquals(72, chrome.contentGapPx(1, true))
    }

    @Test
    fun contextualModeAndRemovedPanesCannotLeaveAnOldNavigationGap() {
        val chrome = AppPaneChromeState(56, 56)
        chrome.reportTitle(0, 112)
        chrome.reportTitle(1, 56)
        assertEquals(0, chrome.contentGapPx(0, false))
        assertEquals(56, chrome.contentGapPx(1, false))
        chrome.removePane(0)
        assertEquals(56, chrome.maxTitleHeightPx)
        assertEquals(0, chrome.contentGapPx(1, false))
    }
}
