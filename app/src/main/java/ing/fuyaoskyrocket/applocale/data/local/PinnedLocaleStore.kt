package ing.fuyaoskyrocket.applocale.data.local

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists pinned locale **language tags only** (per spec).
 * Display names are generated at runtime from the tag.
 */
@Singleton
class PinnedLocaleStore @Inject constructor(
    private val sp: SharedPreferences,
) {

    fun pin(languageTag: String) {
        val current = sp.getStringSet(KEY, emptySet()) ?: emptySet()
        sp.edit().putStringSet(KEY, current + languageTag).apply()
    }

    fun unpin(languageTag: String) {
        val current = sp.getStringSet(KEY, emptySet()) ?: emptySet()
        sp.edit().putStringSet(KEY, current - languageTag).apply()
    }

    /** Raw tag set — used by LocaleQuickSettingsTile which cycles through pinned locales. */
    fun getPinnedTags(): List<String> {
        val tags = sp.getStringSet(KEY, emptySet()) ?: emptySet()
        return tags.filter { it.isNotBlank() }.sorted()
    }

    companion object {
        const val KEY = "pinned_locales"
    }
}
