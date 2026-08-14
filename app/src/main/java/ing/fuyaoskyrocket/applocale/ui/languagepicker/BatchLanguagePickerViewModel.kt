package ing.fuyaoskyrocket.applocale.ui.languagepicker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ing.fuyaoskyrocket.applocale.data.repository.LocaleRepository
import javax.inject.Inject

/**
 * ViewModel for the batch language selection [ModalBottomSheet][androidx.compose.material3.ModalBottomSheet].
 * Holds [LocalePickerUiState] so the Composable never touches [LocaleRepository] directly.
 * Pin/unpin are disabled in batch mode (no-ops).
 */
@HiltViewModel
class BatchLanguagePickerViewModel @Inject constructor(
    private val localeRepository: LocaleRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocalePickerUiState())
    val uiState: StateFlow<LocalePickerUiState> = _uiState.asStateFlow()
    private var displayLocaleTag: String = ""

    init {
        load()
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
            is LocalePickerAction.GroupSortChanged -> {
                _uiState.update {
                    it.copy(groupSortOption = action.option, groupSortAscending = true)
                }
            }
            LocalePickerAction.ToggleGroupSortDirection -> {
                _uiState.update { it.copy(groupSortAscending = !it.groupSortAscending) }
            }
            is LocalePickerAction.VariantSortChanged -> {
                _uiState.update {
                    it.copy(variantSortOption = action.option, variantSortAscending = true)
                }
            }
            LocalePickerAction.ToggleVariantSortDirection -> {
                _uiState.update { it.copy(variantSortAscending = !it.variantSortAscending) }
            }
            is LocalePickerAction.PinClicked,
            is LocalePickerAction.UnpinClicked -> {
                // Pin/unpin disabled in batch mode
            }
            is LocalePickerAction.LocaleSelected -> {
                // Handled by the parent — selection is communicated via callback
            }
        }
    }
}
