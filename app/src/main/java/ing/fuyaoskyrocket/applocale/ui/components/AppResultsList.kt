package ing.fuyaoskyrocket.applocale.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ing.fuyaoskyrocket.applocale.data.system.AppIconLoader
import ing.fuyaoskyrocket.applocale.model.AppModel
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppVerticalScrollBar
import ing.fuyaoskyrocket.applocale.ui.designsystem.readableContentWidth

/**
 * Reusable, keyed installed-app list. Filtering and sorting are deliberately
 * completed before this composable receives [apps], keeping scrolling cheap. The
 * rows are continuous — no inter-row card gaps; position feedback comes from the
 * scrollbar bound to the same [LazyListState]. [header] renders as the first
 * item (the home filter chips) so it scrolls with the directory under the top
 * chrome.
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
    query: String = "",
    state: LazyListState = rememberLazyListState(),
    header: (@Composable () -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(),
) {
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = state,
            modifier = Modifier.fillMaxHeight(),
            contentPadding = contentPadding,
        ) {
            if (header != null) {
                item(key = "list_header", contentType = "header") {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(modifier = Modifier.readableContentWidth()) {
                            header()
                        }
                    }
                }
            }
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
                    query = query,
                )
            }
        }
        AppVerticalScrollBar(
            listState = state,
            modifier = Modifier.align(Alignment.CenterEnd),
        )
    }
}
