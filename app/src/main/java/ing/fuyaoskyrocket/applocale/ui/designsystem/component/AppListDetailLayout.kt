package ing.fuyaoskyrocket.applocale.ui.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.ui.designsystem.*
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.*
import ing.fuyaoskyrocket.applocale.ui.designsystem.eclair.EclairTopTabs
import ing.fuyaoskyrocket.applocale.ui.designsystem.lollipop.LollipopTopTabs
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.*
import ing.fuyaoskyrocket.applocale.ui.screen.LocalPagerPageScaffold
import ing.fuyaoskyrocket.applocale.ui.screen.LocalTopLevelNavigation

/** Owns geometry and aligned fixed chrome; page models and navigation stay in the supplied slots. */
@Composable
fun AppListDetailLayout(contextual: Boolean, list: @Composable () -> Unit, detail: @Composable () -> Unit) {
    val continuous = AppUiTheme.policy.continuousLists
    val density = LocalDensity.current
    val titleHeight = if (continuous) AppUiTheme.spacing.toolbarHeight else 0.dp
    val tabHeight = if (continuous) AppUiTheme.spacing.tabHeight else 0.dp
    val chrome = remember(density, titleHeight, tabHeight) {
        AppPaneChromeState(with(density) { titleHeight.roundToPx() }, with(density) { tabHeight.roundToPx() })
    }
    val hostedByPager = LocalPagerPageScaffold.current != null
    val provideChrome = continuous || hostedByPager
    val tabsVisible = continuous && !contextual && !hostedByPager
    val backgroundResource = if (AppUiTheme.policy.controls == AppControlFamily.Holo) LocalHoloWindowBackground.current else null
    val background = if (backgroundResource == null) Modifier.background(AppUiTheme.palette.background)
        else Modifier.legacyBackground(rememberLegacyDrawable(backgroundResource))
    Box(Modifier.fillMaxSize().then(background)) {
        Row(Modifier.fillMaxSize().padding(start = AppUiTheme.spacing.paneOuterInset, end = AppUiTheme.spacing.paneOuterInset,
            bottom = if (continuous) 0.dp else AppSpacing.screenExpanded), horizontalArrangement = Arrangement.spacedBy(AppUiTheme.spacing.paneGap)) {
            AppPanel(Modifier.width(AppLayout.listPaneWidth).fillMaxHeight(), cornerRadius = AppUiTheme.shapes.section.radius,
                color = AppUiTheme.palette.secondarySurface) {
                CompositionLocalProvider(LocalAppPaneChrome provides if (provideChrome) AppPaneChrome(chrome, 0, tabsVisible) else null, content = list)
            }
            AppPanel(Modifier.weight(1f).fillMaxHeight(), cornerRadius = AppUiTheme.shapes.section.radius, color = AppUiTheme.palette.surface) {
                CompositionLocalProvider(LocalAppPaneChrome provides if (provideChrome) AppPaneChrome(chrome, 1, tabsVisible) else null, content = detail)
            }
        }
        if (tabsVisible) {
            val statusTop = with(density) { WindowInsets.statusBars.asPaddingValues().calculateTopPadding().roundToPx() }
            Box(Modifier.fillMaxWidth().offset { IntOffset(0, statusTop + chrome.maxTitleHeightPx) }
                .onSizeChanged { chrome.measuredTabHeightPx = it.height }) {
                when (AppUiTheme.policy.controls) {
                    AppControlFamily.Eclair -> EclairTopTabs()
                    AppControlFamily.Lollipop -> LollipopTopTabs()
                    AppControlFamily.Holo -> LocalTopLevelNavigation.current?.let {
                        HoloTopTabs(it.currentDestination, it.navigate, it.onTabDoubleTap, it.destinations)
                    }
                    AppControlFamily.Material2, AppControlFamily.Material3, AppControlFamily.Miuix -> Unit
                }
            }
        }
    }
}

@Composable
fun rememberAppPageOverscrollEffect() = if (AppUiTheme.policy.continuousLists) null else androidx.compose.foundation.rememberOverscrollEffect()
