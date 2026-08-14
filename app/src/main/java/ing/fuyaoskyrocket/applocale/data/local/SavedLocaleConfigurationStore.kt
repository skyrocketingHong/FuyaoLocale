package ing.fuyaoskyrocket.applocale.data.local

import android.content.SharedPreferences
import ing.fuyaoskyrocket.applocale.model.ConfigurationImportResult
import ing.fuyaoskyrocket.applocale.model.SavedLocaleConfiguration
import ing.fuyaoskyrocket.applocale.model.SavedLocaleEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Small, self-contained persistence boundary for saved locale configurations.
 *
 * Configurations are stored separately from pinned locales because they are
 * user-owned presets rather than language-picker preferences.
 */
@Singleton
class SavedLocaleConfigurationStore @Inject constructor(
    private val preferences: SharedPreferences,
) {
    private val _configurations = MutableStateFlow(readConfigurations())
    val configurations: StateFlow<List<SavedLocaleConfiguration>> = _configurations.asStateFlow()

    fun save(entries: List<SavedLocaleEntry>): SavedLocaleConfiguration {
        val configuration = SavedLocaleConfiguration(
            id = UUID.randomUUID().toString(),
            createdAt = System.currentTimeMillis(),
            entries = entries,
        )
        update(listOf(configuration) + _configurations.value)
        return configuration
    }

    fun delete(id: String) {
        update(_configurations.value.filterNot { it.id == id })
    }

    /** Exports one preset in the same portable JSON-array format accepted by import. */
    fun serialize(configuration: SavedLocaleConfiguration): String =
        listOf(configuration).toConfigurationJson().toString(2)

    /**
     * Imports the portable JSON array used by this store. Matching ids replace
     * older local copies, so the same file can be imported again after updating it.
     */
    fun importSerialized(serialized: String): ConfigurationImportResult {
        val imported = parseConfigurations(serialized).distinctBy { it.id }
        if (imported.isEmpty()) return ConfigurationImportResult.InvalidFormat

        val existing = _configurations.value
        val importedIds = imported.mapTo(mutableSetOf()) { it.id }
        val replacedCount = existing.count { it.id in importedIds }
        update((imported + existing.filterNot { it.id in importedIds }).sortedByDescending { it.createdAt })
        return ConfigurationImportResult.Success(
            importedCount = imported.size,
            replacedCount = replacedCount,
        )
    }

    private fun update(configurations: List<SavedLocaleConfiguration>) {
        _configurations.value = configurations
        preferences.edit()
            .putString(KEY, configurations.toConfigurationJson().toString())
            .apply()
    }

    private fun readConfigurations(): List<SavedLocaleConfiguration> {
        val serialized = preferences.getString(KEY, null) ?: return emptyList()
        return parseConfigurations(serialized)
    }

    private fun parseConfigurations(serialized: String): List<SavedLocaleConfiguration> {
        return runCatching {
            val configurations = JSONArray(serialized)
            buildList {
                for (index in 0 until configurations.length()) {
                    val json = configurations.optJSONObject(index) ?: continue
                    val id = json.optString(JSON_ID)
                    val createdAt = json.optLong(JSON_CREATED_AT)
                    val entries = json.optJSONArray(JSON_ENTRIES)
                        ?.toEntries()
                        ?.distinctBy(SavedLocaleEntry::packageName)
                        .orEmpty()
                    if (id.isNotBlank() && createdAt > 0L && entries.isNotEmpty()) {
                        add(
                            SavedLocaleConfiguration(
                                id = id,
                                createdAt = createdAt,
                                entries = entries,
                            ),
                        )
                    }
                }
            }.sortedByDescending { it.createdAt }
        }.getOrDefault(emptyList())
    }

    private fun List<SavedLocaleConfiguration>.toConfigurationJson(): JSONArray =
        JSONArray().also { configurations ->
            forEach { configuration ->
                configurations.put(
                    JSONObject()
                        .put(JSON_ID, configuration.id)
                        .put(JSON_CREATED_AT, configuration.createdAt)
                        .put(JSON_ENTRIES, configuration.entries.toEntryJson()),
                )
            }
        }

    private fun List<SavedLocaleEntry>.toEntryJson(): JSONArray =
        JSONArray().also { entries ->
            forEach { entry ->
                entries.put(
                    JSONObject()
                        .put(JSON_PACKAGE_NAME, entry.packageName)
                        .put(JSON_LABEL, entry.label)
                        .put(JSON_LOCALE_TAG, entry.localeTag),
                )
            }
        }

    private fun JSONArray.toEntries(): List<SavedLocaleEntry> =
        buildList {
            for (index in 0 until length()) {
                val json = optJSONObject(index) ?: continue
                val packageName = json.optString(JSON_PACKAGE_NAME)
                val localeTag = json.optString(JSON_LOCALE_TAG)
                if (packageName.isNotBlank() && localeTag.isNotBlank()) {
                    add(
                        SavedLocaleEntry(
                            packageName = packageName,
                            label = json.optString(JSON_LABEL, packageName),
                            localeTag = localeTag,
                        ),
                    )
                }
            }
        }

    private companion object {
        const val KEY = "saved_locale_configurations"
        const val JSON_ID = "id"
        const val JSON_CREATED_AT = "createdAt"
        const val JSON_ENTRIES = "entries"
        const val JSON_PACKAGE_NAME = "packageName"
        const val JSON_LABEL = "label"
        const val JSON_LOCALE_TAG = "localeTag"
    }
}
