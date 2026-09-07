package ing.fuyaoskyrocket.applocale.ui.languagepicker

import ing.fuyaoskyrocket.applocale.model.LocaleGroup
import ing.fuyaoskyrocket.applocale.model.LocaleOption
import java.text.Collator
import java.util.Locale

internal fun LocalePickerUiState.sortedLanguageGroups(
    systemLocaleTag: String,
): List<LocaleGroup> {    val collator = displayCollator(displayLocaleTag.ifBlank { systemLocaleTag })
    val localizedName = comparatorBy(collator, LocaleGroup::localizedLanguage, LocaleGroup::id)
    val baseComparator = when (groupSortOption) {
        LanguageGroupSortOption.Recommended,
        LanguageGroupSortOption.LocalizedName -> localizedName
        LanguageGroupSortOption.NativeName ->
            comparatorBy(collator, LocaleGroup::language, LocaleGroup::id)
        LanguageGroupSortOption.LanguageTag ->
            compareBy(String.CASE_INSENSITIVE_ORDER, LocaleGroup::id)
        LanguageGroupSortOption.VariantCount ->
            compareBy<LocaleGroup> { it.options.size }.then(localizedName)
    }.inDirection(groupSortAscending)

    val currentLanguageId = effectiveLanguageTag(systemLocaleTag)
        .let(Locale::forLanguageTag)
        .language
        .lowercase(Locale.ROOT)
        .takeIf(String::isNotBlank)

    val comparator = if (groupSortOption == LanguageGroupSortOption.Recommended) {
        prioritize(baseComparator) { group -> group.id == currentLanguageId }
    } else {
        baseComparator
    }
    return localeGroups.sortedWith(comparator)
}

internal fun LocalePickerUiState.sortedLocaleVariants(
    options: List<LocaleOption>,
    systemLocaleTag: String,
): List<LocaleOption> {
    val collator = displayCollator(displayLocaleTag.ifBlank { systemLocaleTag })
    val localizedName = comparatorBy(
        collator,
        LocaleOption::localizedDisplayName,
        LocaleOption::languageTag,
    )
    val baseComparator = when (variantSortOption) {
        LocaleVariantSortOption.Recommended,
        LocaleVariantSortOption.LocalizedName -> localizedName
        LocaleVariantSortOption.NativeName ->
            comparatorBy(collator, LocaleOption::displayName, LocaleOption::languageTag)
        LocaleVariantSortOption.LanguageTag ->
            compareBy(String.CASE_INSENSITIVE_ORDER, LocaleOption::languageTag)
        LocaleVariantSortOption.Specificity ->
            compareBy<LocaleOption> { option -> option.languageTag.count { it == '-' } }
                .then(localizedName)
    }.inDirection(variantSortAscending)

    val currentTag = effectiveLanguageTag(systemLocaleTag)
    val comparator = if (variantSortOption == LocaleVariantSortOption.Recommended) {
        prioritize(baseComparator) { option ->
            option.languageTag.equals(currentTag, ignoreCase = true)
        }
    } else {
        baseComparator
    }
    return options.sortedWith(comparator)
}

private fun LocalePickerUiState.effectiveLanguageTag(systemLocaleTag: String): String =
    selectedLanguageTag?.takeIf(String::isNotBlank) ?: systemLocaleTag

private fun displayCollator(displayLocaleTag: String): Collator =
    Collator.getInstance(Locale.forLanguageTag(displayLocaleTag))

private fun <T> comparatorBy(
    collator: Collator,
    name: (T) -> String,
    tieBreaker: (T) -> String,
): Comparator<T> = Comparator { left, right ->
    collator.compare(name(left), name(right)).takeIf { it != 0 }
        ?: String.CASE_INSENSITIVE_ORDER.compare(tieBreaker(left), tieBreaker(right))
}

private fun <T> Comparator<T>.inDirection(ascending: Boolean): Comparator<T> =
    if (ascending) this else reversed()

private fun <T> prioritize(
    comparator: Comparator<T>,
    predicate: (T) -> Boolean,
): Comparator<T> = Comparator { left, right ->
    val priority = when {
        predicate(left) == predicate(right) -> 0
        predicate(left) -> -1
        else -> 1
    }
    if (priority != 0) priority else comparator.compare(left, right)
}

/**
 * The shared tri-state sort cycle (round-5 019-A2): tapping an unselected
 * option selects it ascending; tapping it again flips to descending; a third
 * tap returns to the Recommended baseline with all chips unselected. One
 * atomic transition per event — never a sort change plus an async direction
 * flip. Both picker ViewModels run their levels through these functions so
 * the two surfaces can never diverge.
 */
internal fun cycledLanguageGroupSort(
    currentOption: LanguageGroupSortOption,
    currentAscending: Boolean,
    tapped: LanguageGroupSortOption,
): Pair<LanguageGroupSortOption, Boolean> = when {
    tapped != currentOption -> tapped to true
    currentAscending -> tapped to false
    else -> LanguageGroupSortOption.Recommended to true
}

internal fun cycledLocaleVariantSort(
    currentOption: LocaleVariantSortOption,
    currentAscending: Boolean,
    tapped: LocaleVariantSortOption,
): Pair<LocaleVariantSortOption, Boolean> = when {
    tapped != currentOption -> tapped to true
    currentAscending -> tapped to false
    else -> LocaleVariantSortOption.Recommended to true
}
