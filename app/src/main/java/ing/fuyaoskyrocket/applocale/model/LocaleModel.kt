package ing.fuyaoskyrocket.applocale.model

import java.util.Locale

/**
 * A single selectable locale option (e.g. "English (United States)").
 *
 * [languageTag] is the BCP-47 tag persisted everywhere (SharedPreferences, batch AIDL).
 * Display names are generated at runtime — never persisted.
 */
data class LocaleOption(
    val languageTag: String,
    /** Language autonym, such as `中文（中国）`. */
    val displayName: String,
    /** Base-language autonym, such as `中文`. */
    val languageName: String,
    val regionName: String?,
    /** Locale name translated into the current Fuyao Locale UI language. */
    val localizedDisplayName: String,
    /** Base-language name translated into the current Fuyao Locale UI language. */
    val localizedLanguageName: String,
) {
    companion object {
        /** Special marker representing "system default" (no per-app override). */
        val SYSTEM_DEFAULT: LocaleOption? = null
    }
}

/**
 * A group of locale options sharing the same base language
 * (e.g. "English" → English (US), English (UK), …).
 */
data class LocaleGroup(
    /** Stable BCP-47 base-language identifier, independent of the displayed language name. */
    val id: String,
    /** Base-language autonym shown as the primary row text. */
    val language: String,
    /** Base-language name translated into the current UI language. */
    val localizedLanguage: String,
    val options: List<LocaleOption>,
)

/** Convert a [Locale] to an option containing both its autonym and localized UI name. */
fun Locale.toOption(displayLocale: Locale): LocaleOption {
    val tag = toLanguageTag()
    return LocaleOption(
        languageTag = tag,
        displayName = getDisplayName(this).titlecaseFirst(this),
        languageName = getDisplayLanguage(this).titlecaseFirst(this),
        regionName = getDisplayCountry(this).takeIf { it.isNotEmpty() },
        localizedDisplayName = getDisplayName(displayLocale).titlecaseFirst(displayLocale),
        localizedLanguageName = getDisplayLanguage(displayLocale).titlecaseFirst(displayLocale),
    )
}

private fun String.titlecaseFirst(locale: Locale): String = replaceFirstChar { character ->
    if (character.isLowerCase()) character.titlecase(locale) else character.toString()
}
