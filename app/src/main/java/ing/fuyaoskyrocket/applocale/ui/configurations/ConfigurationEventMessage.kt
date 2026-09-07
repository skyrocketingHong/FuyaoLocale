package ing.fuyaoskyrocket.applocale.ui.configurations

import android.content.Context
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.model.ConfigurationEditRejection

internal fun Context.configurationEventMessage(event: ConfigurationsEvent): String = when (event) {
    is ConfigurationsEvent.Saved -> getString(R.string.configuration_saved, event.appCount)
    ConfigurationsEvent.NothingToSave -> getString(R.string.no_modified_apps_to_save)
    is ConfigurationsEvent.Imported -> {
        if (event.replacedCount == 0) {
            getString(R.string.configuration_imported, event.importedCount)
        } else {
            getString(
                R.string.configuration_imported_with_replacements,
                event.importedCount,
                event.replacedCount,
            )
        }
    }

    ConfigurationsEvent.ImportInvalid -> getString(R.string.configuration_import_invalid)
    is ConfigurationsEvent.Applied -> {
        if (event.result.isAllSuccess) {
            getString(R.string.configuration_applied, event.result.successCount)
        } else {
            getString(
                R.string.configuration_apply_partial,
                event.result.successCount,
                event.result.failedPackages.size,
            )
        }
    }

    ConfigurationsEvent.Deleted -> getString(R.string.configuration_deleted)
    ConfigurationsEvent.Exported -> getString(R.string.configuration_exported)
    ConfigurationsEvent.ExportFailed -> getString(R.string.configuration_export_failed)
    ConfigurationsEvent.Failed -> getString(R.string.configuration_operation_failed)

    is ConfigurationsEvent.EditCompleted -> when {
        event.newConfigurationId == null ->
            getString(R.string.configuration_edit_no_change)

        event.appliedLocaleChange ->
            getString(R.string.configuration_edit_completed)

        else -> getString(R.string.configuration_edit_completed_save_only)
    }

    is ConfigurationsEvent.EditRejected -> when (event.reason) {
        ConfigurationEditRejection.Busy ->
            getString(R.string.configuration_edit_rejected_busy)

        ConfigurationEditRejection.AppNotInstalled ->
            getString(R.string.configuration_state_not_installed)

        ConfigurationEditRejection.CurrentStateUnknown ->
            getString(R.string.configuration_state_unknown)

        ConfigurationEditRejection.SourceMissing,
        ConfigurationEditRejection.InvalidTarget,
        ConfigurationEditRejection.PrepareWriteFailed,
        -> getString(R.string.configuration_operation_failed)
    }

    ConfigurationsEvent.EditApplyFailed -> getString(R.string.configuration_edit_apply_failed)
    ConfigurationsEvent.EditSaveFailed -> getString(R.string.configuration_edit_save_failed)
    ConfigurationsEvent.EditOutcomeUnknown -> getString(R.string.configuration_edit_outcome_unknown)
}
