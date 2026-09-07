package ing.fuyaoskyrocket.applocale.data.local

import android.content.SharedPreferences
import ing.fuyaoskyrocket.applocale.model.ConfigurationImportResult
import ing.fuyaoskyrocket.applocale.model.PendingConfigurationEdit
import ing.fuyaoskyrocket.applocale.model.SavedLocaleConfiguration
import ing.fuyaoskyrocket.applocale.model.SavedLocaleEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** Result of a fixed-id save (round-8 038). */
sealed interface FixedIdSaveResult {
    /** The configuration was inserted and is now on disk. */
    data class Saved(val configuration: SavedLocaleConfiguration) : FixedIdSaveResult

    /** The same id with the same snapshot already exists — treated as done. */
    data object Identical : FixedIdSaveResult

    /** The same id exists with DIFFERENT content; nothing was overwritten. */
    data object Refused : FixedIdSaveResult

    /** The commit did not reach disk. */
    data object Failed : FixedIdSaveResult
}

/**
 * Small, self-contained persistence boundary for saved locale configurations.
 *
 * Configurations are stored separately from pinned locales because they are
 * user-owned presets rather than language-picker preferences.
 *
 * Round-8 038: every write goes through ONE serial mutex and only publishes
 * the StateFlow after the disk commit succeeded. Entries serialize as V2
 * (`schemaVersion` + explicit `mode`); V1 files stay readable, and imports are
 * validated strictly — an invalid entry rejects the whole file instead of
 * being silently dropped.
 */
@Singleton
class SavedLocaleConfigurationStore @Inject constructor(
    private val preferences: SharedPreferences,
) {
    private val writeMutex = Mutex()

    private val _configurations = MutableStateFlow(readStoredConfigurations())
    val configurations: StateFlow<List<SavedLocaleConfiguration>> = _configurations.asStateFlow()

    suspend fun save(entries: List<SavedLocaleEntry>): SavedLocaleConfiguration = writeMutex.withLock {
        val configuration = SavedLocaleConfiguration(
            id = UUID.randomUUID().toString(),
            createdAt = System.currentTimeMillis(),
            entries = entries,
        )
        persistLocked(listOf(configuration) + _configurations.value)
        configuration
    }

    /**
     * Idempotent save with a caller-fixed id (round-8 038): the same id plus
     * the same snapshot is a no-op success, the same id with different content
     * is refused, and the pending record for [clearPendingOperationId] is
     * removed in the SAME editor commit that inserts the configuration.
     */
    suspend fun saveWithFixedId(
        configuration: SavedLocaleConfiguration,
        clearPendingOperationId: String? = null,
    ): FixedIdSaveResult = writeMutex.withLock {
        val existing = _configurations.value.firstOrNull { it.id == configuration.id }
        when {
            existing != null && existing == configuration -> {
                if (clearPendingOperationId != null) {
                    clearPendingEditLocked(clearPendingOperationId)
                }
                FixedIdSaveResult.Identical
            }

            existing != null -> FixedIdSaveResult.Refused

            else -> {
                val next = (listOf(configuration) + _configurations.value)
                    .sortedByDescending { it.createdAt }
                val editor = preferences.edit()
                    .putString(KEY, next.toConfigurationJson().toString())
                if (clearPendingOperationId != null) {
                    editor.remove(KEY_PENDING_EDIT)
                }
                val committed = withContext(Dispatchers.IO) { editor.commit() }
                if (committed) {
                    _configurations.value = next
                    FixedIdSaveResult.Saved(configuration)
                } else {
                    FixedIdSaveResult.Failed
                }
            }
        }
    }

    suspend fun delete(id: String) {
        writeMutex.withLock {
            persistLocked(_configurations.value.filterNot { it.id == id })
        }
    }

    /** Exports one preset in the same portable JSON-array format accepted by import (always V2). */
    fun serialize(configuration: SavedLocaleConfiguration): String =
        listOf(configuration).toConfigurationJson().toString(2)

    /**
     * Imports the portable JSON array used by this store. The whole file is
     * parsed and validated BEFORE anything is written: matching ids replace
     * older local copies; any invalid configuration, unknown schema version,
     * duplicate id or duplicate package rejects the import.
     */
    suspend fun importSerialized(serialized: String): ConfigurationImportResult = writeMutex.withLock {
        val imported = parseConfigurationsStrict(serialized).getOrNull()
            ?: return ConfigurationImportResult.InvalidFormat
        if (imported.isEmpty()) return ConfigurationImportResult.InvalidFormat

        val existing = _configurations.value
        val importedIds = imported.mapTo(mutableSetOf()) { it.id }
        val replacedCount = existing.count { it.id in importedIds }
        persistLocked(
            (imported + existing.filterNot { it.id in importedIds })
                .sortedByDescending { it.createdAt },
        )
        ConfigurationImportResult.Success(
            importedCount = imported.size,
            replacedCount = replacedCount,
        )
    }

    /**
     * The durable prepared record for a live edit (round-8 038). Must land on
     * disk BEFORE any Binder apply; returns false when the write failed.
     */
    suspend fun writePendingEdit(record: PendingConfigurationEdit): Boolean = writeMutex.withLock {
        val committed = withContext(Dispatchers.IO) {
            preferences.edit()
                .putString(KEY_PENDING_EDIT, record.toPendingJson().toString())
                .commit()
        }
        committed
    }

    /** Reads the prepared record; SharedPreferences keeps the parsed value cached in memory. */
    fun readPendingEdit(): PendingConfigurationEdit? {
        val serialized = preferences.getString(KEY_PENDING_EDIT, null) ?: return null
        return runCatching { parsePendingEdit(serialized) }.getOrNull()
    }

    suspend fun clearPendingEdit() {
        writeMutex.withLock {
            withContext(Dispatchers.IO) {
                preferences.edit().remove(KEY_PENDING_EDIT).commit()
            }
        }
    }

    private suspend fun clearPendingEditLocked(operationId: String) {
        // Same-lock helper: only clears the record that matches this operation.
        val pending = readPendingEdit()
        if (pending?.operationId == operationId) {
            withContext(Dispatchers.IO) {
                preferences.edit().remove(KEY_PENDING_EDIT).commit()
            }
        }
    }

    /** Must be called under [writeMutex]; publishes only after a successful commit. */
    private suspend fun persistLocked(configurations: List<SavedLocaleConfiguration>) {
        val committed = withContext(Dispatchers.IO) {
            preferences.edit()
                .putString(KEY, configurations.toConfigurationJson().toString())
                .commit()
        }
        if (!committed) {
            throw IllegalStateException("Configuration preferences commit failed")
        }
        _configurations.value = configurations
    }

    private fun readStoredConfigurations(): List<SavedLocaleConfiguration> {
        val serialized = preferences.getString(KEY, null) ?: return emptyList()
        // Local recovery keeps the tolerant read: an invalid entry is skipped,
        // never upgraded into an implicit default action, and never rewrites
        // the stored bytes (round-8 038 serialization contract).
        return parseConfigurationsLenient(serialized)
    }

    private fun parseConfigurationsLenient(serialized: String): List<SavedLocaleConfiguration> {
        return runCatching {
            val configurations = JSONArray(serialized)
            buildList {
                for (index in 0 until configurations.length()) {
                    val json = configurations.optJSONObject(index) ?: continue
                    val parsed = parseConfiguration(json, strict = false) ?: continue
                    val deduped = parsed.copy(
                        entries = parsed.entries.distinctBy(SavedLocaleEntry::packageName),
                    )
                    if (deduped.entries.isNotEmpty()) {
                        add(deduped)
                    }
                }
            }.distinctBy { it.id }.sortedByDescending { it.createdAt }
        }.getOrDefault(emptyList())
    }

    private fun parseConfigurationsStrict(serialized: String): Result<List<SavedLocaleConfiguration>> =
        runCatching {
            val configurations = JSONArray(serialized)
            if (configurations.length() == 0) {
                throw IllegalArgumentException("Empty configuration file")
            }
            val parsed = buildList {
                for (index in 0 until configurations.length()) {
                    val json = configurations.optJSONObject(index)
                        ?: throw IllegalArgumentException("Configuration entry is not an object")
                    // Strict mode: EVERY field must be valid or the whole import fails.
                    add(
                        parseConfiguration(json, strict = true)
                            ?: throw IllegalArgumentException("Invalid configuration"),
                    )
                }
            }
            if (parsed.distinctBy { it.id }.size != parsed.size) {
                throw IllegalArgumentException("Duplicate configuration id")
            }
            parsed
        }

    /**
     * One entry interpreter shared by strict import and tolerant local
     * recovery (round-8 038). V1 (no schemaVersion): only non-blank locale
     * tags are meaningful — a blank one is invalid everywhere, never a default
     * reset. V2: `mode` is explicit and must agree with the tag.
     */
    private fun parseConfiguration(json: JSONObject, strict: Boolean): SavedLocaleConfiguration? {
        val schemaVersion = if (json.isNull(JSON_SCHEMA_VERSION)) {
            1
        } else {
            json.optInt(JSON_SCHEMA_VERSION)
        }
        if (schemaVersion != 1 && schemaVersion != 2) return null

        val id = json.optStringOrEmpty(JSON_ID)
        val createdAt = json.optLong(JSON_CREATED_AT)
        val entriesJson = json.optJSONArray(JSON_ENTRIES)
        if (id.isBlank() || createdAt <= 0L || entriesJson == null) return null

        val entries = buildList {
            for (index in 0 until entriesJson.length()) {
                val entryJson = entriesJson.optJSONObject(index)
                if (entryJson == null) {
                    if (strict) throw IllegalArgumentException("Entry is not an object")
                    continue
                }
                val entry = interpretEntry(entryJson, schemaVersion, strict) ?: continue
                add(entry)
            }
        }
        // Strict import rejects duplicates and empty configurations outright;
        // local recovery dedupes and may end up with no entries (kept if V1 had
        // at least one valid entry — matching the previous tolerant behavior).
        if (strict) {
            if (entries.isEmpty()) throw IllegalArgumentException("Configuration has no entries")
            if (entries.distinctBy(SavedLocaleEntry::packageName).size != entries.size) {
                throw IllegalArgumentException("Duplicate package in configuration")
            }
        }
        if (entries.isEmpty()) return null
        return SavedLocaleConfiguration(
            id = id,
            createdAt = createdAt,
            entries = entries,
        )
    }

    private fun interpretEntry(
        json: JSONObject,
        schemaVersion: Int,
        strict: Boolean,
    ): SavedLocaleEntry? {
        val packageName = json.optStringOrEmpty(JSON_PACKAGE_NAME)
        if (packageName.isBlank()) {
            if (strict) throw IllegalArgumentException("Blank package name")
            return null
        }
        val label = json.optStringOrEmpty(JSON_LABEL).ifBlank { packageName }
        val localeTag = json.optStringOrNull(JSON_LOCALE_TAG)

        if (schemaVersion == 1) {
            return when {
                localeTag != null && localeTag.isNotBlank() ->
                    SavedLocaleEntry(packageName, label, localeTag)

                else -> {
                    // A V1 blank/missing tag can NEVER become a default action.
                    if (strict) throw IllegalArgumentException("V1 entry without a locale tag")
                    null
                }
            }
        }

        val mode = json.optStringOrNull(JSON_MODE)
        return when (mode) {
            MODE_LOCALE -> {
                if (localeTag == null || localeTag.isBlank()) {
                    throw IllegalArgumentException("locale mode without a tag")
                }
                SavedLocaleEntry(packageName, label, localeTag)
            }

            MODE_SYSTEM_DEFAULT -> {
                if (!localeTag.isNullOrBlank()) {
                    throw IllegalArgumentException("systemDefault mode with a non-empty tag")
                }
                SavedLocaleEntry(packageName, label, null)
            }

            else -> {
                if (strict) throw IllegalArgumentException("Unknown entry mode")
                null
            }
        }
    }

    private fun List<SavedLocaleConfiguration>.toConfigurationJson(): JSONArray =
        JSONArray().also { configurations ->
            forEach { configuration ->
                configurations.put(
                    JSONObject()
                        .put(JSON_SCHEMA_VERSION, 2)
                        .put(JSON_ID, configuration.id)
                        .put(JSON_CREATED_AT, configuration.createdAt)
                        .put(JSON_ENTRIES, configuration.entries.toEntryJson()),
                )
            }
        }

    private fun List<SavedLocaleEntry>.toEntryJson(): JSONArray =
        JSONArray().also { entries ->
            forEach { entry ->
                // A null tag is an explicit follow-system action: mode carries
                // the meaning, the tag serializes as an empty string — never
                // the literal "null".
                entries.put(
                    JSONObject()
                        .put(JSON_PACKAGE_NAME, entry.packageName)
                        .put(JSON_LABEL, entry.label)
                        .put(
                            JSON_MODE,
                            if (entry.localeTag == null) MODE_SYSTEM_DEFAULT else MODE_LOCALE,
                        )
                        .put(JSON_LOCALE_TAG, entry.localeTag.orEmpty()),
                )
            }
        }

    private fun PendingConfigurationEdit.toPendingJson(): JSONObject = JSONObject()
        .put(JSON_PENDING_OPERATION_ID, operationId)
        .put(JSON_PENDING_SOURCE_ID, sourceConfigurationId)
        .put(JSON_PENDING_PACKAGE, packageName)
        .put(JSON_PENDING_PREVIOUS_TAG, previousLocaleTag.orEmpty())
        .put(JSON_PENDING_TARGET_TAG, targetLocaleTag.orEmpty())
        .put(JSON_PENDING_CONFIGURATION, newConfiguration.toPendingConfigurationJson())

    private fun SavedLocaleConfiguration.toPendingConfigurationJson(): JSONObject = JSONObject()
        .put(JSON_ID, id)
        .put(JSON_CREATED_AT, createdAt)
        .put(JSON_ENTRIES, entries.toEntryJson())

    private fun parsePendingEdit(serialized: String): PendingConfigurationEdit? {
        val json = JSONObject(serialized)
        val operationId = json.optStringOrEmpty(JSON_PENDING_OPERATION_ID)
        val sourceConfigurationId = json.optStringOrEmpty(JSON_PENDING_SOURCE_ID)
        val packageName = json.optStringOrEmpty(JSON_PENDING_PACKAGE)
        if (operationId.isBlank() || sourceConfigurationId.isBlank() || packageName.isBlank()) {
            return null
        }
        val configurationJson = json.optJSONObject(JSON_PENDING_CONFIGURATION) ?: return null
        val configuration = parseConfiguration(
            configurationJson.putIfAbsent(JSON_SCHEMA_VERSION, 2),
            strict = false,
        ) ?: return null
        return PendingConfigurationEdit(
            operationId = operationId,
            sourceConfigurationId = sourceConfigurationId,
            packageName = packageName,
            // The record serializes a null tag as "" (never the literal
            // "null"); an empty string reads back as null — the tags are
            // never legitimately blank.
            previousLocaleTag = json.optStringOrNull(JSON_PENDING_PREVIOUS_TAG)
                ?.takeIf(String::isNotBlank),
            targetLocaleTag = json.optStringOrNull(JSON_PENDING_TARGET_TAG)
                ?.takeIf(String::isNotBlank),
            newConfiguration = configuration,
        )
    }

    /** optString turns JSON null into "null"; read nulls explicitly instead. */
    private fun JSONObject.optStringOrNull(key: String): String? = when {
        isNull(key) -> null
        else -> optString(key)
    }

    private fun JSONObject.optStringOrEmpty(key: String): String = optStringOrNull(key).orEmpty()

    private fun JSONObject.putIfAbsent(key: String, value: Int): JSONObject {
        if (isNull(key)) put(key, value)
        return this
    }

    private companion object {
        const val KEY = "saved_locale_configurations"
        const val KEY_PENDING_EDIT = "pending_configuration_edit"
        const val JSON_SCHEMA_VERSION = "schemaVersion"
        const val JSON_ID = "id"
        const val JSON_CREATED_AT = "createdAt"
        const val JSON_ENTRIES = "entries"
        const val JSON_PACKAGE_NAME = "packageName"
        const val JSON_LABEL = "label"
        const val JSON_LOCALE_TAG = "localeTag"
        const val JSON_MODE = "mode"
        const val MODE_LOCALE = "locale"
        const val MODE_SYSTEM_DEFAULT = "systemDefault"
        const val JSON_PENDING_OPERATION_ID = "operationId"
        const val JSON_PENDING_SOURCE_ID = "sourceConfigurationId"
        const val JSON_PENDING_PACKAGE = "packageName"
        const val JSON_PENDING_PREVIOUS_TAG = "previousLocaleTag"
        const val JSON_PENDING_TARGET_TAG = "targetLocaleTag"
        const val JSON_PENDING_CONFIGURATION = "newConfiguration"
    }
}
