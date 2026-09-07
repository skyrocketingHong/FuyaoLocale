package ing.fuyaoskyrocket.applocale.model

import ing.fuyaoskyrocket.applocale.data.repository.buildDerivedSnapshot
import ing.fuyaoskyrocket.applocale.data.repository.planEdit
import ing.fuyaoskyrocket.applocale.data.repository.EditPlan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The pure target/copy algorithm of the live edit (round-8 039): the
 * unmanaged/default/locale tri-state, the 038 key table decisions, and the
 * derived snapshot's "only one package changes, everything survives" rule.
 */
class ConfigurationEditLogicTest {

    private fun source(vararg entries: Pair<String, String?>) = SavedLocaleConfiguration(
        id = "source-1",
        createdAt = 1L,
        entries = entries.map { (pkg, tag) -> SavedLocaleEntry(pkg, pkg, tag) },
    )

    // ---- needsAttention tri-state on the projection ----

    @Test
    fun unmanagedRowsNeverNeedAttention() {
        val row = ConfigurationAppProjection(
            packageName = "a", label = "A", section = ConfigurationAppSection.OtherApps,
            isInstalled = true, isCurrentKnown = true, currentLocaleTag = "ja",
            isManaged = false, savedLocaleTag = null,
        )
        assertFalse(row.needsAttention)
    }

    @Test
    fun unreadableCurrentStateIsNotAGuessableDifference() {
        val row = ConfigurationAppProjection(
            packageName = "a", label = "A", section = ConfigurationAppSection.Managed,
            isInstalled = true, isCurrentKnown = false, currentLocaleTag = null,
            isManaged = true, savedLocaleTag = "en",
        )
        assertFalse("unknown must not count as differing from en", row.needsAttention)
    }

    @Test
    fun explicitDefaultEntryMatchesDefaultCurrentState() {
        val row = ConfigurationAppProjection(
            packageName = "a", label = "A", section = ConfigurationAppSection.Managed,
            isInstalled = true, isCurrentKnown = true, currentLocaleTag = null,
            isManaged = true, savedLocaleTag = null,
        )
        assertFalse("null == null is the explicit follow-system match", row.needsAttention)
    }

    @Test
    fun missingAppAlwaysNeedsAttention() {
        val row = ConfigurationAppProjection(
            packageName = "a", label = "A", section = ConfigurationAppSection.Managed,
            isInstalled = false, isCurrentKnown = false, currentLocaleTag = null,
            isManaged = true, savedLocaleTag = "ja",
        )
        assertTrue(row.needsAttention)
    }

    // ---- The 038 key table through planEdit ----

    @Test
    fun unmanagedJapaneseCurrentToEnglishApplies() {
        val plan = planEdit(source(), packageName = "a", targetLocaleTag = "en", currentTag = "ja")
        assertTrue(plan is EditPlan.ApplyAndSave)
        assertEquals("ja", (plan as EditPlan.ApplyAndSave).previousTag)
    }

    @Test
    fun unmanagedJapaneseCurrentToExplicitDefaultAppliesWithNullTarget() {
        val plan = planEdit(source(), packageName = "a", targetLocaleTag = null, currentTag = "ja")
        assertTrue(plan is EditPlan.ApplyAndSave)
    }

    @Test
    fun unmanagedDefaultCurrentToExplicitDefaultOnlySaves() {
        val plan = planEdit(source(), packageName = "a", targetLocaleTag = null, currentTag = null)
        assertTrue("management change only — no Binder", plan is EditPlan.SaveOnly)
    }

    @Test
    fun savedEqualsCurrentEqualsTargetIsNoChange() {
        val plan = planEdit(
            source("a" to "en"),
            packageName = "a", targetLocaleTag = "en", currentTag = "en",
        )
        assertTrue(plan is EditPlan.NoChange)
    }

    @Test
    fun savedEnglishCurrentJapaneseTargetEnglishApplies() {
        val plan = planEdit(
            source("a" to "en"),
            packageName = "a", targetLocaleTag = "en", currentTag = "ja",
        )
        assertTrue(plan is EditPlan.ApplyAndSave)
    }

    @Test
    fun savedEnglishCurrentJapaneseTargetJapaneseSavesOnly() {
        val plan = planEdit(
            source("a" to "en"),
            packageName = "a", targetLocaleTag = "ja", currentTag = "ja",
        )
        assertTrue(plan is EditPlan.SaveOnly)
    }

    // ---- Derived snapshot ----

    @Test
    fun derivedSnapshotOnlyTouchesTheTargetPackageAndKeepsUninstalled() {
        val src = source(
            "keep" to "ja",
            "missing" to "fr",
            "target" to "en",
        )
        val derived = buildDerivedSnapshot(
            operationId = "op-1",
            createdAt = 42L,
            source = src,
            packageName = "target",
            label = "Target",
            targetLocaleTag = "ko",
        )
        assertEquals("op-1", derived.id)
        assertEquals(42L, derived.createdAt)
        assertEquals(3, derived.entries.size)
        assertEquals(
            listOf("keep" to "ja", "missing" to "fr", "target" to "ko"),
            derived.entries.map { it.packageName to it.localeTag },
        )
        // The source is never mutated.
        assertEquals(listOf("keep" to "ja", "missing" to "fr", "target" to "en"),
            src.entries.map { it.packageName to it.localeTag })
    }

    @Test
    fun derivedSnapshotAppendsWhenTargetWasUnmanaged() {
        val src = source("keep" to "ja")
        val derived = buildDerivedSnapshot(
            operationId = "op-2",
            createdAt = 42L,
            source = src,
            packageName = "new",
            label = "New",
            targetLocaleTag = null,
        )
        assertEquals(2, derived.entries.size)
        assertEquals("new", derived.entries.last().packageName)
        assertEquals(null, derived.entries.last().localeTag)
    }
}
