package ing.fuyaoskyrocket.applocale.ui.configurations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ing.fuyaoskyrocket.applocale.data.repository.ApplySavedLocaleConfigurationUseCase
import ing.fuyaoskyrocket.applocale.data.repository.LocaleChangeNotifier
import ing.fuyaoskyrocket.applocale.data.repository.SavedLocaleConfigurationsRepository
import ing.fuyaoskyrocket.applocale.model.BatchLocaleResult
import ing.fuyaoskyrocket.applocale.model.ConfigurationImportResult
import ing.fuyaoskyrocket.applocale.model.SavedLocaleConfiguration
import ing.fuyaoskyrocket.applocale.model.SavedLocaleConfigurationComparison
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConfigurationsUiState(
    val configurations: List<SavedLocaleConfiguration> = emptyList(),
    val isSaving: Boolean = false,
    val isImporting: Boolean = false,
    val applyingConfigurationId: String? = null,
    val selectedConfigurationId: String? = null,
    val comparison: SavedLocaleConfigurationComparison? = null,
    val isComparing: Boolean = false,
)

sealed interface ConfigurationsEvent {
    data class Saved(val appCount: Int) : ConfigurationsEvent
    data object NothingToSave : ConfigurationsEvent
    data class Imported(val importedCount: Int, val replacedCount: Int) : ConfigurationsEvent
    data object ImportInvalid : ConfigurationsEvent
    data class Applied(val result: BatchLocaleResult) : ConfigurationsEvent
    data object Deleted : ConfigurationsEvent
    data object Exported : ConfigurationsEvent
    data object ExportFailed : ConfigurationsEvent
    data object Failed : ConfigurationsEvent
}

@HiltViewModel
class ConfigurationsViewModel @Inject constructor(
    private val configurationsRepository: SavedLocaleConfigurationsRepository,
    private val applySavedConfiguration: ApplySavedLocaleConfigurationUseCase,
    private val localeChangeNotifier: LocaleChangeNotifier,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfigurationsUiState())
    val uiState: StateFlow<ConfigurationsUiState> = _uiState.asStateFlow()

    private val _events = Channel<ConfigurationsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            configurationsRepository.configurations.collect { configurations ->
                _uiState.update { state ->
                    val selectedId = state.selectedConfigurationId
                        ?.takeIf { selected -> configurations.any { it.id == selected } }
                    state.copy(
                        configurations = configurations,
                        selectedConfigurationId = selectedId,
                        comparison = state.comparison?.takeIf {
                            it.configuration.id == selectedId
                        },
                    )
                }
            }
        }
    }

    fun saveCurrentChanges() {
        if (_uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val configuration = configurationsRepository.saveCurrentModifiedApps()
                _events.send(
                    if (configuration == null) {
                        ConfigurationsEvent.NothingToSave
                    } else {
                        ConfigurationsEvent.Saved(configuration.appCount)
                    },
                )
            } catch (_: Exception) {
                _events.send(ConfigurationsEvent.Failed)
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun apply(configurationId: String) {
        if (_uiState.value.applyingConfigurationId != null) return
        val configuration = _uiState.value.configurations.firstOrNull { it.id == configurationId } ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(applyingConfigurationId = configurationId) }
            try {
                val result = applySavedConfiguration(configuration)
                val changedPackages = configuration.entries
                    .map { it.packageName }
                    .filterNot { it in result.failedPackages }
                localeChangeNotifier.publish(changedPackages)
                _events.send(ConfigurationsEvent.Applied(result))
            } catch (_: Exception) {
                _events.send(ConfigurationsEvent.Failed)
            } finally {
                _uiState.update { it.copy(applyingConfigurationId = null) }
            }
            if (_uiState.value.selectedConfigurationId == configurationId) {
                refreshSelectedComparison()
            }
        }
    }

    fun importConfiguration(serialized: String) {
        if (_uiState.value.isImporting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true) }
            try {
                when (val result = configurationsRepository.importSerialized(serialized)) {
                    is ConfigurationImportResult.Success -> {
                        _events.send(
                            ConfigurationsEvent.Imported(
                                importedCount = result.importedCount,
                                replacedCount = result.replacedCount,
                            ),
                        )
                    }

                    ConfigurationImportResult.InvalidFormat -> {
                        _events.send(ConfigurationsEvent.ImportInvalid)
                    }
                }
            } catch (_: Exception) {
                _events.send(ConfigurationsEvent.ImportInvalid)
            } finally {
                _uiState.update { it.copy(isImporting = false) }
            }
        }
    }

    fun reportImportReadFailure() {
        viewModelScope.launch { _events.send(ConfigurationsEvent.ImportInvalid) }
    }

    fun serializeConfiguration(configurationId: String): String? =
        configurationsRepository.serializeConfiguration(configurationId)

    fun reportExportResult(success: Boolean) {
        viewModelScope.launch {
            _events.send(
                if (success) ConfigurationsEvent.Exported else ConfigurationsEvent.ExportFailed,
            )
        }
    }

    fun selectConfiguration(configurationId: String) {
        loadComparison(configurationId)
    }

    fun refreshSelectedComparison() {
        _uiState.value.selectedConfigurationId?.let(::loadComparison)
    }

    fun clearSelection() {
        _uiState.update {
            it.copy(
                selectedConfigurationId = null,
                comparison = null,
                isComparing = false,
            )
        }
    }

    private fun loadComparison(configurationId: String) {
        val configuration = configurationsRepository.findConfiguration(configurationId) ?: return
        _uiState.update {
            it.copy(
                selectedConfigurationId = configurationId,
                comparison = null,
                isComparing = true,
            )
        }

        viewModelScope.launch {
            try {
                val comparison = configurationsRepository.compare(configuration)
                _uiState.update { state ->
                    if (state.selectedConfigurationId == configurationId) {
                        state.copy(comparison = comparison, isComparing = false)
                    } else {
                        state
                    }
                }
            } catch (_: Exception) {
                _uiState.update { state ->
                    if (state.selectedConfigurationId == configurationId) {
                        state.copy(isComparing = false)
                    } else {
                        state
                    }
                }
                _events.send(ConfigurationsEvent.Failed)
            }
        }
    }

    fun delete(configurationId: String) {
        _uiState.update { state ->
            if (state.selectedConfigurationId == configurationId) {
                state.copy(selectedConfigurationId = null, comparison = null, isComparing = false)
            } else {
                state
            }
        }
        configurationsRepository.delete(configurationId)
        viewModelScope.launch { _events.send(ConfigurationsEvent.Deleted) }
    }
}
