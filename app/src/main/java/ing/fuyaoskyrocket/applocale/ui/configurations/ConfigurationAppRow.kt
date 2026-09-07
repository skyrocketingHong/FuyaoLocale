package ing.fuyaoskyrocket.applocale.ui.configurations

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.data.system.AppIconLoader
import ing.fuyaoskyrocket.applocale.model.ConfigurationAppProjection
import ing.fuyaoskyrocket.applocale.ui.components.AppIcon
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppComponentDefaults
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppLayout
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppCircularProgressIndicator
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSymbol
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSymbolVector
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppText
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppIcon as AppVectorIcon

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
    val shape = RoundedCornerShape(AppComponentDefaults.rowCornerRadius)
    val baseModifier = modifier
        .fillMaxWidth()
        .heightIn(min = AppLayout.localeChoiceRowMinHeight)
    val rowModifier = if (onEdit != null && row.isInstalled) {
        baseModifier
            .clip(shape)
            .clickable(onClick = onEdit)
    } else {
        baseModifier
    }

    Row(
        modifier = rowModifier
            .padding(
                start = AppSpacing.md,
                top = AppSpacing.md,
                end = 0.dp,
                bottom = AppSpacing.md,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(
            packageName = row.packageName,
            iconLoader = iconLoader,
            modifier = Modifier.size(40.dp),
        )
        Spacer(Modifier.width(AppSpacing.md))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            AppText(
                text = row.label,
                style = AppUiTheme.textStyles.itemTitle,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            AppText(
                text = row.packageName,
                style = AppUiTheme.textStyles.metadata,
                color = AppUiTheme.palette.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (isProcessing) {
                AppText(
                    text = stringResource(R.string.configuration_edit_processing),
                    style = AppUiTheme.textStyles.label,
                    color = AppUiTheme.palette.muted,
                )
            } else {
                ConfigurationAppStatusText(
                    row = row,
                    systemLocaleTag = systemLocaleTag,
                )
            }
        }
        Box(
            modifier = Modifier.size(AppLayout.appListSelectionSlotWidth),
            contentAlignment = Alignment.Center,
        ) {
            when {
                isProcessing -> AppCircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = AppSpacing.xs / 2,
                )

                onEdit != null && row.isInstalled -> AppVectorIcon(
                    imageVector = AppSymbolVector(AppSymbol.Forward),
                    contentDescription = null,
                    tint = AppUiTheme.palette.muted,
                    modifier = Modifier.size(24.dp),
                )

                !row.isInstalled -> AppVectorIcon(
                    // No miuix error glyph; the project vector stays an asset
                    // exception drawn by the backend icon control.
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = stringResource(R.string.configuration_state_not_installed),
                    tint = AppUiTheme.palette.muted,
                    modifier = Modifier.size(24.dp),
                )

                else -> Unit
            }
        }
    }
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
