package ing.fuyaoskyrocket.applocale.ui.configurations

import android.content.Context
import ing.fuyaoskyrocket.applocale.R

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
}
