package ing.fuyaoskyrocket.applocale.ui.designsystem

import org.junit.Assert.*
import org.junit.Test

class AppThemeCatalogTest {
    @Test
    fun displayedRetroOrderDoesNotDependOnImplementationOrStoredEnumOrder() {
        val versions = AppThemeCatalog.retro.map { requireNotNull(it.androidVersionOrder) }
        assertEquals(versions.sorted(), versions)
        assertEquals(versions.size, versions.distinct().size)
        assertEquals(AppThemeStyle.ECLAIR, AppThemeCatalog.retro.first().style)
    }

    @Test
    fun everyStoredStyleHasOneLocalizedSelectionEntry() {
        assertEquals(AppThemeStyle.entries.toSet(), AppThemeCatalog.options.map { it.style }.toSet())
        assertEquals(AppThemeStyle.entries.size, AppThemeCatalog.options.size)
        AppThemeCatalog.options.forEach { assertTrue(it.titleRes != 0 && it.summaryRes != 0) }
    }
}
