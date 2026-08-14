package ing.fuyaoskyrocket.applocale.service

import android.app.IActivityManager
import android.app.IActivityTaskManager
import android.app.ILocaleManager
import android.os.Build
import android.os.LocaleList
import android.os.Process
import android.util.Log
import org.lsposed.hiddenapibypass.HiddenApiBypass
import rikka.shizuku.SystemServiceHelper
import ing.fuyaoskyrocket.applocale.BuildConfig
import ing.fuyaoskyrocket.applocale.IUserService
import kotlin.system.exitProcess


class UserService : IUserService.Stub() {

    override fun exit() {
        destroy()
    }

    override fun destroy() {
        exitProcess(0)
    }

    override fun getUid(): Int {
        return Process.myUid()
    }

    private fun getCurrentUserId(): Int =
        HiddenApiBypass.invoke(
            Class.forName("android.app.ActivityManager"),
            null,
            "getCurrentUser",
        ) as Int

    // ------------------------------------------------------------------ Locale

    var LOCALE_MANAGER: ILocaleManager? = null
    fun requiresLocaleManager() {
        if (LOCALE_MANAGER != null) return
        val localeBinder = SystemServiceHelper.getSystemService("locale")
        LOCALE_MANAGER = ILocaleManager.Stub.asInterface(localeBinder)
    }

    private fun setAppLocalesInternal(packageName: String, locales: LocaleList?) {
        requiresLocaleManager()
        val currentUser = getCurrentUserId()
        if (Build.VERSION.SDK_INT == 33 && Build.VERSION.RELEASE_OR_CODENAME != "UpsideDownCake") {
            LOCALE_MANAGER!!.setApplicationLocales(packageName, currentUser, locales)
            return
        }
        LOCALE_MANAGER!!.setApplicationLocales(packageName, currentUser, locales, true)
    }

    override fun setApplicationLocales(packageName: String?, locales: LocaleList?) {
        setAppLocalesInternal(packageName!!, locales)
    }

    override fun getApplicationLocales(packageName: String?): LocaleList {
        requiresLocaleManager()
        val currentUser = getCurrentUserId()
        return LOCALE_MANAGER!!.getApplicationLocales(packageName, currentUser)
    }

    override fun getSystemLocales(): LocaleList {
        requiresLocaleManager()
        return LOCALE_MANAGER!!.systemLocales
    }

    /**
     * Batch: return the language tag for each package (empty string = system default).
     * Single Binder round-trip instead of N calls.
     */
    override fun getApplicationLocaleTags(packageNames: Array<out String>?): Array<String> {
        requiresLocaleManager()
        val currentUser = getCurrentUserId()
        return (packageNames ?: emptyArray()).map { pkg ->
            val locales = try {
                LOCALE_MANAGER!!.getApplicationLocales(pkg, currentUser)
            } catch (e: Exception) {
                return@map ""
            }
            if (locales.isEmpty) "" else locales[0].toLanguageTag()
        }.toTypedArray()
    }

    /**
     * Batch: set the same [locales] to every package.
     * Returns the packages that failed.
     */
    override fun setApplicationLocalesForPackages(
        packageNames: Array<out String>?,
        locales: LocaleList?,
    ): Array<String> {
        val failed = mutableListOf<String>()
        for (pkg in packageNames ?: emptyArray()) {
            try {
                setAppLocalesInternal(pkg, locales)
            } catch (e: Exception) {
                Log.e(BuildConfig.APPLICATION_ID, "setApplicationLocales failed for $pkg", e)
                failed.add(pkg)
            }
        }
        return failed.toTypedArray()
    }

    // -------------------------------------------------------------- Activity

    var ACTIVITY_MANAGER: IActivityManager? = null
    fun requiresActivityManager() {
        if (ACTIVITY_MANAGER != null) return
        val am = SystemServiceHelper.getSystemService("activity")
        ACTIVITY_MANAGER = IActivityManager.Stub.asInterface(am)
    }

    override fun forceStopPackage(packageName: String?) {
        requiresActivityManager()
        val currentUser = getCurrentUserId()
        ACTIVITY_MANAGER!!.forceStopPackage(packageName, currentUser)
    }

    var ACTIVITY_TASK_MANAGER: IActivityTaskManager? = null
    fun requiresActivityTaskManager() {
        if (ACTIVITY_TASK_MANAGER != null) return
        val am = SystemServiceHelper.getSystemService("activity_task")
        ACTIVITY_TASK_MANAGER = IActivityTaskManager.Stub.asInterface(am)
    }

    override fun getFirstRunningTaskPackage(): String {
        requiresActivityTaskManager()
        val runningTask =
            try {
                ACTIVITY_TASK_MANAGER!!.getTasks(1, false, false, -1).first()
            } catch (e: NoSuchMethodError) {
                Log.w(
                    BuildConfig.APPLICATION_ID,
                    "getTasks failed, trying again without displayId, error: ${e.stackTraceToString()}"
                )
                ACTIVITY_TASK_MANAGER!!.getTasks(1, false, false).first()
            }
        return runningTask.topActivity?.packageName ?: ""
    }
}
