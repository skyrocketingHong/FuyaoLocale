package ing.fuyaoskyrocket.applocale.model

/**
 * A reusable snapshot of the non-default per-app locales chosen by the user.
 *
 * Only modified apps are captured, so applying a configuration never resets an
 * unrelated app back to the system default.
 */
data class SavedLocaleConfiguration(
    val id: String,
    val createdAt: Long,
    val entries: List<SavedLocaleEntry>,
) {
    val appCount: Int
        get() = entries.size
}

data class SavedLocaleEntry(
    val packageName: String,
    val label: String,
    /**
     * The explicit target, or null for an EXPLICIT follow-system entry
     * (round-8 038). Whether the configuration manages a package at all is
     * decided by the entry's PRESENCE, never by this tag: a null tag means
     * "reset this listed app to the system default", while a missing entry
     * means "leave this app alone".
     */
    val localeTag: String?,
)

/** Result of importing the same portable JSON array used by local storage. */
sealed interface ConfigurationImportResult {
    data class Success(
        val importedCount: Int,
        val replacedCount: Int,
    ) : ConfigurationImportResult

    data object InvalidFormat : ConfigurationImportResult
}

/** Which part of a configuration detail projection a row belongs to (round-8 037). */
enum class ConfigurationAppSection {
    /** Explicitly listed in the configuration — including already-matching entries. */
    Managed,

    /** Not listed, but currently using a custom per-app locale. */
    ExternalModified,

    /** Not listed and currently on the system default — collapsed "other apps". */
    OtherApps,
}

/**
 * One app row of the editable configuration detail (round-8 037/038).
 *
 * Unknown state and unmanaged state are SEPARATE booleans — a nullable tag never
 * carries three meanings at once:
 * - [isCurrentKnown] false means the device did not report this package's locale
 *   (shown as unreadable, never as the system default);
 * - [currentLocaleTag] null with [isCurrentKnown] true IS the system default;
 * - [isManaged] false means the configuration does not manage this package at all.
 */
data class ConfigurationAppProjection(
    val packageName: String,
    val label: String,
    val section: ConfigurationAppSection,
    val isInstalled: Boolean,
    val isCurrentKnown: Boolean,
    val currentLocaleTag: String?,
    val isManaged: Boolean,
    val savedLocaleTag: String?,
) {
    /**
     * Whether applying this configuration would change something for this row:
     * a missing app, or a managed target that demonstrably differs from the
     * current state. Unreadable current state is never guessed as a difference.
     */
    val needsAttention: Boolean
        get() = when {
            !isInstalled -> true
            !isCurrentKnown -> false
            !isManaged -> false
            else -> currentLocaleTag != savedLocaleTag
        }
}

/** One live "change this app's language, then save a derived configuration" command (round-8 038). */
data class ConfigurationEditCommand(
    /** Also the id of the derived configuration; generated once per command, reused by retries. */
    val operationId: String,
    val sourceConfigurationId: String,
    val packageName: String,
    /** The chosen target, or null for an explicit follow-system action. */
    val targetLocaleTag: String?,
)

enum class ConfigurationEditPhase { Preparing, Applying, Saving }

/** Why a command was refused before any Binder call. */
enum class ConfigurationEditRejection {
    /** Another command is active or an unresolved pending record exists. */
    Busy,
    SourceMissing,
    AppNotInstalled,
    InvalidTarget,
    /** The device did not report the app's current language — never treated as default. */
    CurrentStateUnknown,
    /** The pre-apply prepared record could not be persisted; nothing was applied. */
    PrepareWriteFailed,
}

/**
 * The coordinator's single-command state machine (round-8 038). The
 * retryable outcomes (OutcomeUnknown, AppliedPendingSave) keep the command
 * gate held until resolved; the others release it.
 */
sealed interface ConfigurationEditState {
    data object Idle : ConfigurationEditState
    data class Running(
        val command: ConfigurationEditCommand,
        val phase: ConfigurationEditPhase,
    ) : ConfigurationEditState

    data class Rejected(
        val command: ConfigurationEditCommand,
        val reason: ConfigurationEditRejection,
    ) : ConfigurationEditState

    /** The apply step failed; the app language was NOT changed by this command. */
    data class ApplyFailed(val command: ConfigurationEditCommand) : ConfigurationEditState

    /** The Binder result is unclear; nothing is saved and nothing is auto-retried. */
    data class OutcomeUnknown(val command: ConfigurationEditCommand) : ConfigurationEditState

    /** The language change landed but the derived configuration is not on disk yet. */
    data class AppliedPendingSave(val command: ConfigurationEditCommand) : ConfigurationEditState

    /**
     * Done. [newConfigurationId] is null for a no-change outcome (nothing was
     * applied or saved); [appliedLocaleChange] distinguishes a real language
     * change from a save-only management change.
     */
    data class Completed(
        val command: ConfigurationEditCommand,
        val newConfigurationId: String?,
        val appliedLocaleChange: Boolean,
    ) : ConfigurationEditState
}

/**
 * The durable record of a command that has applied (or is applying) a real
 * language change but has not yet persisted its derived configuration. Written
 * BEFORE the Binder call; cleared in the SAME commit that inserts the derived
 * configuration (round-8 038).
 */
data class PendingConfigurationEdit(
    val operationId: String,
    val sourceConfigurationId: String,
    val packageName: String,
    /** The app's language before the apply step (null = system default). */
    val previousLocaleTag: String?,
    val targetLocaleTag: String?,
    val newConfiguration: SavedLocaleConfiguration,
) {
    fun asCommand(): ConfigurationEditCommand = ConfigurationEditCommand(
        operationId = operationId,
        sourceConfigurationId = sourceConfigurationId,
        packageName = packageName,
        targetLocaleTag = targetLocaleTag,
    )
}
