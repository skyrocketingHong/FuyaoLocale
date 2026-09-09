package ing.fuyaoskyrocket.applocale.ui.languagepicker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ing.fuyaoskyrocket.applocale.data.repository.LocaleRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Shared state holder for read-only locale selection surfaces.
 *
 * Batch application and system-language editing both use the same language directory, search,
 * section ordering, and sort behavior. Their parent surfaces own what a selected locale means.
 */
@HiltViewModel
class LocalePickerViewModel @Inject constructor(
    private val localeRepository: LocaleRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocalePickerUiState())
    val uiState: StateFlow<LocalePickerUiState> = _uiState.asStateFlow()
    private var displayLocaleTag: String = ""

    // Round-8 038: sessions isolate picker state between surfaces. A new
    // session key resets query/group/selection only; the loaded directory
    // stays, and the async loads above never overwrite the session's
    // selectedLanguageTag (they copy over it untouched).
    private var activeSessionKey: String? = null

    init {
        load()
    }

    /**
     * Starts a fresh selection session for a caller-owned surface (round-8
     * 038): query and drilled-in group reset and the selection seeds from the
     * caller. Calling twice with the same key is a no-op, so a config-change
     * recomposition does not clobber an open session.
     */
    fun beginSession(sessionKey: String, selectedLanguageTag: String?) {
        if (activeSessionKey == sessionKey) return
        activeSessionKey = sessionKey
        _uiState.update {
            it.copy(
                query = "",
                selectedGroupId = null,
                selectedLanguageTag = selectedLanguageTag,
            )
        }
    }

    private fun load() {
        viewModelScope.launch {
            val groups = localeRepository.getAllLocaleGroups()
            val system = localeRepository.getSystemLocaleOptions()
            val pinned = localeRepository.getPinnedLocales()
            displayLocaleTag = localeRepository.currentDisplayLocaleTag()
            _uiState.update {
                it.copy(
                    localeGroups = groups,
                    systemLocales = system,
                    pinnedLocales = pinned,
                    displayLocaleTag = displayLocaleTag,
                    isLoading = false,
                )
            }
        }
    }

    fun refreshDisplayLocale() {
        val currentDisplayLocaleTag = localeRepository.currentDisplayLocaleTag()
        if (_uiState.value.isLoading || currentDisplayLocaleTag == displayLocaleTag) return
        displayLocaleTag = currentDisplayLocaleTag
        viewModelScope.launch {
            val groups = localeRepository.getAllLocaleGroups()
            _uiState.update { current ->
                current.copy(
                    localeGroups = groups,
                    displayLocaleTag = currentDisplayLocaleTag,
                    systemLocales = localeRepository.localizeLocaleTags(
                        current.systemLocales.map { option -> option.languageTag },
                    ),
                    pinnedLocales = localeRepository.getPinnedLocales(),
                )
            }
        }
    }

    fun onAction(action: LocalePickerAction) {
        when (action) {
            is LocalePickerAction.QueryChanged -> {
                _uiState.update { it.copy(query = action.query) }
            }
            is LocalePickerAction.GroupOpened -> {
                _uiState.update { it.copy(selectedGroupId = action.groupId) }
            }
            LocalePickerAction.BackToGroups -> {
                _uiState.update { it.copy(selectedGroupId = null) }
            }
            is LocalePickerAction.CycleGroupSort -> {
                val (option, ascending) = cycledLanguageGroupSort(
                    currentOption = _uiState.value.groupSortOption,
                    currentAscending = _uiState.value.groupSortAscending,
                    tapped = action.option,
                )
                _uiState.update {
                    it.copy(groupSortOption = option, groupSortAscending = ascending)
                }
            }
            is LocalePickerAction.CycleVariantSort -> {
                val (option, ascending) = cycledLocaleVariantSort(
                    currentOption = _uiState.value.variantSortOption,
                    currentAscending = _uiState.value.variantSortAscending,
                    tapped = action.option,
                )
                _uiState.update {
                    it.copy(variantSortOption = option, variantSortAscending = ascending)
                }
            }
            is LocalePickerAction.SetGroupSort -> {
                _uiState.update {
                    it.copy(groupSortOption = action.option, groupSortAscending = action.ascending)
                }
            }
            is LocalePickerAction.SetVariantSort -> {
                _uiState.update {
                    it.copy(variantSortOption = action.option, variantSortAscending = action.ascending)
                }
            }
            is LocalePickerAction.PinClicked,
            is LocalePickerAction.UnpinClicked,
            is LocalePickerAction.LocaleSelected -> Unit
        }
    }
}
