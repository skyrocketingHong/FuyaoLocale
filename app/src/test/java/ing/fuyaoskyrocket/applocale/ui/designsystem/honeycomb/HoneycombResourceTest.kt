package ing.fuyaoskyrocket.applocale.ui.designsystem.honeycomb

import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloAsset
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.resolveIcsAsset
import ing.fuyaoskyrocket.applocale.ui.designsystem.kitkat.resolveKitKatAsset
import org.junit.Assert.*
import org.junit.Test

class HoneycombResourceTest {
    @Test
    fun everyWidgetAndModeUsesALinkedApi11Resource() {
        val own = (R.drawable::class.java.declaredFields + R.color::class.java.declaredFields)
            .filter { it.name.startsWith("honeycomb_") }.map { it.getInt(null) }.toSet()
        HoloAsset.entries.forEach { asset ->
            listOf(false, true).forEach { dark ->
                val resolved = resolveHoneycombAsset(asset, dark)
                assertTrue("$asset must be linked", resolved != 0)
                assertTrue("$asset must come from API 11", resolved in own)
                assertNotEquals(resolveIcsAsset(asset, dark), resolved)
                assertNotEquals(resolveKitKatAsset(asset, dark), resolved)
            }
        }
    }
}
