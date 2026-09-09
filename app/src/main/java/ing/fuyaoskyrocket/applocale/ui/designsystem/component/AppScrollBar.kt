package ing.fuyaoskyrocket.applocale.ui.designsystem.component

import ing.fuyaoskyrocket.applocale.ui.designsystem.eclair.*
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppControlFamily
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme

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
    when (AppUiTheme.policy.controls) {
        AppControlFamily.Miuix -> top.yukonga.miuix.kmp.basic.VerticalScrollBar(
            adapter = top.yukonga.miuix.kmp.basic.rememberScrollBarAdapter(
                scrollState = listState,
            ),
            modifier = modifier,
        )

        AppControlFamily.Eclair -> ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.LegacyVerticalScrollBar(
            ing.fuyaoskyrocket.applocale.ui.designsystem.eclair.rememberClassicDrawable(ing.fuyaoskyrocket.applocale.R.drawable.eclair_scrollbar_handle_vertical),
            fadeDelayMillis = 300, fadeDurationMillis = 250, listState = listState, modifier = modifier,
        )
        AppControlFamily.Holo ->
            ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloVerticalScrollBar(
                listState = listState,
                modifier = modifier,
            )

        AppControlFamily.Lollipop -> ing.fuyaoskyrocket.applocale.ui.designsystem.lollipop.LollipopVerticalScrollBar(listState, modifier)
        AppControlFamily.Material2, AppControlFamily.Material3 -> Unit
    }
}
