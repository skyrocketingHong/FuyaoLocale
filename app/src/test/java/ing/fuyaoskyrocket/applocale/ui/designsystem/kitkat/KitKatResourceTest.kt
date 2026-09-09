package ing.fuyaoskyrocket.applocale.ui.designsystem.kitkat

import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloDrawables
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloSymbolDrawables
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.resolveHoloDrawableId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KitKatResourceTest {
    @Test
    fun everySharedWidgetAliasResolvesToALinkedKitKatAssetInBothModes() {
        val ownIds = R.drawable::class.java.fields
            .filter { it.name.startsWith("kitkat_") }
            .map { it.getInt(null) }.toSet()
        assertTrue(ownIds.isNotEmpty() && 0 !in ownIds)
        val aliases = listOf(HoloDrawables, HoloSymbolDrawables).flatMap { holder ->
            holder.javaClass.methods.filter {
                it.name.startsWith("get") && it.parameterCount == 0 &&
                    it.returnType == Int::class.javaPrimitiveType
            }.map { it.invoke(holder) as Int }
        }
        assertTrue(aliases.isNotEmpty())
        aliases.forEach { alias ->
            listOf(false, true).forEach { dark ->
                val resolved = resolveKitKatDrawableId(alias, dark)
                assertTrue(resolved in ownIds)
                assertNotEquals(resolveHoloDrawableId(alias, dark), resolved)
            }
        }
    }

    @Test
    fun versionAndModeSwitchingDoesNotChangeTheOtherAssetSet() {
        val reference = HoloDrawables.Button
        assertEquals(R.drawable.kitkat_btn_default_holo_light, resolveKitKatDrawableId(reference, false))
        assertEquals(R.drawable.holo_ics_btn_default_holo_dark, resolveHoloDrawableId(reference, true))
        assertEquals(R.drawable.kitkat_btn_default_holo_dark, resolveKitKatDrawableId(reference, true))
        assertEquals(R.drawable.holo_ics_btn_default_holo_light, resolveHoloDrawableId(reference, false))
    }

    @Test(expected = IllegalStateException::class)
    fun anUnmappedCompatibilityKeyCannotSilentlyLoadAnIcsAsset() {
        resolveKitKatDrawableId(-1, true)
    }
}
