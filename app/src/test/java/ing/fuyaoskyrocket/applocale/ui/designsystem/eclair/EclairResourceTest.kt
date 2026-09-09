package ing.fuyaoskyrocket.applocale.ui.designsystem.eclair

import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSymbol
import org.junit.Assert.assertTrue
import org.junit.Test

class EclairResourceTest {
    @Test
    fun everyNamedGlyphLinksToAnEclairResourceInsteadOfAnInlineZeroOrAnotherEra() {
        val allowed = R.drawable::class.java.declaredFields.filter { it.name.startsWith("eclair_") }
            .map { it.getInt(null) }.toSet()
        AppSymbol.entries.forEach { symbol ->
            val resource = eclairSymbolResource(symbol)
            assertTrue("$symbol must have a linked resource", resource != 0)
            assertTrue("$symbol must be an Eclair asset", resource in allowed)
        }
    }
}
