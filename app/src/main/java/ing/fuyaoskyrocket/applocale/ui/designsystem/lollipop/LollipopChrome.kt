package ing.fuyaoskyrocket.applocale.ui.designsystem.lollipop

import androidx.compose.foundation.*
import androidx.compose.foundation.selection.selectable
import ing.fuyaoskyrocket.applocale.ui.components.AppNavigationDestination
import ing.fuyaoskyrocket.applocale.ui.components.rememberTabDoubleTapDetector
import ing.fuyaoskyrocket.applocale.ui.components.tabTouchTapObserver
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.rememberAppPaneChrome
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.*
import ing.fuyaoskyrocket.applocale.ui.screen.LocalTopLevelNavigation
import java.util.Locale
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.revealSelectedTab

@Composable
internal fun LollipopToolbar(modifier: Modifier = Modifier, title: @Composable () -> Unit,
    navigation: @Composable () -> Unit = {}, actions: @Composable RowScope.() -> Unit = {}) {
    CompositionLocalProvider(LocalLollipopContentColor provides Color.White) {
    Row(modifier.fillMaxWidth().heightIn(min = dimensionResource(R.dimen.lollipop_action_bar_default_height_material))
        .background(LocalLollipopColors.current.primary), verticalAlignment = Alignment.CenterVertically) {
        navigation()
        Box(Modifier.weight(1f).padding(start = 16.dp)) { title() }
        Row(verticalAlignment = Alignment.CenterVertically, content = actions)
    }
    }
}

@Composable
internal fun LollipopToolbarTitle(title: String) {
    BasicText(title, style = lollipopTextStyle(20, FontWeight.Medium).copy(color = Color.White),
        maxLines = 2, overflow = TextOverflow.Ellipsis)
}

/** AOSP Widget.Material.ActionBar.TabText remains 12sp bold in this exact tag. */
@Composable
internal fun LollipopTopTabs() {
    val navigation = LocalTopLevelNavigation.current ?: return
    val detector = rememberTabDoubleTapDetector()
    val scrollToTop = stringResource(R.string.scroll_to_top)
    val colors = LocalLollipopColors.current
    Row(Modifier.fillMaxWidth().background(colors.primary).horizontalScroll(rememberScrollState())) {
        navigation.destinations.forEach { (destination, label) ->
            key(destination) {
                val selected = destination == navigation.currentDestination
                val interaction = remember { MutableInteractionSource() }
                val state = legacyControlState(interaction, selected = selected)
                Box(Modifier.widthIn(min = 88.dp).heightIn(min = dimensionResource(R.dimen.lollipop_action_bar_default_height_material))
                    .legacyBackground(rememberLollipopDrawable(R.drawable.lollipop_tab_indicator_material, state), state)
                    .selectable(selected, interaction, LollipopRipple(Color.White.copy(alpha = .18f)), role = Role.Tab,
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
                    }.revealSelectedTab(selected).padding(horizontal = 16.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                    BasicText(stringResource(label).uppercase(Locale.getDefault()), style = lollipopTextStyle(12, FontWeight.Bold).copy(color = Color.White), maxLines = 1, softWrap = false)
                }
            }
        }
    }
}

@Composable
internal fun LollipopScaffold(modifier: Modifier = Modifier, topLevelNavigation: Boolean = false, contextual: Boolean = false,
    topBar: @Composable () -> Unit, snackbarHost: @Composable () -> Unit, containerColor: Color,
    content: @Composable (PaddingValues) -> Unit, floatingActionButton: @Composable () -> Unit = {}) {
    val colors = LocalLollipopColors.current
    val top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val horizontal = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal).asPaddingValues()
    val pane = rememberAppPaneChrome()
    val density = LocalDensity.current
    Box(modifier.fillMaxSize().background(containerColor)) {
        Column(Modifier.fillMaxSize().padding(horizontal)) {
            Spacer(Modifier.fillMaxWidth().height(top).background(colors.primaryDark))
            Column(Modifier.shadow(if (pane == null) ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme.elevation.toolbar else 0.dp)) {
                Box(Modifier.onSizeChanged { pane?.state?.reportTitle(pane.pane, it.height) }) { topBar() }
                if (pane == null && topLevelNavigation && !contextual) LollipopTopTabs()
            }
            if (pane != null) Spacer(Modifier.height(with(density) { pane.state.contentGapPx(pane.pane, pane.navigationVisible).toDp() }))
            Box(Modifier.weight(1f).fillMaxWidth()) { content(PaddingValues(0.dp)) }
            Spacer(Modifier.fillMaxWidth().height(bottom).background(colors.primaryDark))
        }
        Box(Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = bottom + 16.dp)) { floatingActionButton() }
        Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = bottom)) { snackbarHost() }
    }
}
