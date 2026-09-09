package ing.fuyaoskyrocket.applocale.ui.designsystem.component

import ing.fuyaoskyrocket.applocale.ui.designsystem.lollipop.*
import ing.fuyaoskyrocket.applocale.ui.designsystem.eclair.*
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppControlFamily
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloActionBar
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloActionBarTitle

/** Compact top bar content height, following the FuyaoColorPicker app bar design. */
private val CompactAppBarHeight = 48.dp

/** The miuix small app bar's fixed centre height; kept in sync with its layout token. */
private val MiuixAppBarHeight = 50.dp

/** The Material search capsule's standard height; it grows with large font scales. */
private val SearchCapsuleHeight = 40.dp

/**
 * The top inset the compact bars reserve: the actual status-bar height (safeDrawing's
 * top can be taller on cutout devices), while horizontal cutouts stay protected.
 * The WindowInsets factories are composable, so this stays a composable getter.
 */
private val CompactAppBarInsets: WindowInsets
    @Composable
    get() = WindowInsets.statusBars.only(WindowInsetsSides.Top)
        .union(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))

/**
 * The chrome container colour with the bar's own background yielding to the
 * scaffold's sampling layer by live progress — never by the effect's target
 * boolean, so the exit fade cannot be suddenly covered by an opaque bar.
 */
@Composable
private fun chromeContainerColor(base: androidx.compose.ui.graphics.Color): androidx.compose.ui.graphics.Color {
    val backdrop = ing.fuyaoskyrocket.applocale.ui.designsystem.LocalPageContentBackdrop.current
    val blurProgress = ing.fuyaoskyrocket.applocale.ui.designsystem.LocalBlurProgress.current
    return if (backdrop != null) {
        base.copy(alpha = (1f - blurProgress).coerceIn(0f, 1f))
    } else {
        base
    }
}

/**
 * Theme-aware top app bar. The Miuix style renders the native miuix small app bar
 * (String title, miuix typography and colours); the other styles keep Material 3 at
 * the compact FuyaoColorPicker height instead of the default 64dp.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: (@Composable RowScope.() -> Unit)? = null,
    titleAlpha: Float = 1f,
) {
    if (AppUiTheme.policy.controls == AppControlFamily.Material2) {
        androidx.compose.material.TopAppBar(
            title = { androidx.compose.material.Text(title, style = AppUiTheme.textStyles.pageTitle,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.graphicsLayer { alpha = titleAlpha.coerceIn(0f, 1f) }) },
            windowInsets = CompactAppBarInsets, modifier = modifier,
            navigationIcon = navigationIcon, actions = actions ?: {},
            backgroundColor = AppUiTheme.palette.surface, contentColor = AppUiTheme.palette.foreground,
            elevation = AppUiTheme.elevation.toolbar,
        )
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Lollipop) {
        LollipopToolbar(modifier, title = { LollipopToolbarTitle(title) }, navigation = navigationIcon, actions = actions ?: {})
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Eclair) {
        EclairTitleBar(title, modifier, navigationIcon, actions)
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Holo) {
        ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloActionBar(
            modifier = modifier,
            title = { HoloActionBarTitle(text = title) },
            navigation = navigationIcon,
            actions = actions ?: {},
        )
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Miuix) {
        top.yukonga.miuix.kmp.basic.SmallTopAppBar(
            title = title,
            modifier = modifier,
            color = chromeContainerColor(
                top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.surface,
            ),
            // The native bar has no composable title slot; the alpha rides the
            // resolved onSurface title colour instead of a Material Text overlay.
            titleColor = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.onSurface.copy(
                alpha = titleAlpha.coerceIn(0f, 1f),
            ),
            navigationIcon = navigationIcon,
            actions = actions ?: {},
        )
    } else {
        androidx.compose.material3.TopAppBar(
            title = {
                Text(
                    text = title,
                    style = AppUiTheme.textStyles.pageTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.graphicsLayer {
                        alpha = titleAlpha.coerceIn(0f, 1f)
                    },
                )
            },
            modifier = modifier,
            navigationIcon = navigationIcon,
            actions = actions ?: {},
            expandedHeight = if (AppUiTheme.style == ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemeStyle.MATERIAL3_EXPRESSIVE) 64.dp else CompactAppBarHeight,
            windowInsets = CompactAppBarInsets,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = chromeContainerColor(MaterialTheme.colorScheme.surface),
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
    }
}

/**
 * The home screen's three-state top bar. Normal state shows the page title with a
 * search entry and a two-item more menu; the search state swaps the same slot for a
 * close affordance and a full-width input (a blank expanded query or a restored
 * non-blank query both count as active). The selection state is owned by the home
 * screen's selection bar and never reaches this bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHomeTopAppBar(
    title: String,
    searchExpanded: Boolean,
    query: String,
    showSystemApps: Boolean,
    onOpenSearch: () -> Unit,
    onCloseSearch: () -> Unit,
    onQueryChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onToggleSystemApps: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppSearchableTopAppBar(
        searchExpanded = searchExpanded,
        query = query,
        onCloseSearch = onCloseSearch,
        onQueryChange = onQueryChange,
        modifier = modifier,
    ) {
        AppTopAppBar(
            title = title,
            actions = {
                HomeToolbarActions(showSystemApps, onOpenSearch, onRefresh, onToggleSystemApps)
            },
        )
    }
}

/** Shared toolbar search; the caller owns its normal title and action menu. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSearchableTopAppBar(
    searchExpanded: Boolean,
    query: String,
    onCloseSearch: () -> Unit,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    searchLabel: String = stringResource(R.string.search),
    normalBar: @Composable () -> Unit,
) {
    val searchActive = searchExpanded || query.isNotBlank()
    val focusRequester = remember { FocusRequester() }
    // The field's own stable name: readable with and without typed text, distinct
    // from the close/clear affordances around it.
    val searchFieldName = searchLabel
    // Focus is requested only for a false→active transition, i.e. the search-entry
    // click. Re-entering composition already active (after selection mode, or a
    // restored query) initialises the flag to true and stays silent, so the IME is
    // never re-raised on its own.
    var wasSearchActive by remember { mutableStateOf(searchActive) }
    LaunchedEffect(searchActive) {
        if (searchActive && !wasSearchActive) {
            focusRequester.requestFocus()
        }
        wasSearchActive = searchActive
    }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    // The IME search action keeps the query and only resigns the keyboard.
    val submitSearch = {
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    if (AppUiTheme.policy.controls == AppControlFamily.Material2) {
        if (searchActive) androidx.compose.material.TopAppBar(
            title = { ing.fuyaoskyrocket.applocale.ui.designsystem.material2.RoundedSearchField(
                query, onQueryChange, searchLabel, Modifier.fillMaxWidth().focusRequester(focusRequester)) },
            windowInsets = CompactAppBarInsets, modifier = modifier,
            navigationIcon = { AppToolbarIconButton(AppSymbol.Back, stringResource(R.string.close_search), onCloseSearch) },
            backgroundColor = AppUiTheme.palette.surface, contentColor = AppUiTheme.palette.foreground,
            elevation = AppUiTheme.elevation.toolbar,
        ) else normalBar()
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Lollipop) {
        if (searchActive) LollipopToolbar(modifier,
            navigation = { AppToolbarIconButton(AppSymbol.Back, stringResource(R.string.close_search), onCloseSearch) },
            title = { LollipopSearchField(query, onQueryChange, searchLabel, Modifier.fillMaxWidth().focusRequester(focusRequester), submitSearch) },
            actions = { if (query.isNotEmpty()) AppToolbarIconButton(AppSymbol.Close, stringResource(R.string.clear_search), { onQueryChange("") }) })
        else normalBar()
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Eclair) {
        if (searchActive) {
            androidx.compose.foundation.layout.Column(modifier.fillMaxWidth()) {
                EclairTitleBar(searchLabel, navigation = {
                    EclairButton(stringResource(R.string.close_search), onCloseSearch)
                })
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    EclairSearchField(query, onQueryChange, searchLabel, Modifier.weight(1f).focusRequester(focusRequester))
                    if (query.isNotEmpty()) EclairButton(stringResource(R.string.clear_search), { onQueryChange("") })
                }
            }
        } else normalBar()
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Holo) {
        if (searchActive) {
            // SearchBar (ICS SearchView collapsed into the action bar): Up
            // affordance closes, the underline field filters live, clear only
            // empties the query — closing and clearing stay distinct actions.
            ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloActionBar(
                navigation = {
                    AppToolbarIconButton(
                        symbol = AppSymbol.Back,
                        contentDescription = stringResource(R.string.close_search),
                        onClick = onCloseSearch,
                    )
                },
                title = {
                    ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloSearchTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        hint = searchLabel,
                        fieldDescription = searchFieldName,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                actions = {
                    if (query.isNotEmpty()) {
                        AppToolbarIconButton(
                            symbol = AppSymbol.Close,
                            contentDescription = stringResource(R.string.clear_search),
                            onClick = { onQueryChange("") },
                        )
                    }
                },
            )
        } else {
            normalBar()
        }
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Miuix) {
        if (searchActive) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .background(chromeContainerColor(top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.surface))
                    .windowInsetsPadding(CompactAppBarInsets)
                    .padding(horizontal = AppSpacing.sm)
                    .heightIn(min = MiuixAppBarHeight),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppToolbarIconButton(
                    symbol = AppSymbol.Close,
                    contentDescription = stringResource(R.string.close_search),
                    onClick = onCloseSearch,
                )
                top.yukonga.miuix.kmp.basic.SearchBar(
                    inputField = {
                        top.yukonga.miuix.kmp.basic.InputField(
                            query = query,
                            onQueryChange = onQueryChange,
                            onSearch = { submitSearch() },
                            expanded = false,
                            onExpandedChange = {},
                            label = searchLabel,
                            // The native label disappears behind typed text; the
                            // explicit description keeps the field named after
                            // input, matching the Material branch.
                            modifier = Modifier
                                .focusRequester(focusRequester)
                                .semantics { contentDescription = searchFieldName },
                        )
                    },
                    expanded = false,
                    onExpandedChange = {},
                    insideMargin = DpSize(0.dp, 0.dp),
                    modifier = Modifier.weight(1f),
                ) {}
                if (query.isNotEmpty()) {
                    AppToolbarIconButton(
                        symbol = AppSymbol.Close,
                        contentDescription = stringResource(R.string.clear_search),
                        onClick = { onQueryChange("") },
                    )
                }
            }
        } else {
            normalBar()
        }
    } else {
        if (searchActive) {
            // The capsule grows with the text at large font scales; the bar follows
            // as max(base height, measured capsule + 4dp) so standard scales stay 48dp.
            var capsuleHeightPx by remember { mutableIntStateOf(0) }
            val density = LocalDensity.current
            val searchBarHeight = maxOf(
                if (AppUiTheme.style == ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemeStyle.MATERIAL3_EXPRESSIVE) 64.dp else CompactAppBarHeight,
                with(density) { capsuleHeightPx.toDp() } + 4.dp,
            )
            androidx.compose.material3.TopAppBar(
                title = {
                    // A Material TextField's fixed inner padding clips text at this
                    // height, so the capsule is composed from a BasicTextField directly.
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = SearchCapsuleHeight)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .padding(start = 12.dp)
                            .onSizeChanged { capsuleHeightPx = it.height },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = AppSpacing.sm),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (query.isEmpty()) {
                                Text(
                                    text = searchLabel,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            BasicTextField(
                                value = query,
                                onValueChange = onQueryChange,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester)
                                    // A stable localized name that survives both
                                    // the empty (placeholder) and typed states —
                                    // the placeholder alone is not an accessible
                                    // name once text is present.
                                    .semantics {
                                        contentDescription = searchFieldName
                                    },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(
                                    onSearch = { submitSearch() },
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onCloseSearch) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.close_search),
                        )
                    }
                },
                actions = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.clear_search),
                            )
                        }
                    }
                },
                modifier = modifier,
                expandedHeight = searchBarHeight,
                windowInsets = CompactAppBarInsets,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = chromeContainerColor(MaterialTheme.colorScheme.surface),
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        } else {
            normalBar()
        }
    }
}

/**
 * The normal state's right side: the search entry and the more menu with the two
 * existing actions. Menu rows close the menu first, then run the action. The
 * glyphs resolve through [AppSymbolVector] so each style draws its native set.
 */
@Composable
private fun HomeToolbarActions(
    showSystemApps: Boolean,
    onOpenSearch: () -> Unit,
    onRefresh: () -> Unit,
    onToggleSystemApps: () -> Unit,
) {
    AppToolbarIconButton(
        symbol = AppSymbol.Search,
        contentDescription = stringResource(R.string.search),
        onClick = onOpenSearch,
    )
    if (AppUiTheme.policy.controls == AppControlFamily.Holo
    ) {
        // Holo keeps refresh as a visible action bar action (round-9 C16);
        // the system-apps toggle lives behind the overflow.
        AppToolbarIconButton(
            symbol = AppSymbol.Refresh,
            contentDescription = stringResource(R.string.refresh),
            onClick = onRefresh,
        )
    }
    Box {
        var menuExpanded by remember { mutableStateOf(false) }
        AppToolbarIconButton(
            symbol = AppSymbol.Menu,
            contentDescription = stringResource(R.string.more_actions),
            onClick = { menuExpanded = true },
        )
        val isHolo = AppUiTheme.policy.controls == AppControlFamily.Holo
        AppDropdownMenu(
            expanded = menuExpanded,
            onDismiss = { menuExpanded = false },
            items = buildList {
                if (!isHolo) {
                    add(
                        AppDropdownItem(
                            text = stringResource(R.string.refresh),
                            onClick = {
                                menuExpanded = false
                                onRefresh()
                            },
                        ),
                    )
                }
                add(
                    AppDropdownItem(
                        text = stringResource(R.string.show_system_apps),
                        selected = showSystemApps,
                        onClick = {
                            menuExpanded = false
                            onToggleSystemApps()
                        },
                    ),
                )
            },
        )
    }
}
