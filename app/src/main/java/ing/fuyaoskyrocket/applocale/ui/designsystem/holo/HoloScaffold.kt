package ing.fuyaoskyrocket.applocale.ui.designsystem.holo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import ing.fuyaoskyrocket.applocale.ui.designsystem.rememberAppPaneChrome
import ing.fuyaoskyrocket.applocale.ui.components.AppNavigationDestination
import ing.fuyaoskyrocket.applocale.ui.screen.LocalTopLevelNavigation
import ing.fuyaoskyrocket.applocale.ui.designsystem.kitkat.KitKatChrome

/**
 * The Holo scaffold: a fixed chrome column — status-bar inset, the caller's
 * action bar (or CAB), the top tabs on real top-level pages, then the content.
 * ICS solid bars are not translucent overlays, so the content starts below the
 * chrome (the pages' innerPadding top is zero here) and the floating feedback
 * area anchors to the bottom edge. No M3 Scaffold, no MaterialTheme.
 */
@Composable
internal fun HoloScaffold(
    modifier: Modifier = Modifier,
    topLevelNavigation: Boolean = false,
    contextual: Boolean = false,
    topBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    containerColor: Color,
    content: @Composable (PaddingValues) -> Unit,
) {
    val statusBarTop = WindowInsets.statusBars
        .only(androidx.compose.foundation.layout.WindowInsetsSides.Top)
        .asPaddingValues()
        .calculateTopPadding()
    val navigation = LocalTopLevelNavigation.current
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val horizontalInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal).asPaddingValues()
    val pane = rememberAppPaneChrome()
    val density = LocalDensity.current

    val windowBackground = LocalHoloWindowBackground.current
    val backgroundModifier = if (pane != null) {
        Modifier
    } else if (windowBackground != null) {
        Modifier.holoBackground(
            ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.rememberLegacyDrawable(windowBackground),
        )
    } else {
        Modifier.background(containerColor)
    }
    Box(modifier = modifier.fillMaxSize().then(backgroundModifier)) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontalInsets)) {
            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier.fillMaxWidth().height(statusBarTop).background(Color.Black),
            )
            KitKatChrome {
            Box(Modifier.onSizeChanged { pane?.state?.reportTitle(pane.pane, it.height) }) { topBar() }
            if (pane != null) {
                androidx.compose.foundation.layout.Spacer(Modifier.height(with(density) {
                    pane.state.contentGapPx(pane.pane, pane.navigationVisible).toDp()
                }))
            } else if (topLevelNavigation && !contextual && navigation != null) {
                HoloTopTabs(
                    currentDestination = navigation.currentDestination,
                    onNavigate = navigation.navigate,
                    onTabDoubleTap = navigation.onTabDoubleTap,
                    destinations = navigation.destinations,
                )
            }
            }
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                content(PaddingValues(0.dp, 0.dp, 0.dp, 0.dp))
            }
            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier.fillMaxWidth().height(bottomInset).background(Color.Black),
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = bottomInset),
        ) {
            snackbarHost()
        }
    }
}
