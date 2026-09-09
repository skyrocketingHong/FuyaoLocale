package ing.fuyaoskyrocket.applocale.ui.configurations

import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppListRow
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppRowAffordance
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.data.system.AppIconLoader
import ing.fuyaoskyrocket.applocale.model.ConfigurationAppProjection
import ing.fuyaoskyrocket.applocale.ui.components.AppIcon
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.LocalUserPreferences
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppText

/**
 * One continuous app row of the configuration detail (round-8 037): a single
 * layer of information on the page background — no card shell, no nested
 * comparison surface. State is carried by ONE short status line (unmanaged /
 * follow-system / unreadable / not installed are all distinct), the comparison
 * itself is plain text, and heights grow with the content.
 *
 * The trailing 48dp slot keeps a 24dp glyph. [onEdit] is provided only when a
 * real action exists (the live edit path, 038); an affordance that does
 * nothing is never rendered, so rows without an action show no chevron.
 */
@Composable
internal fun ConfigurationAppRow(
    row: ConfigurationAppProjection,
    systemLocaleTag: String,
    iconLoader: AppIconLoader,
    onEdit: (() -> Unit)?,
    modifier: Modifier = Modifier,
    isProcessing: Boolean = false,
) {
    AppListRow(
        title = row.label,
        subtitle = row.packageName.takeIf { LocalUserPreferences.current.showPackageNames },
        onClick = onEdit.takeIf { row.isInstalled },
        modifier = modifier,
        leadingSize = 40.dp,
        leading = { iconModifier -> AppIcon(row.packageName, iconLoader, iconModifier) },
        detail = {
            if (isProcessing) AppText(stringResource(R.string.configuration_edit_processing),
                style = AppUiTheme.textStyles.label, color = AppUiTheme.palette.muted)
            else ConfigurationAppStatusText(row, systemLocaleTag)
        },
        trailing = { AppRowAffordance(processing = isProcessing, navigable = onEdit != null && row.isInstalled) },
    )
}

/** The single status line: never collapses unmanaged, default, unknown or missing into one label. */
@Composable
private fun ConfigurationAppStatusText(
    row: ConfigurationAppProjection,
    systemLocaleTag: String,
) {
    val muted = AppUiTheme.palette.muted
    when {
        !row.isInstalled -> {
            // Keep the configured target visible even when the app is gone.
            val target = row.savedLocaleTag?.let { localeLabel(it, systemLocaleTag) }
            AppText(
                text = stringResource(R.string.configuration_state_not_installed) +
                    (target?.let { " · $it" } ?: ""),
                style = AppUiTheme.textStyles.label,
                color = muted,
            )
        }

        !row.isCurrentKnown -> AppText(
            text = stringResource(R.string.configuration_state_unknown),
            style = AppUiTheme.textStyles.label,
            color = muted,
        )

        !row.isManaged -> AppText(
            text = stringResource(
                R.string.configuration_current_only_line,
                localeLabel(row.currentLocaleTag, systemLocaleTag),
            ) + " · " + stringResource(R.string.difference_current_only),
            style = AppUiTheme.textStyles.label,
            color = muted,
        )

        else -> AppText(
            text = stringResource(
                R.string.configuration_current_to_target,
                localeLabel(row.currentLocaleTag, systemLocaleTag),
                localeLabel(row.savedLocaleTag, systemLocaleTag),
            ),
            style = AppUiTheme.textStyles.label,
            color = if (row.needsAttention) {
                AppUiTheme.palette.surfaceContent
            } else {
                muted
            },
        )
    }
}

/** "Follow system" carries the real system tag when the device reports one. */
@Composable
private fun localeLabel(tag: String?, systemLocaleTag: String): String =
    if (tag == null) {
        if (systemLocaleTag.isBlank()) {
            stringResource(R.string.system_default)
        } else {
            stringResource(R.string.system_default_with_locale, systemLocaleTag)
        }
    } else {
        tag
    }
