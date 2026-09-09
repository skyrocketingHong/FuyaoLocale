package ing.fuyaoskyrocket.applocale.ui.screen

import androidx.lifecycle.Lifecycle
import org.junit.Assert.*
import org.junit.Test

class PageScopingTest {
    @Test fun neighboringLanguagePickersDoNotShareModels() {
        assertNotEquals(pageViewModelKey(0, null, "LocalePicker"), pageViewModelKey(1, null, "LocalePicker"))
        assertEquals(pageViewModelKey(0, null, "LocalePicker"), pageViewModelKey(0, null, "LocalePicker"))
        assertNotEquals(pageViewModelKey(0, "custom", "Picker"), pageViewModelKey(0, "custom", "Detail"))
    }

    @Test fun detailRoutesKeepTheirOriginalModelKeys() {
        assertNull(pageViewModelKey(null, null, "Picker"))
        assertEquals("configuration-picker", pageViewModelKey(null, "configuration-picker", "Picker"))
    }

    @Test fun previewPagesCannotReachTheBackAndEventActivationState() {
        Lifecycle.State.entries.forEach { parent ->
            assertFalse(pageInteractionLifecycle(parent, false).isAtLeast(Lifecycle.State.STARTED))
        }
    }

    @Test fun activePageNeverOutlivesItsNavigationEntry() {
        Lifecycle.State.entries.forEach { parent ->
            assertEquals(parent, pageInteractionLifecycle(parent, true))
        }
    }
}
