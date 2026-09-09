package ing.fuyaoskyrocket.applocale.ui.designsystem.lollipop

import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemeStyle
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSymbol
import ing.fuyaoskyrocket.applocale.ui.designsystem.systemBarAppearance
import org.junit.Assert.*
import org.junit.Test

class LollipopResourceTest {
    @Test
    fun namedGlyphsResolveToOwnLinkedVersionedResources() {
        val own = R.drawable::class.java.declaredFields.filter { it.name.startsWith("lollipop_") }.map { it.getInt(null) }.toSet()
        AppSymbol.entries.forEach { symbol ->
            assertTrue("$symbol has a nonzero linked resource", lollipopSymbolResource(symbol) != 0)
            assertTrue("$symbol uses the pinned Android 5.0 closure", lollipopSymbolResource(symbol) in own)
        }
        assertTrue(R.style.LollipopDrawablesLight != 0)
        assertTrue(R.style.LollipopDrawablesDark != 0)
    }

    @Test
    fun bothSystemBarBackgroundsUseTheAppPrimaryDarkAndLightIcons() {
        val light = systemBarAppearance(AppThemeStyle.MATERIAL_LOLLIPOP, false)
        val dark = systemBarAppearance(AppThemeStyle.MATERIAL_LOLLIPOP, true)
        assertEquals(0xff303f9f.toInt(), light.background)
        assertEquals(0xff303f9f.toInt(), dark.background)
        assertTrue(light.darkSurface && dark.darkSurface)
    }
}
