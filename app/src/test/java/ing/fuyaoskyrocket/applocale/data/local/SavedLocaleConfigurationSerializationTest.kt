package ing.fuyaoskyrocket.applocale.data.local

import android.content.SharedPreferences
import ing.fuyaoskyrocket.applocale.model.ConfigurationImportResult
import ing.fuyaoskyrocket.applocale.model.SavedLocaleConfiguration
import ing.fuyaoskyrocket.applocale.model.SavedLocaleEntry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * JSON round-trip checks against a REAL org.json implementation (the JVM
 * testRuntime dependency, not the android.jar stub — round-8 039): V1
 * compatibility, V2 explicit defaults, and the strict import rejections.
 */
class SavedLocaleConfigurationSerializationTest {

    /** Minimal in-memory SharedPreferences; commit always succeeds. */
    private class FakeSharedPreferences : SharedPreferences {
        val values = mutableMapOf<String, Any?>()

        override fun getAll(): Map<String, *> = values

        override fun getString(key: String?, defValue: String?): String? =
            values[key] as? String ?: defValue

        override fun getStringSet(key: String?, defValues: Set<String>?): Set<String>? = null
        override fun getInt(key: String?, defValue: Int): Int = 0
        override fun getLong(key: String?, defValue: Long): Long = 0L
        override fun getFloat(key: String?, defValue: Float): Float = 0f
        override fun getBoolean(key: String?, defValue: Boolean): Boolean = false
        override fun contains(key: String?): Boolean = values.containsKey(key)
        override fun edit(): SharedPreferences.Editor = Editor(values)

        private val listeners = mutableListOf<SharedPreferences.OnSharedPreferenceChangeListener>()

        override fun registerOnSharedPreferenceChangeListener(
            listener: SharedPreferences.OnSharedPreferenceChangeListener?,
        ) {
            listener?.let(listeners::add)
        }

        override fun unregisterOnSharedPreferenceChangeListener(
            listener: SharedPreferences.OnSharedPreferenceChangeListener?,
        ) {
            listeners.remove(listener)
        }

        private class Editor(private val values: MutableMap<String, Any?>) : SharedPreferences.Editor {
            private val pending = mutableMapOf<String, Any?>()
            private val removals = mutableSetOf<String>()

            override fun putString(key: String?, value: String?): SharedPreferences.Editor {
                if (key != null) {
                    pending[key] = value
                }
                return this
            }

            override fun putStringSet(
                key: String?,
                values: MutableSet<String>?,
            ): SharedPreferences.Editor = this

            override fun putInt(key: String?, value: Int): SharedPreferences.Editor = this
            override fun putLong(key: String?, value: Long): SharedPreferences.Editor = this
            override fun putFloat(key: String?, value: Float): SharedPreferences.Editor = this
            override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor = this
            override fun remove(key: String?): SharedPreferences.Editor {
                removals += key!!
                return this
            }

            override fun clear(): SharedPreferences.Editor = this
            override fun commit(): Boolean {
                values.putAll(pending)
                removals.forEach { values.remove(it) }
                return true
            }

            override fun apply() {
                commit()
            }
        }
    }

    private fun store(preloaded: String? = null): SavedLocaleConfigurationStore {
        val prefs = FakeSharedPreferences()
        if (preloaded != null) {
            prefs.values["saved_locale_configurations"] = preloaded
        }
        return SavedLocaleConfigurationStore(prefs)
    }

    @Test
    fun v1FilesStayReadableAndBlankTagsNeverBecomeDefaults() = runBlocking {
        val v1 = """
            [
              {
                "id": "v1-1",
                "createdAt": 100,
                "entries": [
                  {"packageName": "a.one", "label": "One", "localeTag": "ja"},
                  {"packageName": "a.two", "label": "Two", "localeTag": ""}
                ]
              }
            ]
        """.trimIndent()
        val configurations = store(v1).configurations.value
        assertEquals(1, configurations.size)
        assertEquals(
            listOf(SavedLocaleEntry("a.one", "One", "ja")),
            configurations.single().entries,
        )
    }

    @Test
    fun v2ExplicitDefaultEntriesRoundTrip() = runBlocking {
        val store = store()
        val saved = store.save(
            listOf(
                SavedLocaleEntry("a.one", "One", "ja"),
                SavedLocaleEntry("a.two", "Two", null),
            ),
        )
        val serialized = store.serialize(saved)
        assertTrue("V2 marker must be exported", serialized.contains("\"schemaVersion\": 2"))
        assertTrue(
            "a null tag is an explicit systemDefault mode, never the string null",
            serialized.contains("\"mode\": \"systemDefault\""),
        )

        val imported = store.importSerialized(serialized)
        assertTrue(imported is ConfigurationImportResult.Success)
        val restored = store.configurations.value.single { it.id == saved.id }
        assertEquals(
            listOf("a.one" to "ja", "a.two" to null),
            restored.entries.map { it.packageName to it.localeTag },
        )
    }

    @Test
    fun strictImportRejectsInvalidFilesEntirely() = runBlocking {
        val cases = listOf(
            "[]",
            """[{"id":"x","createdAt":1,"schemaVersion":2,"entries":[{"packageName":"a","label":"a","mode":"nope","localeTag":"ja"}]}]""",
            """[{"id":"x","createdAt":1,"schemaVersion":2,"entries":[{"packageName":"a","label":"a","mode":"locale","localeTag":""}]}]""",
            """[{"id":"x","createdAt":1,"schemaVersion":2,"entries":[{"packageName":"a","label":"a","mode":"systemDefault","localeTag":"ja"}]}]""",
            """[{"id":"x","createdAt":1,"schemaVersion":3,"entries":[{"packageName":"a","label":"a","mode":"locale","localeTag":"ja"}]}]""",
            """[{"id":"x","createdAt":1,"schemaVersion":2,"entries":[{"packageName":"a","label":"a","localeTag":"ja"},{"packageName":"a","label":"a2","localeTag":"en"}]}]""",
            """[{"id":"x","createdAt":1,"entries":[{"packageName":"a","label":"a","localeTag":"ja"}]},{"id":"x","createdAt":2,"entries":[{"packageName":"b","label":"b","localeTag":"ja"}]}]""",
            // V1 blank tag can never upgrade into a default reset.
            """[{"id":"x","createdAt":1,"entries":[{"packageName":"a","label":"a","localeTag":""}]}]""",
        )
        for (serialized in cases) {
            val store = store()
            assertEquals(
                "expected rejection for: $serialized",
                ConfigurationImportResult.InvalidFormat,
                store.importSerialized(serialized),
            )
            assertEquals("a rejected import must write nothing", 0, store.configurations.value.size)
        }
    }

    @Test
    fun fixedIdSaveIsIdempotentAndRefusesConflictingContent() = runBlocking {
        val store = store()
        val snapshot = SavedLocaleConfiguration(
            id = "op-1",
            createdAt = 5L,
            entries = listOf(SavedLocaleEntry("a.one", "One", "ja")),
        )
        assertTrue(
            store.saveWithFixedId(snapshot, "op-1") is FixedIdSaveResult.Saved,
        )
        assertEquals(
            "same id + same snapshot is a no-op success",
            FixedIdSaveResult.Identical,
            store.saveWithFixedId(snapshot, "op-1"),
        )
        val conflicting = snapshot.copy(createdAt = 6L)
        assertEquals(
            FixedIdSaveResult.Refused,
            store.saveWithFixedId(conflicting),
        )
        assertEquals(1, store.configurations.value.size)
        assertEquals(5L, store.configurations.value.single().createdAt)
    }

    @Test
    fun pendingRecordRoundTripsThroughTheStore() = runBlocking {
        val store = store()
        val record = ing.fuyaoskyrocket.applocale.model.PendingConfigurationEdit(
            operationId = "op-9",
            sourceConfigurationId = "source-1",
            packageName = "a.one",
            previousLocaleTag = "ja",
            targetLocaleTag = null,
            newConfiguration = SavedLocaleConfiguration(
                id = "op-9",
                createdAt = 7L,
                entries = listOf(
                    SavedLocaleEntry("a.one", "One", null),
                    SavedLocaleEntry("a.two", "Two", "en"),
                ),
            ),
        )
        assertTrue(store.writePendingEdit(record))
        val restored = store.readPendingEdit()
        assertNotNull(restored)
        assertEquals(record, restored)
        // Clearing happens in the same commit path the coordinator uses.
        assertTrue(
            store.saveWithFixedId(record.newConfiguration, record.operationId) is FixedIdSaveResult.Saved,
        )
        assertNull("the save clears the matching record", store.readPendingEdit())
    }

}
