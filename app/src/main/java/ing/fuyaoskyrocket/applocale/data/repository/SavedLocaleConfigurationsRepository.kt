package ing.fuyaoskyrocket.applocale.data.repository

import ing.fuyaoskyrocket.applocale.data.local.SavedLocaleConfigurationStore
import ing.fuyaoskyrocket.applocale.model.ConfigurationImportResult
import ing.fuyaoskyrocket.applocale.model.SavedLocaleConfiguration
import ing.fuyaoskyrocket.applocale.model.SavedLocaleConfigurationComparison
import ing.fuyaoskyrocket.applocale.model.SavedLocaleDifference
import ing.fuyaoskyrocket.applocale.model.SavedLocaleDifferenceKind
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
            entries.takeIf { it.isNotEmpty() }?.let(store::save)
        }

    suspend fun importSerialized(serialized: String): ConfigurationImportResult =
        withContext(Dispatchers.IO) { store.importSerialized(serialized) }

    fun serializeConfiguration(id: String): String? =
        findConfiguration(id)?.let(store::serialize)

    /**
     * Compares a saved preset against the installed-app list in one locale query.
     * Differences include missing packages, changed locale values, and current
     * custom locales that the preset deliberately does not manage.
     */
    suspend fun compare(
        configuration: SavedLocaleConfiguration,
    ): SavedLocaleConfigurationComparison = withContext(Dispatchers.IO) {
        val apps = appRepository.getCachedApps() ?: appRepository.loadApps()
        val localeTags = appRepository.fetchLocaleTags(apps.map { it.packageName })
        val appsByPackage = apps.associateBy { it.packageName }
        val savedPackages = configuration.entries.mapTo(mutableSetOf()) { it.packageName }

        val differences = buildList {
            configuration.entries.forEach { entry ->
                val app = appsByPackage[entry.packageName]
                val currentTag = app?.let {
                    localeTags[it.packageName]?.takeIf(String::isNotBlank)
                }
                when {
                    app == null -> add(
                        SavedLocaleDifference(
                            kind = SavedLocaleDifferenceKind.MissingApp,
                            packageName = entry.packageName,
                            label = entry.label,
                            savedLocaleTag = entry.localeTag,
                        ),
                    )

                    currentTag != entry.localeTag -> add(
                        SavedLocaleDifference(
                            kind = SavedLocaleDifferenceKind.NeedsApply,
                            packageName = entry.packageName,
                            label = app.label,
                            currentLocaleTag = currentTag,
                            savedLocaleTag = entry.localeTag,
                        ),
                    )
                }
            }

            apps.forEach { app ->
                val currentTag = localeTags[app.packageName]?.takeIf(String::isNotBlank)
                if (currentTag != null && app.packageName !in savedPackages) {
                    add(
                        SavedLocaleDifference(
                            kind = SavedLocaleDifferenceKind.CurrentOnly,
                            packageName = app.packageName,
                            label = app.label,
                            currentLocaleTag = currentTag,
                        ),
                    )
                }
            }
        }.sortedWith(
            compareBy<SavedLocaleDifference> { it.kind.ordinal }
                .thenBy { it.label.lowercase(Locale.ROOT) }
                .thenBy { it.packageName },
        )

        SavedLocaleConfigurationComparison(configuration, differences)
    }

    fun delete(id: String) = store.delete(id)
}
