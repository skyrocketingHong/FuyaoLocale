package ing.fuyaoskyrocket.applocale.ui.designsystem.holo

import ing.fuyaoskyrocket.applocale.R
import java.lang.reflect.Modifier
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Detect aliases that accidentally inline the compile-time R stub's zero IDs. */
class HoloResourceIdsTest {
    @Test
    fun backgroundsUseLinkedDrawableIds() =
        assertLinkedAliases(HoloDrawables, R.drawable::class.java)

    @Test
    fun symbolsUseLinkedDrawableIds() =
        assertLinkedAliases(HoloSymbolDrawables, R.drawable::class.java)

    @Test
    fun textColorsUseLinkedColorIds() =
        assertLinkedAliases(HoloColorLists, R.color::class.java)

    private fun assertLinkedAliases(holder: Any, resources: Class<*>) {
        val linkedIds = resources.fields
            .filter { it.type == Int::class.javaPrimitiveType && it.name.startsWith("holo_ics_") }
            .map { it.getInt(null) }
            .toSet()
        assertTrue("The test must use the linked R class", linkedIds.any { it != 0 })

        // Accept fields AND properties, so changing an alias back to const val
        // remains covered rather than silently removing it from the test.
        val fields = holder.javaClass.fields
            .filter { it.type == Int::class.javaPrimitiveType && !it.name.startsWith("$") }
            .map { it.name to it.getInt(if (Modifier.isStatic(it.modifiers)) null else holder) }
        val properties = holder.javaClass.methods
            .filter {
                it.name.startsWith("get") && it.parameterCount == 0 &&
                    it.returnType == Int::class.javaPrimitiveType
            }
            .map { it.name.removePrefix("get") to (it.invoke(holder) as Int) }
        val aliases = fields + properties
        assertFalse("No resource aliases were inspected", aliases.isEmpty())
        aliases.forEach { (name, id) ->
            assertTrue("${holder.javaClass.simpleName}.$name resolved to resource ID 0", id != 0)
            assertTrue("$name is not a linked Holo resource: ${id.toString(16)}", id in linkedIds)
        }
    }
}
