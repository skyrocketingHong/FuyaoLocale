package ing.fuyaoskyrocket.applocale.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ing.fuyaoskyrocket.applocale.data.system.AppIconLoader
import ing.fuyaoskyrocket.applocale.model.AppModel
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing

/**
 * Reusable, keyed installed-app list. Filtering and sorting are deliberately
 * completed before this composable receives [apps], keeping scrolling cheap.
 */
@Composable
fun AppResultsList(
    apps: List<AppModel>,
    systemLocaleTag: String,
    iconLoader: AppIconLoader,
    selectedPackages: Set<String>,
    isSelectionMode: Boolean,
    onAppClick: (AppModel) -> Unit,
    onAppLongClick: (AppModel) -> Unit,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    LazyColumn(
        state = state,
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        items(
            items = apps,
            key = { it.packageName },
            contentType = { "app" },
        ) { app ->
            AppListItem(
                app = app,
                systemLocaleTag = systemLocaleTag,
                iconLoader = iconLoader,
                isSelected = app.packageName in selectedPackages,
                isSelectionMode = isSelectionMode,
                onClick = { onAppClick(app) },
                onLongClick = { onAppLongClick(app) },
            )
        }
    }
}
