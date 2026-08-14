package ing.fuyaoskyrocket.applocale

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.LocaleList
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku
import ing.fuyaoskyrocket.applocale.di.AppEntryPoint
import ing.fuyaoskyrocket.applocale.service.PrivilegedServiceClient
import java.util.Locale

class LocaleQuickSettingsTile : TileService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var isLoaded = false
    private var pinnedTags = listOf<String>()
    private var targetPackage: ApplicationInfo? = null

    private fun entryPoint(): AppEntryPoint =
        EntryPointAccessors.fromApplication(applicationContext, AppEntryPoint::class.java)

    private val serviceClient: PrivilegedServiceClient
        get() = entryPoint().privilegedServiceClient()

    private fun loadPinnedTags() {
        if (!isLoaded) {
            pinnedTags = entryPoint().pinnedLocaleStore().getPinnedTags()
            isLoaded = true
        }
    }

    /**
     * Cycle: "" (system default) → first pinned → second → … → "" again.
     * Returns the next tag in the cycle.
     */
    private fun getNextLocaleTag(currentTag: String): String {
        if (pinnedTags.isEmpty()) return ""
        val cycle = listOf("") + pinnedTags
        val idx = cycle.indexOfFirst { it == currentTag }
        return cycle[(idx + 1).mod(cycle.size)]
    }

    private fun setDisabledTile() {
        qsTile.label = getString(R.string.app_name)
        qsTile.subtitle = getString(R.string.unavailable)
        qsTile.state = Tile.STATE_UNAVAILABLE
        qsTile.updateTile()
    }

    private fun updateTile() {
        scope.launch {
            try {
                val service = serviceClient.awaitService(timeoutMs = 5_000) ?: run {
                    setDisabledTile()
                    return@launch
                }

                val currentAppPackage = service.firstRunningTaskPackage
                if (currentAppPackage.isBlank() || currentAppPackage == BuildConfig.APPLICATION_ID) {
                    setDisabledTile()
                    return@launch
                }

                val info = packageManager.getApplicationInfo(
                    currentAppPackage,
                    PackageManager.ApplicationInfoFlags.of(0),
                )
                targetPackage = info

                if ((info.flags and ApplicationInfo.FLAG_SYSTEM) != 0) {
                    setDisabledTile()
                    return@launch
                }

                val appLocales = service.getApplicationLocales(currentAppPackage)
                val isCustom = !appLocales.isEmpty
                val currentDisplay = if (isCustom) {
                    val loc = appLocales[0]
                    loc.getDisplayName(loc).replaceFirstChar { it.uppercaseChar() }
                } else {
                    getString(R.string.system_default)
                }

                qsTile.state = if (isCustom) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
                qsTile.label = currentDisplay
                qsTile.subtitle = info.loadLabel(packageManager).toString()
                qsTile.updateTile()
            } catch (e: Exception) {
                Log.e(BuildConfig.APPLICATION_ID, "LocaleQuickSettingsTile updateTile failed", e)
            }
        }
    }

    override fun onStartListening() {
        if (BuildConfig.DEBUG)
            Log.d(BuildConfig.APPLICATION_ID, "LocaleQuickSettingsTile onStartListening()")
        super.onStartListening()
        setDisabledTile()

        try {
            if (!serviceClient.isConnected) {
                Shizuku.bindUserService(ShizukuArgs.userServiceArgs, serviceClient.connection)
            }
        } catch (e: Exception) {
            Log.e(
                BuildConfig.APPLICATION_ID,
                "Cannot bind UserService on LocaleQuickSettingsTile.\n" + e.stackTraceToString()
            )
            return
        }

        loadPinnedTags()
        if (pinnedTags.isNotEmpty())
            updateTile()
    }

    override fun onStopListening() {
        if (BuildConfig.DEBUG)
            Log.d(BuildConfig.APPLICATION_ID, "LocaleQuickSettingsTile onStopListening()")
        isLoaded = false
        pinnedTags = emptyList()

        var shouldUnbind = true
        run {
            try {
                val service = serviceClient.connection.service ?: return@run
                if (BuildConfig.APPLICATION_ID == service.firstRunningTaskPackage)
                    shouldUnbind = false
            } catch (e: Exception) {
                // ignore
            }
        }
        if (serviceClient.isConnected && shouldUnbind) {
            try {
                Shizuku.unbindUserService(
                    ShizukuArgs.userServiceArgs,
                    serviceClient.connection,
                    true,
                )
            } catch (e: Exception) {
                Log.e(BuildConfig.APPLICATION_ID, "LocaleQuickSettingsTile unbind failed", e)
            }
        }
        super.onStopListening()
    }

    override fun onClick() {
        if (BuildConfig.DEBUG)
            Log.d(BuildConfig.APPLICATION_ID, "LocaleQuickSettingsTile onClick()")
        super.onClick()

        val target = targetPackage ?: return

        scope.launch {
            try {
                val service = serviceClient.awaitService(timeoutMs = 5_000) ?: return@launch
                val currentLocales = service.getApplicationLocales(target.packageName)
                val currentTag = if (currentLocales.isEmpty) "" else currentLocales[0].toLanguageTag()
                val nextTag = getNextLocaleTag(currentTag)

                val localeList = if (nextTag.isBlank()) {
                    LocaleList.getEmptyLocaleList()
                } else {
                    LocaleList(Locale.forLanguageTag(nextTag))
                }
                service.setApplicationLocales(target.packageName, localeList)
                updateTile()
            } catch (e: Exception) {
                Log.e(BuildConfig.APPLICATION_ID, "LocaleQuickSettingsTile onClick failed", e)
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
