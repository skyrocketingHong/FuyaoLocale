package ing.fuyaoskyrocket.applocale.data.repository

import android.content.pm.ApplicationInfo
import ing.fuyaoskyrocket.applocale.data.system.PackageDataSource
import ing.fuyaoskyrocket.applocale.data.system.PrivilegedLocaleDataSource
import ing.fuyaoskyrocket.applocale.model.AppModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Combines [PackageDataSource] and [PrivilegedLocaleDataSource] to build the
 * immutable [AppModel] list shown on the home screen.
 */
@Singleton
class AppRepository @Inject constructor(
    private val packageDataSource: PackageDataSource,
    private val localeDataSource: PrivilegedLocaleDataSource,
) {
    private val cacheLock = Any()

    @Volatile
    private var cachedApps: List<AppModel>? = null

    /**
     * Returns the last complete app snapshot for Activity/configuration recreation.
     * The snapshot is process-local; pull-to-refresh remains the explicit way to rescan packages.
     */
    fun getCachedApps(): List<AppModel>? = cachedApps

    fun replaceCachedApps(apps: List<AppModel>) {
        synchronized(cacheLock) {
            cachedApps = apps.toList()
        }
    }

    /** Keeps the process cache coherent when another screen changes app locales. */
    fun updateCachedLocaleTags(localeTags: Map<String, String?>) {
        if (localeTags.isEmpty()) return
        synchronized(cacheLock) {
            val current = cachedApps ?: return
            cachedApps = current.map { app ->
                if (app.packageName in localeTags) {
                    app.copy(
                        localeTag = localeTags[app.packageName]
                            ?.takeIf(String::isNotBlank),
                    )
                } else {
                    app
                }
            }
        }
    }

    /**
     * Quickly returns the app list **without** locale info so the UI can render
     * immediately.  Call [fetchLocaleTags] afterwards and merge.
     */
    suspend fun loadApps(): List<AppModel> = withContext(Dispatchers.IO) {
        packageDataSource.getInstalledApplications().map { info ->
            AppModel(
                packageName = info.packageName,
                label = packageDataSource.getLabel(info),
                isSystemApp = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                localeTag = null,
            )
        }
    }

    /**
     * Batch query — one Binder round-trip for all packages.
     * Returns a map of packageName → localeTag (empty string = system default).
     */
    suspend fun fetchLocaleTags(packages: List<String>): Map<String, String> =
        withContext(Dispatchers.IO) {
            if (packages.isEmpty()) return@withContext emptyMap()
            val tags = localeDataSource.getApplicationLocaleTags(packages)
            packages.zip(tags).associate { (pkg, tag) -> pkg to tag }
        }

    suspend fun getApplicationInfo(packageName: String): ApplicationInfo =
        withContext(Dispatchers.IO) { packageDataSource.getApplicationInfo(packageName) }
}
