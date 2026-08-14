package ing.fuyaoskyrocket.applocale.data.system

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import ing.fuyaoskyrocket.applocale.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Encapsulates all [PackageManager] access so ViewModels never touch it directly.
 */
@Singleton
class PackageDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val pm: PackageManager
        get() = context.packageManager

    /** Returns enabled, non-self applications. */
    fun getInstalledApplications(): List<ApplicationInfo> {
        return pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
            .filter { it.enabled && it.packageName != BuildConfig.APPLICATION_ID }
    }

    fun getLabel(info: ApplicationInfo): String {
        return info.loadLabel(pm).toString()
    }

    fun getApplicationInfo(packageName: String): ApplicationInfo {
        return pm.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
    }

    fun getLaunchIntent(packageName: String): Intent? =
        pm.getLaunchIntentForPackage(packageName)
}
