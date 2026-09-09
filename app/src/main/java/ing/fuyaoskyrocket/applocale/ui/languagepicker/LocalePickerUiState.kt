package ing.fuyaoskyrocket.applocale.ui.languagepicker

import ing.fuyaoskyrocket.applocale.model.LocaleGroup
import ing.fuyaoskyrocket.applocale.model.LocaleOption

/**
 * Immutable state for the unified language picker.
 * Used by both [ing.fuyaoskyrocket.applocale.ui.appinfo.AppInfoViewModel] and
 * [LocalePickerViewModel].
 *
 * [selectedGroupId] is the BCP-47 base-language ID of the drilled-into group,
 * or `null` when showing the top-level group list.
 */
data class LocalePickerUiState(
    val query: String = "",
    val selectedGroupId: String? = null,
    val pinnedLocales: List<LocaleOption> = emptyList(),
    val systemLocales: List<LocaleOption> = emptyList(),
    val localeGroups: List<LocaleGroup> = emptyList(),
    val selectedLanguageTag: String? = null,
    val displayLocaleTag: String = "",
    val groupSortOption: LanguageGroupSortOption = LanguageGroupSortOption.Recommended,
    val groupSortAscending: Boolean = true,
    val variantSortOption: LocaleVariantSortOption = LocaleVariantSortOption.Recommended,
    val variantSortAscending: Boolean = true,
    val isLoading: Boolean = true,
) {
    val isInGroup: Boolean get() = selectedGroupId != null

    /** The [LocaleGroup] currently drilled into, or `null`. */
    val activeGroup: LocaleGroup?
        get() = selectedGroupId?.let { id -> localeGroups.find { it.id == id } }
}

enum class LanguageGroupSortOption {
    Recommended,
    LocalizedName,
    NativeName,
    LanguageTag,
    VariantCount,
}

enum class LocaleVariantSortOption {
    Recommended,
    LocalizedName,
    NativeName,
    LanguageTag,
    Specificity,
}

/**
 * UI actions dispatched from the language picker composable to the ViewModel.
 */
sealed interface LocalePickerAction {
    data class QueryChanged(val query: String) : LocalePickerAction
    data class GroupOpened(val groupId: String) : LocalePickerAction
    data object BackToGroups : LocalePickerAction

    /** Tri-state sort cycling from the single-row chips (round-5 019-A2). */
    data class CycleGroupSort(val option: LanguageGroupSortOption) : LocalePickerAction
    data class CycleVariantSort(val option: LocaleVariantSortOption) : LocalePickerAction

    /**
     * One-shot explicit sort target (round-9 044): the Holo spinner menu sets
     * the option AND direction in a single update — never by replaying cycles.
     */
    data class SetGroupSort(
        val option: LanguageGroupSortOption,
        val ascending: Boolean,
    ) : LocalePickerAction

    data class SetVariantSort(
        val option: LocaleVariantSortOption,
        val ascending: Boolean,
    ) : LocalePickerAction
    data class LocaleSelected(val option: LocaleOption) : LocalePickerAction
    data class PinClicked(val option: LocaleOption) : LocalePickerAction
    data class UnpinClicked(val option: LocaleOption) : LocalePickerAction
}

/**
 * One-time events emitted by the picker ViewModels (e.g. for Snackbar messages).
 */
sealed interface LocalePickerEvent {
    data class Pinned(val displayName: String) : LocalePickerEvent
    data class Unpinned(val displayName: String) : LocalePickerEvent
}
