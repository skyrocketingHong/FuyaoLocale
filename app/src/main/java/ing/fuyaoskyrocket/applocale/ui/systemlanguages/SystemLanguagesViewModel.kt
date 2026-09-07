package ing.fuyaoskyrocket.applocale.ui.systemlanguages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ing.fuyaoskyrocket.applocale.data.repository.LocaleRepository
import ing.fuyaoskyrocket.applocale.model.LocaleOption
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SystemLanguagesUiState(
    val locales: List<LocaleOption> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSaving: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
)

sealed interface SystemLanguagesEvent {
    data object Saved : SystemLanguagesEvent
    data object LoadFailed : SystemLanguagesEvent
    data object SaveFailed : SystemLanguagesEvent
    data object CannotRemoveLastLocale : SystemLanguagesEvent
    data object LocaleAlreadyAdded : SystemLanguagesEvent
    data object RefreshBlockedByDraft : SystemLanguagesEvent
}

/**
 * Keeps global system-locale edits local until the user explicitly saves them.
 * This prevents drag and add operations from repeatedly writing the device Configuration.
 */
@HiltViewModel
class SystemLanguagesViewModel @Inject constructor(
    private val localeRepository: LocaleRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SystemLanguagesUiState())
    val uiState: StateFlow<SystemLanguagesUiState> = _uiState.asStateFlow()

    private val _events = Channel<SystemLanguagesEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun load() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { localeRepository.getSystemLocaleOptions() }
                .onSuccess { locales ->
                    _uiState.value = SystemLanguagesUiState(locales = locales)
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.send(SystemLanguagesEvent.LoadFailed)
                }
        }
    }

    /**
     * Pull-to-refresh (round-5 019-B): never a second concurrent request, never
     * over an unsaved draft. A blocked refresh keeps the draft and says so; a
     * clean one keeps the current list on screen (isRefreshing, not isLoading)
     * and lands the fresh order only when the draft has not changed underneath.
     */
    fun refresh() {
        val state = _uiState.value
        when {
            state.isLoading || state.isRefreshing || state.isSaving -> return
            state.hasUnsavedChanges -> {
                viewModelScope.launch { _events.send(SystemLanguagesEvent.RefreshBlockedByDraft) }
            }

            else -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isRefreshing = true) }
                    runCatching { localeRepository.getSystemLocaleOptions() }
                        .onSuccess { locales ->
                            _uiState.update { current ->
                                if (current.hasUnsavedChanges) {
                                    current.copy(isRefreshing = false)
                                } else {
                                    current.copy(locales = locales, isRefreshing = false)
                                }
                            }
                        }
                        .onFailure {
                            _uiState.update { it.copy(isRefreshing = false) }
                            _events.send(SystemLanguagesEvent.LoadFailed)
                        }
                }
            }
        }
    }

    fun addLocale(locale: LocaleOption) {
        val state = _uiState.value
        if (state.isRefreshing) return
        if (state.locales.any { it.systemLocaleIdentity() == locale.systemLocaleIdentity() }) {
            viewModelScope.launch { _events.send(SystemLanguagesEvent.LocaleAlreadyAdded) }
            return
        }
        _uiState.update {
            it.copy(
                locales = it.locales + locale,
                hasUnsavedChanges = true,
            )
        }
    }

    fun removeLocale(locale: LocaleOption) {
        val state = _uiState.value
        if (state.isRefreshing) return
        if (state.locales.size <= 1) {
            viewModelScope.launch { _events.send(SystemLanguagesEvent.CannotRemoveLastLocale) }
            return
        }
        _uiState.update {
            it.copy(
                locales = it.locales.filterNot { option -> option.languageTag == locale.languageTag },
                hasUnsavedChanges = true,
            )
        }
    }

    fun moveLocale(fromIndex: Int, toIndex: Int) {
        val locales = _uiState.value.locales
        if (_uiState.value.isRefreshing) return
        if (fromIndex !in locales.indices || toIndex !in locales.indices || fromIndex == toIndex) return
        _uiState.update {
            it.copy(
                locales = it.locales.toMutableList().apply {
                    add(toIndex, removeAt(fromIndex))
                },
                hasUnsavedChanges = true,
            )
        }
    }

    fun moveLocaleByTag(sourceTag: String, targetTag: String) {
        val locales = _uiState.value.locales
        moveLocale(
            fromIndex = locales.indexOfFirst { it.languageTag == sourceTag },
            toIndex = locales.indexOfFirst { it.languageTag == targetTag },
        )
    }

    fun save() {
        val state = _uiState.value
        if (state.isSaving || state.isRefreshing || !state.hasUnsavedChanges || state.locales.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            runCatching { localeRepository.setSystemLocaleOptions(state.locales) }
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, hasUnsavedChanges = false) }
                    _events.send(SystemLanguagesEvent.Saved)
                }
                .onFailure {
                    _uiState.update { it.copy(isSaving = false) }
                    _events.send(SystemLanguagesEvent.SaveFailed)
                }
        }
    }
}

private fun LocaleOption.systemLocaleIdentity(): String = runCatching {
    Locale.Builder()
        .setLocale(Locale.forLanguageTag(languageTag))
        .clearExtensions()
        .build()
        .toLanguageTag()
        .lowercase(Locale.ROOT)
}.getOrDefault(languageTag.lowercase(Locale.ROOT))
