package ing.fuyaoskyrocket.applocale.ui.configurations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ing.fuyaoskyrocket.applocale.data.repository.ApplySavedLocaleConfigurationUseCase
import ing.fuyaoskyrocket.applocale.data.repository.ConfigurationEditCoordinator
import ing.fuyaoskyrocket.applocale.data.repository.LocaleChangeNotifier
import ing.fuyaoskyrocket.applocale.data.repository.SavedLocaleConfigurationsRepository
import ing.fuyaoskyrocket.applocale.data.system.AppIconLoader
import ing.fuyaoskyrocket.applocale.model.BatchLocaleResult
import ing.fuyaoskyrocket.applocale.model.ConfigurationAppProjection
import ing.fuyaoskyrocket.applocale.model.ConfigurationEditCommand
import ing.fuyaoskyrocket.applocale.model.ConfigurationEditPhase
import ing.fuyaoskyrocket.applocale.model.ConfigurationEditRejection
import ing.fuyaoskyrocket.applocale.model.ConfigurationEditState
import ing.fuyaoskyrocket.applocale.model.ConfigurationImportResult
import ing.fuyaoskyrocket.applocale.model.SavedLocaleConfiguration
import kotlinx.coroutines.Job
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
    /** The selected configuration's full app projection; null while loading or after a failure. */
    val detailRows: List<ConfigurationAppProjection>? = null,
    val isComparing: Boolean = false,
    /** Round-8 038: the live edit command's target row while it runs. */
    val editingPackageName: String? = null,
    val editingPhase: ConfigurationEditPhase? = null,
    /** An unresolved pending record (save failed or outcome unknown) blocking new edits. */
    val pendingEdit: ConfigurationEditCommand? = null,
    val pendingEditOutcomeUnknown: Boolean = false,
) {
    /** Any live or unresolved command: edit rows, apply and delete stand down. */
    val isEditBusy: Boolean
        get() = editingPackageName != null || pendingEdit != null
}

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

    /** Round-8 038 live-edit outcomes. */
    data class EditCompleted(
        val sourceConfigurationId: String,
        val packageName: String,
        val newConfigurationId: String?,
        val appliedLocaleChange: Boolean,
    ) : ConfigurationsEvent

    data class EditRejected(val reason: ConfigurationEditRejection) : ConfigurationsEvent
    data object EditApplyFailed : ConfigurationsEvent
    data object EditSaveFailed : ConfigurationsEvent
    data object EditOutcomeUnknown : ConfigurationsEvent
}

@HiltViewModel
class ConfigurationsViewModel @Inject constructor(
    private val configurationsRepository: SavedLocaleConfigurationsRepository,
    private val applySavedConfiguration: ApplySavedLocaleConfigurationUseCase,
    private val localeChangeNotifier: LocaleChangeNotifier,
    private val editCoordinator: ConfigurationEditCoordinator,
    val appIconLoader: AppIconLoader,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfigurationsUiState())
    val uiState: StateFlow<ConfigurationsUiState> = _uiState.asStateFlow()

    private val _events = Channel<ConfigurationsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    /**
     * Detail projection requests carry a generation (round-8 038): a stale job
     * for an older source configuration — or an older refresh of the same id —
     * can never overwrite a newer result.
     */
    private var detailJob: Job? = null
    private var detailGeneration = 0

    init {
        // The coordinator's app-level state drives this surface's mirrors and
        // one-shot events; terminal outcomes are acknowledged here exactly
        // once, so a rotation or route change never replays them.
        viewModelScope.launch {
            editCoordinator.state.collect { state ->
                when (state) {
                    is ConfigurationEditState.Running -> _uiState.update {
                        it.copy(
                            editingPackageName = state.command.packageName,
                            editingPhase = state.phase,
                            pendingEdit = null,
                            pendingEditOutcomeUnknown = false,
                        )
                    }

                    is ConfigurationEditState.Rejected -> {
                        _uiState.update { it.copy(editingPackageName = null, editingPhase = null) }
                        _events.send(ConfigurationsEvent.EditRejected(state.reason))
                        editCoordinator.acknowledge()
                    }

                    is ConfigurationEditState.ApplyFailed -> {
                        _uiState.update { it.copy(editingPackageName = null, editingPhase = null) }
                        _events.send(ConfigurationsEvent.EditApplyFailed)
                        editCoordinator.acknowledge()
                    }

                    is ConfigurationEditState.OutcomeUnknown -> _uiState.update {
                        it.copy(
                            editingPackageName = null,
                            editingPhase = null,
                            pendingEdit = state.command,
                            pendingEditOutcomeUnknown = true,
                        )
                    }

                    is ConfigurationEditState.AppliedPendingSave -> _uiState.update {
                        it.copy(
                            editingPackageName = null,
                            editingPhase = null,
                            pendingEdit = state.command,
                            pendingEditOutcomeUnknown = false,
                        )
                    }

                    is ConfigurationEditState.Completed -> {
                        _uiState.update {
                            it.copy(editingPackageName = null, editingPhase = null, pendingEdit = null)
                        }
                        _events.send(
                            ConfigurationsEvent.EditCompleted(
                                sourceConfigurationId = state.command.sourceConfigurationId,
                                packageName = state.command.packageName,
                                newConfigurationId = state.newConfigurationId,
                                appliedLocaleChange = state.appliedLocaleChange,
                            ),
                        )
                        // The wide pane's selection switches to the derived
                        // configuration here; the phone detail route follows
                        // from the event consumer.
                        state.newConfigurationId?.let(::loadDetail)
                        editCoordinator.acknowledge()
                    }

                    ConfigurationEditState.Idle -> _uiState.update {
                        it.copy(
                            editingPackageName = null,
                            editingPhase = null,
                            pendingEdit = null,
                            pendingEditOutcomeUnknown = false,
                        )
                    }
                }
            }
        }
        viewModelScope.launch {
            configurationsRepository.configurations.collect { configurations ->
                _uiState.update { state ->
                    val selectedId = state.selectedConfigurationId
                        ?.takeIf { selected -> configurations.any { it.id == selected } }
                    state.copy(
                        configurations = configurations,
                        selectedConfigurationId = selectedId,
                        detailRows = state.detailRows?.takeIf {
                            state.selectedConfigurationId == selectedId
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
        loadDetail(configurationId)
    }

    fun refreshSelectedComparison() {
        _uiState.value.selectedConfigurationId?.let(::loadDetail)
    }

    /**
     * Submits one live edit (round-8 038): the coordinator owns validation,
     * the Binder apply and the derived save; closing the sheet or a cancelled
     * gesture never reaches here.
     */
    fun submitEdit(sourceConfigurationId: String, packageName: String, targetLocaleTag: String?) {
        if (_uiState.value.isEditBusy) return
        editCoordinator.submit(
            sourceConfigurationId = sourceConfigurationId,
            packageName = packageName,
            targetLocaleTag = targetLocaleTag,
        )
    }

    fun retryPendingSave() {
        editCoordinator.retrySave()
    }

    fun recheckPendingEdit() {
        editCoordinator.recheckPendingOutcome()
    }

    fun discardPendingEdit() {
        editCoordinator.discardPendingEdit()
    }

    fun clearSelection() {
        detailJob?.cancel()
        _uiState.update {
            it.copy(
                selectedConfigurationId = null,
                detailRows = null,
                isComparing = false,
            )
        }
    }

    private fun loadDetail(configurationId: String) {
        val configuration = configurationsRepository.findConfiguration(configurationId) ?: return
        detailJob?.cancel()
        val generation = ++detailGeneration
        _uiState.update {
            it.copy(
                selectedConfigurationId = configurationId,
                detailRows = null,
                isComparing = true,
            )
        }

        detailJob = viewModelScope.launch {
            try {
                val rows = configurationsRepository.buildAppProjection(configuration)
                if (detailGeneration == generation) {
                    _uiState.update { state ->
                        if (state.selectedConfigurationId == configurationId) {
                            state.copy(detailRows = rows, isComparing = false)
                        } else {
                            state
                        }
                    }
                }
            } catch (_: Exception) {
                if (detailGeneration == generation) {
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
    }

    fun delete(configurationId: String) {
        // An unresolved edit command must not be overwritten by a deletion.
        if (_uiState.value.isEditBusy) return
        _uiState.update { state ->
            if (state.selectedConfigurationId == configurationId) {
                state.copy(selectedConfigurationId = null, detailRows = null, isComparing = false)
            } else {
                state
            }
        }
        viewModelScope.launch {
            try {
                configurationsRepository.delete(configurationId)
                _events.send(ConfigurationsEvent.Deleted)
            } catch (_: Exception) {
                _events.send(ConfigurationsEvent.Failed)
            }
        }
    }
}
