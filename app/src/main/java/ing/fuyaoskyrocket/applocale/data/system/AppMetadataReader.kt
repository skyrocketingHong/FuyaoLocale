package ing.fuyaoskyrocket.applocale.data.system

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import ing.fuyaoskyrocket.applocale.model.AppDisplayMetadata
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reads the read-only display metadata for a package. Every read runs on IO and
 * returns null as a whole on failure, so the caller can distinguish "not
 * available" from fabricated zero values. It never participates in locale
 * writes.
 */
@Singleton
class AppMetadataReader @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun read(packageName: String): AppDisplayMetadata? = withContext(Dispatchers.IO) {
        runCatching {
            val pm = context.packageManager
            val info = pm.getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(0),
            )
            AppDisplayMetadata(
                versionName = info.versionName,
                versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    info.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    info.versionCode.toLong()
                },
                minSdk = info.applicationInfo?.minSdkVersion,
                targetSdk = info.applicationInfo?.targetSdkVersion,
            )
        }.getOrNull()
    }
}
