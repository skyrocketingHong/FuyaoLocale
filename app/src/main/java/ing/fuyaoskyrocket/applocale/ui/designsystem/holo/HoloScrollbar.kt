package ing.fuyaoskyrocket.applocale.ui.designsystem.holo

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.LegacyVerticalScrollBar

@Composable
internal fun HoloVerticalScrollBar(listState: LazyListState, modifier: Modifier = Modifier) =
    LegacyVerticalScrollBar(rememberHoloDrawable(HoloAsset.ScrollbarHandle), fadeDelayMillis = 300, fadeDurationMillis = 250, listState = listState, modifier = modifier)
