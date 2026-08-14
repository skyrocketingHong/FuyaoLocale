package ing.fuyaoskyrocket.applocale.data.system

import android.app.Application
import ing.fuyaoskyrocket.applocale.model.LocaleGroup
import ing.fuyaoskyrocket.applocale.model.LocaleOption
import ing.fuyaoskyrocket.applocale.model.toOption
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Builds the full locale directory from [Locale.getAvailableLocales].
 *
 * Locale tags are normalized and grouped by their stable base-language code. Regional aliases
 * omitted by Android's maximized locale directory are derived in [expandedVariants].
 *
 * Canonical variants are built once; localized labels are cached per current UI locale.
 * [LocaleRepository] performs the expensive first build on a background dispatcher.
 */
@Singleton
class LocaleManager @Inject constructor(
    private val application: Application,
) {

    private val localeVariants: List<Locale> by lazy { buildLocaleVariants() }
    private var cachedDisplayLocaleTag: String? = null
    private var cachedLocaleGroups: List<LocaleGroup> = emptyList()

    @Synchronized
    fun getLocaleGroups(): List<LocaleGroup> {
        val displayLocale = currentDisplayLocale()
        val displayLocaleTag = displayLocale.toLanguageTag()
        if (displayLocaleTag != cachedDisplayLocaleTag) {
            cachedLocaleGroups = buildLocaleGroups(displayLocale)
            cachedDisplayLocaleTag = displayLocaleTag
        }
        return cachedLocaleGroups
    }

    fun toOption(locale: Locale): LocaleOption = locale.toOption(currentDisplayLocale())

    fun currentDisplayLocaleTag(): String = currentDisplayLocale().toLanguageTag()

    private fun currentDisplayLocale(): Locale {
        val locales = application.resources.configuration.locales
        return if (locales.isEmpty) Locale.getDefault() else locales[0]
    }

    private fun buildLocaleVariants(): List<Locale> {
        val localesByTag = linkedMapOf<String, Locale>()
        Locale.getAvailableLocales().forEach { locale ->
            locale.expandedVariants().forEach { variant ->
                val tag = variant.toLanguageTag()
                if (variant.language.isNotBlank() && tag.isNotBlank() && tag != UNDEFINED_TAG) {
                    localesByTag.putIfAbsent(tag, variant)
                }
            }
        }
        return localesByTag.values.toList()
    }

    private fun buildLocaleGroups(displayLocale: Locale): List<LocaleGroup> =
        localeVariants
            .groupBy { it.language.lowercase(Locale.ROOT) }
            .map { (languageId, locales) ->
                val options = locales
                    .map { locale -> locale.toOption(displayLocale) }
                    .distinctBy(LocaleOption::languageTag)
                    .sortedWith(
                        compareBy<LocaleOption>(
                            { option -> option.languageTag.count { it == '-' } },
                            { option -> option.displayName.lowercase(Locale.ROOT) },
                            LocaleOption::languageTag,
                        ),
                    )
                LocaleGroup(
                    id = languageId,
                    language = options.first().languageName,
                    localizedLanguage = options.first().localizedLanguageName,
                    options = options,
                )
            }
            .sortedBy { it.language.lowercase(Locale.ROOT) }

    /**
     * Android's locale directory often exposes only the maximized script form of a locale,
     * such as `zh-Hans-CN`. LocaleManager also accepts the canonical regional form (`zh-CN`),
     * so derive the useful language, script, region, and script-region forms from every locale.
     * This fills aliases without inventing region/language combinations absent from the system.
     */
    private fun Locale.expandedVariants(): Sequence<Locale> {
        val baseLanguage = language.takeIf(String::isNotBlank) ?: return emptySequence()
        val tags = linkedSetOf(toLanguageTag(), baseLanguage)

        script.takeIf(String::isNotBlank)?.let { scriptCode ->
            tags += "$baseLanguage-$scriptCode"
        }
        country.takeIf(String::isNotBlank)?.let { regionCode ->
            tags += "$baseLanguage-$regionCode"
            script.takeIf(String::isNotBlank)?.let { scriptCode ->
                tags += "$baseLanguage-$scriptCode-$regionCode"
            }
        }

        return tags.asSequence()
            .map(Locale::forLanguageTag)
            .filter { it.language.isNotBlank() && it.toLanguageTag() != UNDEFINED_TAG }
    }

    private companion object {
        const val UNDEFINED_TAG = "und"
    }
}
