package ing.fuyaoskyrocket.applocale.ui.designsystem.legacy

import androidx.compose.ui.unit.IntSize
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyDrawableBoundsTest {
    @Test
    fun namedGlyphsFitWithoutStretchingOrUpscaling() {
        assertEquals(IntSize(32, 16), fitLegacyDrawableBounds(IntSize(64, 32), IntSize(32, 32)))
        assertEquals(IntSize(16, 32), fitLegacyDrawableBounds(IntSize(32, 64), IntSize(32, 32)))
        assertEquals(IntSize(12, 16), fitLegacyDrawableBounds(IntSize(12, 16), IntSize(48, 48)))
    }
}
