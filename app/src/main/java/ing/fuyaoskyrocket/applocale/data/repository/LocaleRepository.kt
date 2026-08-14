package ing.fuyaoskyrocket.applocale.data.repository

import ing.fuyaoskyrocket.applocale.data.local.PinnedLocaleStore
import ing.fuyaoskyrocket.applocale.data.system.LocaleManager
import ing.fuyaoskyrocket.applocale.data.system.PrivilegedLocaleDataSource
import ing.fuyaoskyrocket.applocale.model.LocaleGroup
import ing.fuyaoskyrocket.applocale.model.LocaleOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocaleRepository @Inject constructor(
    private val localeManager: LocaleManager,
    private val localeDataSource: PrivilegedLocaleDataSource,
    private val pinnedStore: PinnedLocaleStore,
    private val appRepository: AppRepository,
) {
    /** Full directory of all available locales, grouped by language. */
    suspend fun getAllLocaleGroups(): List<LocaleGroup> = withContext(Dispatchers.Default) {
        localeManager.getLocaleGroups()
    }

    fun currentDisplayLocaleTag(): String = localeManager.currentDisplayLocaleTag()

    fun localizeLocaleTags(languageTags: List<String>): List<LocaleOption> = languageTags
        .map { tag -> localeManager.toOption(Locale.forLanguageTag(tag)) }
        .distinctBy(LocaleOption::languageTag)

    /** Current system locale options (shown under "User languages"). */
    suspend fun getSystemLocaleOptions(): List<LocaleOption> =
        withContext(Dispatchers.IO) {
            val systemLocales = localeDataSource.getSystemLocales()
            (0 until systemLocales.size()).map { i ->
                localeManager.toOption(systemLocales[i])
            }
        }

    /** Persisted pinned locales (tags only, display names generated at runtime). */
    fun getPinnedLocales(): List<LocaleOption> = localizeLocaleTags(pinnedStore.getPinnedTags())
        .sortedBy { option -> option.displayName.lowercase(Locale.ROOT) }

    fun pinLocale(option: LocaleOption) = pinnedStore.pin(option.languageTag)

    fun unpinLocale(option: LocaleOption) = pinnedStore.unpin(option.languageTag)

    /** Raw tags — used by LocaleQuickSettingsTile. */
    fun getPinnedTags(): List<String> = pinnedStore.getPinnedTags()

    suspend fun setAppLocale(packageName: String, localeTag: String?) =
        withContext(Dispatchers.IO) {
            val locales = if (localeTag.isNullOrBlank()) {
                android.os.LocaleList.getEmptyLocaleList()
            } else {
                android.os.LocaleList(java.util.Locale.forLanguageTag(localeTag))
            }
            localeDataSource.setApplicationLocales(packageName, locales)
            appRepository.updateCachedLocaleTags(mapOf(packageName to localeTag))
        }

    suspend fun getAppLocaleTag(packageName: String): String =
        withContext(Dispatchers.IO) {
            val locales = localeDataSource.getApplicationLocales(packageName)
            if (locales.isEmpty) "" else locales[0].toLanguageTag()
        }

    suspend fun forceStopPackage(packageName: String) =
        withContext(Dispatchers.IO) { localeDataSource.forceStopPackage(packageName) }
}
