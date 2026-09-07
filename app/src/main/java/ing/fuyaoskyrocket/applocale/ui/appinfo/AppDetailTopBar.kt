package ing.fuyaoskyrocket.applocale.ui.appinfo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.data.system.AppIconLoader
import ing.fuyaoskyrocket.applocale.ui.components.AppIcon
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppDropdownItem
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppDropdownMenu
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSearchableTopAppBar
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSymbol
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSymbolVector
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppToolbarIconButton
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppTopAppBar

/**
 * One native toolbar in both themes. The back/search/menu actions are permanent;
 * only the identity crossfades: the title's alpha and the 28dp app icon animate
 * between the page title and the compact app name as the header scrolls away.
 */
@Composable
fun AppDetailTopBar(
    state: AppInfoUiState,
    iconLoader: AppIconLoader,
    collapsed: Boolean,
    searchExpanded: Boolean,
    query: String,
    onBack: () -> Unit,
    onOpenSearch: () -> Unit,
    onCloseSearch: () -> Unit,
    onQueryChange: (String) -> Unit,
    onOpen: () -> Unit,
    onForceStop: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember(state.packageName) { mutableStateOf(false) }
    val pageTitle = stringResource(R.string.app_language)
    // Reset per app: a new identity never inherits the previous app's fade.
    val titleAlpha = remember(state.packageName) { Animatable(1f) }
    var displayedCompact by remember(state.packageName) { mutableStateOf(collapsed) }
    var displayedTitle by remember(state.packageName) {
        mutableStateOf(if (collapsed) state.label else pageTitle)
    }

    // Interruptible swap: fade the title out, exchange it together with the
    // compact icon, fade back in. A retarget mid-flight cancels the old job and
    // continues from the current alpha; an unchanged target still restores a
    // partially faded title instead of stranding it below 1f.
    LaunchedEffect(collapsed, state.label, pageTitle) {
        val targetTitle = if (collapsed) state.label else pageTitle
        if (displayedCompact == collapsed && displayedTitle == targetTitle) {
            if (titleAlpha.value < 1f) {
                titleAlpha.animateTo(1f, tween(110))
            }
            return@LaunchedEffect
        }
        if (titleAlpha.value > 0f) {
            titleAlpha.animateTo(0f, tween(90))
        }
        displayedCompact = collapsed
        displayedTitle = targetTitle
        titleAlpha.animateTo(1f, tween(110))
    }

    AppSearchableTopAppBar(
        searchExpanded = searchExpanded,
        query = query,
        onCloseSearch = onCloseSearch,
        onQueryChange = onQueryChange,
        searchLabel = stringResource(R.string.search_languages),
        modifier = modifier,
    ) {
        AppTopAppBar(
            title = displayedTitle,
            titleAlpha = titleAlpha.value,
            modifier = modifier,
            navigationIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppToolbarIconButton(
                        icon = AppSymbolVector(AppSymbol.Back),
                        contentDescription = stringResource(R.string.back),
                        onClick = onBack,
                    )
                    // Identity decoration only; the loader reuses the same
                    // request as the header below, no second bitmap pipeline.
                    AnimatedVisibility(
                        visible = displayedCompact,
                        enter = fadeIn(tween(110)) + expandHorizontally(tween(110)),
                        exit = fadeOut(tween(90)) + shrinkHorizontally(tween(90)),
                    ) {
                        AppIcon(
                            packageName = state.packageName,
                            iconLoader = iconLoader,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }
            },
            actions = {
                AppToolbarIconButton(
                    icon = AppSymbolVector(AppSymbol.Search),
                    contentDescription = stringResource(R.string.search_languages),
                    onClick = onOpenSearch,
                    enabled = !state.isLoading,
                )
                Box {
                    AppToolbarIconButton(
                        icon = AppSymbolVector(AppSymbol.Menu),
                        contentDescription = stringResource(R.string.more_actions),
                        onClick = { menuExpanded = true },
                        enabled = !state.isLoading,
                    )
                    AppDropdownMenu(
                        expanded = menuExpanded,
                        onDismiss = { menuExpanded = false },
                        items = listOf(
                            AppDropdownItem(stringResource(R.string.open), onClick = { menuExpanded = false; onOpen() }),
                            AppDropdownItem(stringResource(R.string.close), onClick = { menuExpanded = false; onForceStop() }),
                            AppDropdownItem(stringResource(R.string.settings), onClick = { menuExpanded = false; onSettings() }),
                        ),
                    )
                }
            },
        )
    }
}
