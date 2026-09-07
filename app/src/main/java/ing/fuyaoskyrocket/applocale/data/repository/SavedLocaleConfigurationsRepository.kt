package ing.fuyaoskyrocket.applocale.data.repository

import ing.fuyaoskyrocket.applocale.data.local.FixedIdSaveResult
import ing.fuyaoskyrocket.applocale.data.local.SavedLocaleConfigurationStore
import ing.fuyaoskyrocket.applocale.model.ConfigurationAppProjection
import ing.fuyaoskyrocket.applocale.model.ConfigurationAppSection
import ing.fuyaoskyrocket.applocale.model.ConfigurationImportResult
import ing.fuyaoskyrocket.applocale.model.PendingConfigurationEdit
import ing.fuyaoskyrocket.applocale.model.SavedLocaleConfiguration
import ing.fuyaoskyrocket.applocale.model.SavedLocaleEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns the user-facing configuration workflow: take a current process snapshot,
 * persist modified entries, and expose the saved presets to the UI.
 */
@Singleton
class SavedLocaleConfigurationsRepository @Inject constructor(
    private val appRepository: AppRepository,
    private val store: SavedLocaleConfigurationStore,
) {
    val configurations: StateFlow<List<SavedLocaleConfiguration>> = store.configurations

    fun findConfiguration(id: String): SavedLocaleConfiguration? =
        configurations.value.firstOrNull { it.id == id }

    suspend fun saveCurrentModifiedApps(): SavedLocaleConfiguration? =
        withContext(Dispatchers.IO) {
            val apps = appRepository.getCachedApps() ?: appRepository.loadApps()
            val localeTags = appRepository.fetchLocaleTags(apps.map { it.packageName })
            val entries = apps.mapNotNull { app ->
                localeTags[app.packageName]
                    ?.takeIf { it.isNotBlank() }
                    ?.let { localeTag ->
                        SavedLocaleEntry(
                            packageName = app.packageName,
                            label = app.label,
                            localeTag = localeTag,
                        )
                    }
            }
            entries.takeIf { it.isNotEmpty() }?.let { entriesToSave -> store.save(entriesToSave) }
        }

    suspend fun importSerialized(serialized: String): ConfigurationImportResult =
        withContext(Dispatchers.IO) { store.importSerialized(serialized) }

    fun serializeConfiguration(id: String): String? =
        findConfiguration(id)?.let(store::serialize)

    /**
     * Saves a caller-built snapshot under its own fixed id (round-8 038) — the
     * derived-configuration path. Never re-snapshots the whole device the way
     * [saveCurrentModifiedApps] does; same id + same snapshot is idempotent.
     */
    suspend fun saveWithFixedId(
        configuration: SavedLocaleConfiguration,
        clearPendingOperationId: String? = null,
    ): FixedIdSaveResult = withContext(Dispatchers.IO) {
        store.saveWithFixedId(configuration, clearPendingOperationId)
    }

    suspend fun writePendingEdit(record: PendingConfigurationEdit): Boolean =
        withContext(Dispatchers.IO) { store.writePendingEdit(record) }

    fun readPendingEdit(): PendingConfigurationEdit? = store.readPendingEdit()

    suspend fun clearPendingEdit() = withContext(Dispatchers.IO) { store.clearPendingEdit() }

    /**
     * The full editable detail projection (round-8 037/038): every saved entry
     * (including already-matching ones), every installed app the configuration
     * does not manage but which currently carries a custom locale, and the
     * remaining installed apps for the collapsed "other apps" section. Missing
     * packages from a partial locale query stay explicitly unreadable — an
     * absent answer is never reported as the system default.
     */
    suspend fun buildAppProjection(
        configuration: SavedLocaleConfiguration,
    ): List<ConfigurationAppProjection> = withContext(Dispatchers.IO) {
        val apps = appRepository.getCachedApps() ?: appRepository.loadApps()
        val localeTags = appRepository.fetchLocaleTags(apps.map { it.packageName })
        val appsByPackage = apps.associateBy { it.packageName }
        val managedPackages = configuration.entries.mapTo(mutableSetOf()) { it.packageName }

        val managedRows = configuration.entries.map { entry ->
            val app = appsByPackage[entry.packageName]
            val currentKnown = app != null && localeTags.containsKey(entry.packageName)
            ConfigurationAppProjection(
                packageName = entry.packageName,
                label = app?.label ?: entry.label,
                section = ConfigurationAppSection.Managed,
                isInstalled = app != null,
                isCurrentKnown = currentKnown,
                currentLocaleTag = localeTags[entry.packageName]?.takeIf(String::isNotBlank),
                isManaged = true,
                savedLocaleTag = entry.localeTag,
            )
        }.sortedWith(
            compareBy<ConfigurationAppProjection> { it.label.lowercase(Locale.ROOT) }
                .thenBy { it.packageName },
        )

        val unmanagedRows = apps.mapNotNull { app ->
            if (app.packageName in managedPackages) return@mapNotNull null
            val currentKnown = localeTags.containsKey(app.packageName)
            val currentTag = localeTags[app.packageName]?.takeIf(String::isNotBlank)
            ConfigurationAppProjection(
                packageName = app.packageName,
                label = app.label,
                section = if (currentTag != null) {
                    ConfigurationAppSection.ExternalModified
                } else {
                    ConfigurationAppSection.OtherApps
                },
                isInstalled = true,
                isCurrentKnown = currentKnown,
                currentLocaleTag = currentTag,
                isManaged = false,
                savedLocaleTag = null,
            )
        }.sortedWith(
            compareBy<ConfigurationAppProjection> { it.section.ordinal }
                .thenBy { it.label.lowercase(Locale.ROOT) }
                .thenBy { it.packageName },
        )

        managedRows + unmanagedRows
    }

    suspend fun delete(id: String) = withContext(Dispatchers.IO) { store.delete(id) }
}
