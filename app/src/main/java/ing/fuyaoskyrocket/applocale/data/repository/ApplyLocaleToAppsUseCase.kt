package ing.fuyaoskyrocket.applocale.data.repository

import ing.fuyaoskyrocket.applocale.data.system.PrivilegedLocaleDataSource
import ing.fuyaoskyrocket.applocale.model.BatchLocaleResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cross-repository use case: applies a single locale (or system default)
 * to multiple packages in one Binder round-trip.
 */
@Singleton
class ApplyLocaleToAppsUseCase @Inject constructor(
    private val localeDataSource: PrivilegedLocaleDataSource,
    private val appRepository: AppRepository,
) {
    /**
     * @param packages target package names.
     * @param localeTag BCP-47 tag, or null/blank to reset to system default.
     */
    suspend operator fun invoke(packages: List<String>, localeTag: String?): BatchLocaleResult {
        if (packages.isEmpty()) return BatchLocaleResult(0, emptyList())
        val failed = localeDataSource.setApplicationLocalesForPackages(packages, localeTag)
        val failedSet = failed.toSet()
        appRepository.updateCachedLocaleTags(
            packages
                .asSequence()
                .filterNot(failedSet::contains)
                .associateWith { localeTag },
        )
        return BatchLocaleResult(
            totalCount = packages.size,
            failedPackages = failed,
        )
    }
}
