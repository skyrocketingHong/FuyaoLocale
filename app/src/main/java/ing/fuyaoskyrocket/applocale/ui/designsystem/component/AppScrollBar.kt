package ing.fuyaoskyrocket.applocale.ui.designsystem.component

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemePreferences
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemeStyle

/**
 * Position feedback for long lists, bound to the caller's [LazyListState] — never
 * a second scroll state. The miuix style renders the native scrollbar; Material
 * You keeps its scrollbar-free list idiom, so it renders nothing there.
 */
@OptIn(top.yukonga.miuix.kmp.interfaces.ExperimentalScrollBarApi::class)
@Composable
fun AppVerticalScrollBar(
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    if (AppThemePreferences.style == AppThemeStyle.MIUIX) {
        top.yukonga.miuix.kmp.basic.VerticalScrollBar(
            adapter = top.yukonga.miuix.kmp.basic.rememberScrollBarAdapter(
                scrollState = listState,
            ),
            modifier = modifier,
        )
    }
}
