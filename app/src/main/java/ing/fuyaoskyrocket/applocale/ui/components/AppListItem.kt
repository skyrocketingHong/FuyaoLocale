package ing.fuyaoskyrocket.applocale.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.data.system.AppIconLoader
import ing.fuyaoskyrocket.applocale.model.AppModel
import ing.fuyaoskyrocket.applocale.ui.designsystem.LocalUserPreferences
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppListRow

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
    query: String = "",
) {
    val preferences = LocalUserPreferences.current
    AppListRow(
        title = app.label,
        subtitle = app.packageName.takeIf { preferences.showPackageNames },
        query = query,
        spacious = true,
        selected = isSelected,
        selectionMode = isSelectionMode,
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier,
        leading = { iconModifier -> AppIcon(app.packageName, iconLoader, iconModifier) },
        metaLabel = if (preferences.showAppTypes) stringResource(if (app.isSystemApp) R.string.system_app else R.string.user_app) else null,
        detail = { AppLocaleStatusBadge(app.localeTag, systemLocaleTag, emphasis = LocaleStatusEmphasis.Quiet) },
    )
}
