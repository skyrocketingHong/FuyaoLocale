package ing.fuyaoskyrocket.applocale.data.repository

import ing.fuyaoskyrocket.applocale.model.BatchLocaleResult
import ing.fuyaoskyrocket.applocale.model.SavedLocaleConfiguration
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Restores a saved preset with the fewest Binder calls by grouping apps that
 * share the same language tag.
 */
@Singleton
class ApplySavedLocaleConfigurationUseCase @Inject constructor(
    private val applyLocaleToApps: ApplyLocaleToAppsUseCase,
) {
    suspend operator fun invoke(configuration: SavedLocaleConfiguration): BatchLocaleResult {
        val failedPackages = buildList {
            configuration.entries
                .groupBy { it.localeTag }
                .forEach { (localeTag, entries) ->
                    addAll(
                        applyLocaleToApps(
                            packages = entries.map { it.packageName },
                            localeTag = localeTag,
                        ).failedPackages,
                    )
                }
        }
        return BatchLocaleResult(
            totalCount = configuration.entries.size,
            failedPackages = failedPackages.distinct(),
        )
    }
}
