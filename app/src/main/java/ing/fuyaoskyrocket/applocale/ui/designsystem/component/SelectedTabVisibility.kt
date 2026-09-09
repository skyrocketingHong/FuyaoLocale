package ing.fuyaoskyrocket.applocale.ui.designsystem.component

import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize

/** Scrollable era tabs grow to their label and reveal the selected item after layout. */
@Composable
internal fun Modifier.revealSelectedTab(selected: Boolean): Modifier {
    val requester = remember { BringIntoViewRequester() }
    var size by remember { mutableStateOf(IntSize.Zero) }
    LaunchedEffect(selected, size) {
        if (selected && size.width > 0 && size.height > 0) requester.bringIntoView()
    }
    return bringIntoViewRequester(requester).onSizeChanged { size = it }
}
