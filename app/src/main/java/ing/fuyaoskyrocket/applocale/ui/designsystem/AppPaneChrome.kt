package ing.fuyaoskyrocket.applocale.ui.designsystem

import androidx.compose.runtime.*

/** Geometry only: navigation and page state remain owned by their existing callers. */
@Stable
internal class AppPaneChromeState(val fallbackTitleHeightPx: Int, val fallbackTabHeightPx: Int) {
    private val titleHeights = mutableStateMapOf<Int, Int>()
    var measuredTabHeightPx by mutableIntStateOf(0)
    val maxTitleHeightPx: Int get() = maxOf(fallbackTitleHeightPx, titleHeights.values.maxOrNull() ?: 0)
    val tabHeightPx: Int get() = measuredTabHeightPx.takeIf { it > 0 } ?: fallbackTabHeightPx
    fun reportTitle(pane: Int, height: Int) { titleHeights[pane] = height }
    fun removePane(pane: Int) { titleHeights.remove(pane) }
    fun contentGapPx(pane: Int, navigationVisible: Boolean): Int =
        (maxTitleHeightPx - (titleHeights[pane] ?: fallbackTitleHeightPx)).coerceAtLeast(0) +
            if (navigationVisible) tabHeightPx else 0
}

internal data class AppPaneChrome(val state: AppPaneChromeState, val pane: Int, val navigationVisible: Boolean)
internal val LocalAppPaneChrome = staticCompositionLocalOf<AppPaneChrome?> { null }

@Composable
internal fun rememberAppPaneChrome(): AppPaneChrome? {
    val pane = LocalAppPaneChrome.current
    DisposableEffect(pane?.state, pane?.pane) {
        onDispose { if (pane != null) pane.state.removePane(pane.pane) }
    }
    return pane
}
