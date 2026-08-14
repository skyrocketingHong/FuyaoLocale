package ing.fuyaoskyrocket.applocale.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.data.system.AppIconLoader
import ing.fuyaoskyrocket.applocale.model.AppModel
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppLayout

/**
 * M3 list row for an installed app.
 *
 * The leading icon, text baseline, and touch target are delegated to [ListItem]
 * so the compact and wide list panes maintain the same visual rhythm.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppListItem(
    app: AppModel,
    systemLocaleTag: String,
    iconLoader: AppIconLoader,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val localeText = app.localeTag ?: if (systemLocaleTag.isBlank()) {
        stringResource(R.string.system_default)
    } else {
        stringResource(R.string.system_default_with_locale, systemLocaleTag)
    }
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerLowest
    }
    val headlineColor = if (isSelected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val supportingColor = if (isSelected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = AppLayout.appListItemMinHeight)
            .clip(MaterialTheme.shapes.large)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .then(
                if (isSelectionMode) Modifier.semantics { role = Role.Checkbox } else Modifier,
            ),
        headlineContent = {
            Text(
                text = app.label,
                style = MaterialTheme.typography.titleMedium,
                color = headlineColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = supportingColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        leadingContent = {
            AppIcon(
                packageName = app.packageName,
                iconLoader = iconLoader,
                modifier = Modifier.size(AppLayout.appListIconSize),
            )
        },
        trailingContent = {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                )
            } else {
                Text(
                    text = if (app.isModified) {
                        "${stringResource(R.string.modified)} · $localeText"
                    } else {
                        localeText
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (app.isModified) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        supportingColor
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        colors = ListItemDefaults.colors(containerColor = containerColor),
    )
}
