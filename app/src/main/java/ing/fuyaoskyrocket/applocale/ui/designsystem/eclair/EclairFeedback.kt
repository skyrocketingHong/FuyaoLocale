package ing.fuyaoskyrocket.applocale.ui.designsystem.eclair

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.*

@Composable
internal fun EclairToastHost(queue: LegacyMessageQueue, modifier: Modifier = Modifier) {
    LegacyMessageHost(queue, 3000L) { message ->
        val frame = rememberClassicDrawable(R.drawable.eclair_toast_frame)
        val rect = remember(frame) { android.graphics.Rect().also { frame.getPadding(it) } }
        val density = LocalDensity.current
        Box(modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 48.dp), contentAlignment = Alignment.BottomCenter) {
            Box(Modifier.legacyBackground(frame).padding(with(density) {
                PaddingValues(rect.left.toDp(), rect.top.toDp(), rect.right.toDp(), rect.bottom.toDp())
            })) { BasicText(message, style = eclairTextStyle(14).copy(color = Color.White), maxLines = 3) }
        }
    }
}
