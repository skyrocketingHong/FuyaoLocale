package ing.fuyaoskyrocket.applocale.ui.designsystem.lollipop

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.*

@Composable
internal fun LollipopToastHost(queue: LegacyMessageQueue, modifier: Modifier = Modifier) {
    LegacyMessageHost(queue, 3000L) { message ->
        val frame = rememberLollipopDrawable(R.drawable.lollipop_toast_frame)
        val rect = remember(frame) { android.graphics.Rect().also { frame.getPadding(it) } }
        val density = LocalDensity.current
        Box(modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 48.dp), contentAlignment = Alignment.BottomCenter) {
            Box(Modifier.legacyBackground(frame).semantics { liveRegion = LiveRegionMode.Polite }.padding(with(density) {
                PaddingValues(rect.left.toDp(), rect.top.toDp(), rect.right.toDp(), rect.bottom.toDp())
            })) {
                BasicText(message, style = lollipopTextStyle(14).copy(color = Color.White,
                    shadow = Shadow(Color(0xbb000000), blurRadius = 2.75f)), maxLines = 3)
            }
        }
    }
}
