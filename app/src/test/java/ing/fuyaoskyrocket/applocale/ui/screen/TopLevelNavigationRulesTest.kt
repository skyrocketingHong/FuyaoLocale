package ing.fuyaoskyrocket.applocale.ui.screen

import org.junit.Assert.assertEquals
import org.junit.Test

class TopLevelNavigationRulesTest {
    @Test fun launchPreferenceRestoresOnlyAValidMainPage() {
        assertEquals(Destinations.SETTINGS, restoredTopLevelRoute(true, Destinations.SETTINGS))
        assertEquals(Destinations.HOME, restoredTopLevelRoute(false, Destinations.SETTINGS))
        assertEquals(Destinations.HOME, restoredTopLevelRoute(true, "configuration_detail/123"))
        assertEquals(Destinations.HOME, restoredTopLevelRoute(true, "unknown"))
    }
}
