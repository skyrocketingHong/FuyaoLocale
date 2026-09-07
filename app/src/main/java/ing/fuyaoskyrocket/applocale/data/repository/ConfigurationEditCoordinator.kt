package ing.fuyaoskyrocket.applocale.data.repository

import ing.fuyaoskyrocket.applocale.data.local.SavedLocaleConfigurationStore
import ing.fuyaoskyrocket.applocale.model.ConfigurationEditCommand
import ing.fuyaoskyrocket.applocale.model.ConfigurationEditPhase
import ing.fuyaoskyrocket.applocale.model.ConfigurationEditRejection
import ing.fuyaoskyrocket.applocale.model.ConfigurationEditState
import ing.fuyaoskyrocket.applocale.model.PendingConfigurationEdit
import ing.fuyaoskyrocket.applocale.model.SavedLocaleConfiguration
import ing.fuyaoskyrocket.applocale.model.SavedLocaleEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The I/O seam of [ConfigurationEditCoordinator] (round-8 038): one boundary
 * per external system the command touches — the Binder apply, the locale
 * cache signal, and the durable store. Production wires
 * [RealConfigurationEditEnvironment]; tests record real call counts.
 */
interface ConfigurationEditEnvironment {
    /** The tri-state read of a package's current per-app locale. */
    sealed interface TagRead {
        /** [tag] null means the system default — a definite answer. */
        data class Known(val tag: String?) : TagRead

        data object Unknown : TagRead
    }

    /** The apply step's verdict — [Unclear] covers Binder exceptions/timeouts. */
    sealed interface ApplyOutcome {
        data object Applied : ApplyOutcome
        data object Failed : ApplyOutcome
        data object Unclear : ApplyOutcome
    }

    /** Mirrors [ing.fuyaoskyrocket.applocale.data.local.FixedIdSaveResult]. */
    sealed interface SaveOutcome {
        data object Saved : SaveOutcome
        data object Identical : SaveOutcome
        data object Refused : SaveOutcome
        data object Failed : SaveOutcome
    }

    suspend fun findSourceConfiguration(id: String): SavedLocaleConfiguration?

    suspend fun isPackageInstalled(packageName: String): Boolean

    suspend fun readCurrentTag(packageName: String): TagRead

    suspend fun writePendingEdit(record: PendingConfigurationEdit): Boolean

    suspend fun applyLocale(packageName: String, localeTag: String?): ApplyOutcome

    suspend fun saveDerivedSnapshot(
        snapshot: SavedLocaleConfiguration,
        clearPendingOperationId: String,
    ): SaveOutcome

    suspend fun clearPendingEdit()

    fun readPendingEdit(): PendingConfigurationEdit?

    fun notifyLocaleChanged(packageName: String)

    /** Display label for a package that may not exist in the source entries. */
    suspend fun resolveLabel(packageName: String, fallback: String?): String
}

/**
 * The production environment: Binder apply through the existing single
 * package use case, the locale-change signal, and the serialized store.
 */
@Singleton
class RealConfigurationEditEnvironment @Inject constructor(
    private val store: SavedLocaleConfigurationStore,
    private val configurationsRepository: SavedLocaleConfigurationsRepository,
    private val appRepository: AppRepository,
    private val applyLocaleToApps: ApplyLocaleToAppsUseCase,
    private val localeChangeNotifier: LocaleChangeNotifier,
) : ConfigurationEditEnvironment {
    override suspend fun findSourceConfiguration(id: String): SavedLocaleConfiguration? =
        store.configurations.value.firstOrNull { it.id == id }

    override suspend fun isPackageInstalled(packageName: String): Boolean {
        appRepository.getCachedApps()?.let { apps ->
            if (apps.any { it.packageName == packageName }) return true
        }
        return try {
            appRepository.getApplicationInfo(packageName)
            true
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun readCurrentTag(packageName: String): ConfigurationEditEnvironment.TagRead =
        try {
            val tags = appRepository.fetchLocaleTags(listOf(packageName))
            if (packageName in tags) {
                ConfigurationEditEnvironment.TagRead.Known(
                    tags[packageName]?.takeIf(String::isNotBlank),
                )
            } else {
                ConfigurationEditEnvironment.TagRead.Unknown
            }
        } catch (_: Exception) {
            ConfigurationEditEnvironment.TagRead.Unknown
        }

    override suspend fun writePendingEdit(record: PendingConfigurationEdit): Boolean =
        configurationsRepository.writePendingEdit(record)

    override suspend fun applyLocale(
        packageName: String,
        localeTag: String?,
    ): ConfigurationEditEnvironment.ApplyOutcome = try {
        val result = applyLocaleToApps(listOf(packageName), localeTag)
        if (result.totalCount == 1 && result.failedPackages.isEmpty()) {
            ConfigurationEditEnvironment.ApplyOutcome.Applied
        } else {
            ConfigurationEditEnvironment.ApplyOutcome.Failed
        }
    } catch (_: Exception) {
        ConfigurationEditEnvironment.ApplyOutcome.Unclear
    }

    override suspend fun saveDerivedSnapshot(
        snapshot: SavedLocaleConfiguration,
        clearPendingOperationId: String,
    ): ConfigurationEditEnvironment.SaveOutcome = when (
        val result = configurationsRepository.saveWithFixedId(snapshot, clearPendingOperationId)
    ) {
        is ing.fuyaoskyrocket.applocale.data.local.FixedIdSaveResult.Saved ->
            ConfigurationEditEnvironment.SaveOutcome.Saved

        ing.fuyaoskyrocket.applocale.data.local.FixedIdSaveResult.Identical ->
            ConfigurationEditEnvironment.SaveOutcome.Identical

        ing.fuyaoskyrocket.applocale.data.local.FixedIdSaveResult.Refused ->
            ConfigurationEditEnvironment.SaveOutcome.Refused

        ing.fuyaoskyrocket.applocale.data.local.FixedIdSaveResult.Failed ->
            ConfigurationEditEnvironment.SaveOutcome.Failed
    }

    override suspend fun clearPendingEdit() {
        configurationsRepository.clearPendingEdit()
    }

    override fun readPendingEdit(): PendingConfigurationEdit? =
        configurationsRepository.readPendingEdit()

    override fun notifyLocaleChanged(packageName: String) {
        localeChangeNotifier.publish(listOf(packageName))
    }

    override suspend fun resolveLabel(packageName: String, fallback: String?): String =
        fallback ?: appRepository.getCachedApps()
            ?.firstOrNull { it.packageName == packageName }?.label
            ?: packageName
}

/** The post-read decision of one live edit (round-8 038 key table). */
internal sealed interface EditPlan {
    /** Current and source both already equal the target: no Binder, no new configuration. */
    data object NoChange : EditPlan

    /** Only management/target bookkeeping changed: save without a Binder call. */
    data object SaveOnly : EditPlan

    /** A real language change must be applied first; [previousTag] feeds the durable record. */
    data class ApplyAndSave(val previousTag: String?) : EditPlan
}

internal fun planEdit(
    source: SavedLocaleConfiguration,
    packageName: String,
    targetLocaleTag: String?,
    currentTag: String?,
): EditPlan {
    val sourceEntry = source.entries.firstOrNull { it.packageName == packageName }
    val localeNeedsChange = currentTag != targetLocaleTag
    // A MISSING entry is never "equivalent": unmanaged + default current +
    // explicit-default target is a real management change (save-only), not a
    // no-change outcome — only an EXISTING entry with the same tag is.
    val sourceEquivalent = sourceEntry != null && sourceEntry.localeTag == targetLocaleTag
    return when {
        !localeNeedsChange && sourceEquivalent -> EditPlan.NoChange
        localeNeedsChange -> EditPlan.ApplyAndSave(previousTag = currentTag)
        else -> EditPlan.SaveOnly
    }
}

/**
 * The derived snapshot (round-8 038): every source entry survives — including
 * uninstalled ones — only the target package is replaced or appended,
 * deduplicated by package. The operationId doubles as the new configuration
 * id; the createdAt stamp is chosen by the caller once and never regenerated
 * by retries.
 */
internal fun buildDerivedSnapshot(
    operationId: String,
    createdAt: Long,
    source: SavedLocaleConfiguration,
    packageName: String,
    label: String,
    targetLocaleTag: String?,
): SavedLocaleConfiguration = SavedLocaleConfiguration(
    id = operationId,
    createdAt = createdAt,
    entries = source.entries
        .filterNot { it.packageName == packageName } +
        SavedLocaleEntry(
            packageName = packageName,
            label = label,
            localeTag = targetLocaleTag,
        ),
)

/**
 * The application-level owner of the single "change one app's language, then
 * save a derived configuration" command (round-8 038).
 *
 * The command lives in an application-scoped [CoroutineScope] — it never rides
 * on a sheet's or a composable's lifetime and survives route replacement.
 * External apply and local persistence have no common transaction: the
 * prepared record is committed to disk BEFORE the Binder call, and the derived
 * configuration plus the record's removal share one store commit afterwards.
 *
 * Only one command may be active. The gate is claimed atomically BEFORE any
 * asynchronous launch (two rapid submissions cannot both pass an idle check),
 * and it stays held across the retryable outcomes — OutcomeUnknown and
 * AppliedPendingSave — until they are resolved or explicitly discarded, so no
 * later edit can silently overwrite a pending record.
 */
@Singleton
class ConfigurationEditCoordinator @Inject constructor(
    private val environment: ConfigurationEditEnvironment,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _state = MutableStateFlow<ConfigurationEditState>(ConfigurationEditState.Idle)
    val state: StateFlow<ConfigurationEditState> = _state.asStateFlow()

    private val gate = AtomicBoolean(false)

    // Guarded by the gate: the snapshot of the active or pending-save command.
    private var activeCommand: ConfigurationEditCommand? = null
    private var activeSnapshot: SavedLocaleConfiguration? = null

    init {
        recoverPending()
    }

    fun submit(
        sourceConfigurationId: String,
        packageName: String,
        targetLocaleTag: String?,
    ) {
        val command = ConfigurationEditCommand(
            operationId = UUID.randomUUID().toString(),
            sourceConfigurationId = sourceConfigurationId,
            packageName = packageName,
            targetLocaleTag = targetLocaleTag,
        )
        // Atomic admission: a second rapid tap sees the gate held and is
        // dropped before any coroutine can flip the busy flag. No Rejected
        // state is published here — overwriting a Running command or an
        // unresolved pending outcome would erase what the UI is showing.
        if (!gate.compareAndSet(false, true)) {
            return
        }
        activeCommand = command
        scope.launch { run(command) }
    }

    /** Retries ONLY the store save with the SAME id and snapshot — never the Binder call. */
    fun retrySave() {
        val state = _state.value
        if (state !is ConfigurationEditState.AppliedPendingSave) return
        val command = state.command
        val snapshot = activeSnapshot
        if (snapshot == null || snapshot.id != command.operationId) return
        scope.launch {
            saveDerived(command, snapshot, appliedLocaleChange = true)
        }
    }

    /** Re-runs the pending-record reconciliation against the real device state. */
    fun recheckPendingOutcome() {
        val state = _state.value
        if (state !is ConfigurationEditState.OutcomeUnknown &&
            state !is ConfigurationEditState.AppliedPendingSave
        ) {
            return
        }
        recoverPending()
    }

    /**
     * Explicitly releases an unresolved pending record (OutcomeUnknown or
     * AppliedPendingSave). The app language is NOT rolled back — the user may
     * have changed it elsewhere; only the local record is dropped.
     */
    fun discardPendingEdit() {
        val state = _state.value
        if (state !is ConfigurationEditState.OutcomeUnknown &&
            state !is ConfigurationEditState.AppliedPendingSave
        ) {
            return
        }
        if (!gate.compareAndSet(true, false)) {
            // A command is mid-flight (state raced); leave it alone.
            return
        }
        activeCommand = null
        activeSnapshot = null
        _state.value = ConfigurationEditState.Idle
        scope.launch { environment.clearPendingEdit() }
    }

    /** UI marks a terminal outcome as handled; the state returns to Idle. */
    fun acknowledge() {
        when (_state.value) {
            is ConfigurationEditState.Rejected,
            is ConfigurationEditState.ApplyFailed,
            is ConfigurationEditState.Completed,
            -> _state.value = ConfigurationEditState.Idle

            else -> Unit
        }
    }

    private suspend fun run(command: ConfigurationEditCommand) {
        publishRunning(command, ConfigurationEditPhase.Preparing)

        // ---- Validation: nothing below touches Binder when any check fails.
        val source = environment.findSourceConfiguration(command.sourceConfigurationId)
        if (source == null) {
            return finishFinal(
                ConfigurationEditState.Rejected(command, ConfigurationEditRejection.SourceMissing),
            )
        }
        if (!environment.isPackageInstalled(command.packageName)) {
            return finishFinal(
                ConfigurationEditState.Rejected(command, ConfigurationEditRejection.AppNotInstalled),
            )
        }
        if (command.targetLocaleTag != null && command.targetLocaleTag.isBlank()) {
            return finishFinal(
                ConfigurationEditState.Rejected(command, ConfigurationEditRejection.InvalidTarget),
            )
        }

        // ---- Read the app's real current language; a missing answer is
        // Unknown and never guessed as the system default.
        val currentRead = environment.readCurrentTag(command.packageName)
        if (currentRead !is ConfigurationEditEnvironment.TagRead.Known) {
            return finishFinal(
                ConfigurationEditState.Rejected(
                    command,
                    ConfigurationEditRejection.CurrentStateUnknown,
                ),
            )
        }

        val label = environment.resolveLabel(
            packageName = command.packageName,
            fallback = source.entries
                .firstOrNull { it.packageName == command.packageName }?.label,
        )
        val snapshot = buildDerivedSnapshot(
            operationId = command.operationId,
            createdAt = System.currentTimeMillis(),
            source = source,
            packageName = command.packageName,
            label = label,
            targetLocaleTag = command.targetLocaleTag,
        )
        activeSnapshot = snapshot

        when (
            val plan = planEdit(
                source = source,
                packageName = command.packageName,
                targetLocaleTag = command.targetLocaleTag,
                currentTag = currentRead.tag,
            )
        ) {
            EditPlan.NoChange -> return finishFinal(
                ConfigurationEditState.Completed(
                    command = command,
                    newConfigurationId = null,
                    appliedLocaleChange = false,
                ),
            )

            is EditPlan.ApplyAndSave -> {
                // ---- Prepared record on disk BEFORE the apply step.
                val prepared = PendingConfigurationEdit(
                    operationId = command.operationId,
                    sourceConfigurationId = command.sourceConfigurationId,
                    packageName = command.packageName,
                    previousLocaleTag = plan.previousTag,
                    targetLocaleTag = command.targetLocaleTag,
                    newConfiguration = snapshot,
                )
                if (!environment.writePendingEdit(prepared)) {
                    return finishFinal(
                        ConfigurationEditState.Rejected(
                            command,
                            ConfigurationEditRejection.PrepareWriteFailed,
                        ),
                    )
                }

                // ---- Apply to exactly this one package.
                publishRunning(command, ConfigurationEditPhase.Applying)
                var applied = false
                var unclear = false
                when (
                    environment.applyLocale(
                        packageName = command.packageName,
                        localeTag = command.targetLocaleTag,
                    )
                ) {
                    ConfigurationEditEnvironment.ApplyOutcome.Applied -> applied = true
                    ConfigurationEditEnvironment.ApplyOutcome.Failed -> Unit
                    ConfigurationEditEnvironment.ApplyOutcome.Unclear -> unclear = true
                }
                if (!applied) {
                    if (unclear) {
                        // The call may still have landed: re-read the real
                        // value once. Matching now completes the save WITHOUT
                        // claiming the original call succeeded; anything else
                        // is an unresolved outcome — the record stays, nothing
                        // auto-retries.
                        val reread = environment.readCurrentTag(command.packageName)
                        if (reread is ConfigurationEditEnvironment.TagRead.Known &&
                            reread.tag == command.targetLocaleTag
                        ) {
                            applied = true
                        } else {
                            _state.value = ConfigurationEditState.OutcomeUnknown(command)
                            return
                        }
                    } else {
                        // Explicit failure: this command did not change the
                        // language, so the prepared record is moot — clear it.
                        environment.clearPendingEdit()
                        return finishFinal(
                            ConfigurationEditState.ApplyFailed(command),
                        )
                    }
                }
                environment.notifyLocaleChanged(command.packageName)
            }

            EditPlan.SaveOnly -> Unit
        }

        // ---- Save the derived configuration (also the save-only path where
        // the language already matched but management/target changed).
        publishRunning(command, ConfigurationEditPhase.Saving)
        val appliedChange = currentRead.tag != command.targetLocaleTag
        saveDerived(command, snapshot, appliedLocaleChange = appliedChange)
    }

    private suspend fun saveDerived(
        command: ConfigurationEditCommand,
        snapshot: SavedLocaleConfiguration,
        appliedLocaleChange: Boolean,
    ) {
        when (environment.saveDerivedSnapshot(snapshot, command.operationId)) {
            ConfigurationEditEnvironment.SaveOutcome.Saved,
            ConfigurationEditEnvironment.SaveOutcome.Identical,
            -> finishFinal(
                ConfigurationEditState.Completed(
                    command = command,
                    newConfigurationId = snapshot.id,
                    appliedLocaleChange = appliedLocaleChange,
                ),
            )

            // The language may already be changed on the device: keep the
            // pending record and offer a save-only retry.
            ConfigurationEditEnvironment.SaveOutcome.Refused,
            ConfigurationEditEnvironment.SaveOutcome.Failed,
            -> _state.value = ConfigurationEditState.AppliedPendingSave(command)
        }
    }

    /**
     * Startup / recheck reconciliation of a durable pending record: if the
     * derived configuration already exists identically the save had landed;
     * if the app's real language still matches the target the save may be
     * completed; anything else surfaces for a human decision — nothing is
     * auto-applied or rolled back.
     */
    private fun recoverPending() {
        scope.launch {
            val pending = environment.readPendingEdit() ?: return@launch
            val existing = environment.findSourceConfiguration(pending.newConfiguration.id)
            if (existing != null && existing == pending.newConfiguration) {
                environment.clearPendingEdit()
                _state.value = ConfigurationEditState.Idle
                return@launch
            }
            // Hold the gate on the record while it is being resolved so no new
            // edit can overwrite it; a successful save releases it again.
            activeCommand = pending.asCommand()
            activeSnapshot = pending.newConfiguration
            gate.set(true)
            val current = environment.readCurrentTag(pending.packageName)
            if (current is ConfigurationEditEnvironment.TagRead.Known &&
                current.tag == pending.targetLocaleTag
            ) {
                saveDerived(pending.asCommand(), pending.newConfiguration, appliedLocaleChange = true)
            } else {
                // Unknown or mismatched: recheck or an explicit discard are the
                // only ways forward — the language itself is never rolled back.
                _state.value = ConfigurationEditState.OutcomeUnknown(pending.asCommand())
            }
        }
    }

    private fun publishRunning(command: ConfigurationEditCommand, phase: ConfigurationEditPhase) {
        _state.value = ConfigurationEditState.Running(command, phase)
    }

    private fun releaseGate() {
        activeCommand = null
        activeSnapshot = null
        gate.set(false)
    }

    private fun finishFinal(state: ConfigurationEditState) {
        releaseGate()
        _state.value = state
    }
}
