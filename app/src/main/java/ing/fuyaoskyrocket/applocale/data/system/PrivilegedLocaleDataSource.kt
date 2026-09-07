package ing.fuyaoskyrocket.applocale.data.system

import android.os.LocaleList
import ing.fuyaoskyrocket.applocale.service.PrivilegedServiceClient
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps all privileged Binder locale operations.
 * ViewModels and Repositories use this instead of touching [ing.fuyaoskyrocket.applocale.IUserService] directly.
 */
@Singleton
class PrivilegedLocaleDataSource @Inject constructor(
    private val client: PrivilegedServiceClient,
) {
    private suspend fun service() =
        client.awaitService() ?: throw IllegalStateException("Privileged service unavailable")

    suspend fun getApplicationLocales(packageName: String): LocaleList =
        service().getApplicationLocales(packageName)

    suspend fun setApplicationLocales(packageName: String, locales: LocaleList) {
        service().setApplicationLocales(packageName, locales)
    }

    suspend fun getSystemLocales(): LocaleList =
        service().systemLocales

    suspend fun setSystemLocales(locales: LocaleList) {
        service().setSystemLocales(locales)
    }

    /**
     * Batch: returns the language tag for each package (empty = system default).
     * Single Binder round-trip.
     */
    suspend fun getApplicationLocaleTags(packageNames: List<String>): List<String> {
        if (packageNames.isEmpty()) return emptyList()
        val service = client.awaitService() ?: return List(packageNames.size) { "" }
        return service.getApplicationLocaleTags(packageNames.toTypedArray()).toList()
    }

    /**
     * Batch: apply [localeTag] to every package. Empty/null tag = reset to system default.
     * Returns the list of packages that failed.
     */
    suspend fun setApplicationLocalesForPackages(
        packageNames: List<String>,
        localeTag: String?,
    ): List<String> {
        if (packageNames.isEmpty()) return emptyList()
        val service = client.awaitService() ?: return packageNames
        val locales = if (localeTag.isNullOrBlank()) {
            LocaleList.getEmptyLocaleList()
        } else {
            LocaleList(Locale.forLanguageTag(localeTag))
        }
        return service.setApplicationLocalesForPackages(packageNames.toTypedArray(), locales).toList()
    }

    suspend fun forceStopPackage(packageName: String) {
        service().forceStopPackage(packageName)
    }

    suspend fun getFirstRunningTaskPackage(): String =
        service().firstRunningTaskPackage
}
