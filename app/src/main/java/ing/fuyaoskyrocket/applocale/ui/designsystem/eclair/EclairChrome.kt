package ing.fuyaoskyrocket.applocale.ui.designsystem.eclair

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.selection.selectable
import ing.fuyaoskyrocket.applocale.ui.components.rememberTabDoubleTapDetector
import ing.fuyaoskyrocket.applocale.ui.components.tabTouchTapObserver
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.revealSelectedTab
import ing.fuyaoskyrocket.applocale.ui.designsystem.rememberAppPaneChrome
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.components.AppNavigationDestination
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.*
import ing.fuyaoskyrocket.applocale.ui.screen.LocalTopLevelNavigation

/** Compact classic title strip with its menu anchored at the trailing edge. */
@Composable
internal fun EclairTitleBar(
    title: String, modifier: Modifier = Modifier, navigation: @Composable () -> Unit = {},
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    var menuOpen by remember { mutableStateOf(false) }
    BackHandler(menuOpen) { menuOpen = false }
    Row(modifier.fillMaxWidth().heightIn(min = 48.dp)
        .legacyBackground(rememberClassicDrawable(R.drawable.eclair_title_bar))
        .padding(start = 6.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        navigation()
        BasicText(title, Modifier.weight(1f).padding(horizontal = 6.dp, vertical = 6.dp),
            style = eclairTextStyle(14, true).copy(color = Color.White,
                shadow = Shadow(Color.Black, Offset.Zero, 2.75f)), maxLines = 2, overflow = TextOverflow.Ellipsis)
        if (actions != null) {
            Box {
                EclairButton(stringResource(R.string.legacy_menu), { menuOpen = !menuOpen })
                if (menuOpen) {
                    val configuration = LocalConfiguration.current
                    Popup(alignment = Alignment.TopEnd, onDismissRequest = { menuOpen = false },
                        properties = PopupProperties(focusable = true)) {
                        LegacyDialogSurface(rememberClassicDrawable(R.drawable.eclair_menu_background), classicMenuFace,
                            Modifier.widthIn(min = 196.dp, max = minOf(360.dp, configuration.screenWidthDp.dp - 24.dp))
                                .heightIn(max = configuration.screenHeightDp.dp * .7f)) {
                            CompositionLocalProvider(LocalEclairMenuContent provides true,
                                LocalEclairMenuDismiss provides { menuOpen = false }) {
                                FlowRow(Modifier.fillMaxWidth().padding(4.dp), content = actions)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun EclairTopTabs() {
    val navigation = LocalTopLevelNavigation.current ?: return
    val detector = rememberTabDoubleTapDetector()
    val scrollToTop = stringResource(R.string.scroll_to_top)
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
        navigation.destinations.forEach { (destination, label) ->
            key(destination) {
                val selected = destination == navigation.currentDestination
                val interaction = remember { MutableInteractionSource() }
                val interactionState = legacyControlState(interaction, selected = selected)
                // Classic TabHost gave the current tab keyboard focus. Use its original
                // amber focus artwork for the active tab on today's touch-only devices.
                val state = interactionState.copy(focused = interactionState.focused || selected)
                val icon = when (destination) {
                    AppNavigationDestination.Home -> R.drawable.eclair_ic_menu_view
                    AppNavigationDestination.SystemLanguages -> R.drawable.eclair_ic_menu_mapmode
                    AppNavigationDestination.Configurations -> R.drawable.eclair_ic_menu_manage
                    AppNavigationDestination.Settings -> R.drawable.eclair_ic_menu_preferences
                    AppNavigationDestination.About -> R.drawable.eclair_ic_menu_info_details
                }
                Column(Modifier.widthIn(min = 80.dp).heightIn(min = 64.dp)
                    .legacyBackground(rememberClassicDrawable(R.drawable.eclair_tab_indicator), state)
                    .selectable(selected, interaction, indication = null, role = Role.Tab,
                        onClick = { navigation.navigate(destination) })
                    .tabTouchTapObserver(destination, enabled = { true }) { target ->
                        if (detector.recordTap(target)) navigation.onTabDoubleTap(target as AppNavigationDestination)
                    }
                    .semantics {
                        this.selected = selected
                        if (selected) customActions = listOf(CustomAccessibilityAction(scrollToTop) {
                            navigation.onTabDoubleTap(destination)
                            true
                        })
                    }.revealSelectedTab(selected).padding(horizontal = 6.dp, vertical = 3.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(Modifier.size(ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme.icons.tabSize).legacyDrawable(rememberClassicDrawable(icon),
                        boundsMode = LegacyDrawableBounds.Intrinsic, alignment = Alignment.Center))
                    BasicText(stringResource(label), style = eclairTextStyle(14).copy(color = eclairColor(R.color.eclair_tab_indicator_text, state)), maxLines = 1, softWrap = false)
                }
            }
        }
    }
}

@Composable
internal fun EclairScaffold(
    modifier: Modifier = Modifier, topLevelNavigation: Boolean = false, contextual: Boolean = false,
    topBar: @Composable () -> Unit, bottomBar: @Composable () -> Unit,
    snackbarHost: @Composable () -> Unit, containerColor: Color, content: @Composable (PaddingValues) -> Unit,
) {
    val top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val horizontal = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal).asPaddingValues()
    val pane = rememberAppPaneChrome()
    val density = LocalDensity.current
    Box(modifier.fillMaxSize().background(containerColor)) {
        Column(Modifier.fillMaxSize().padding(horizontal)) {
            Spacer(Modifier.fillMaxWidth().height(top).background(Color.Black))
            Box(Modifier.onSizeChanged { pane?.state?.reportTitle(pane.pane, it.height) }) { topBar() }
            if (pane != null) Spacer(Modifier.height(with(density) {
                pane.state.contentGapPx(pane.pane, pane.navigationVisible).toDp()
            }))
            else if (topLevelNavigation && !contextual) EclairTopTabs()
            Box(Modifier.weight(1f).fillMaxWidth()) { content(PaddingValues(0.dp)) }
            bottomBar()
            Spacer(Modifier.fillMaxWidth().height(bottom).background(Color.Black))
        }
        Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = bottom)) { snackbarHost() }
    }
}
