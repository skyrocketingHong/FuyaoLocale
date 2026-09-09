package ing.fuyaoskyrocket.applocale.ui.designsystem.holo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay

internal class HoloToastQueue : ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.LegacyMessageQueue()

@Composable
internal fun HoloToastHost(
    queue: HoloToastQueue,
    modifier: Modifier = Modifier,
) {
    ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.LegacyMessageHost(queue, 3000L) { message ->
    val density = LocalDensity.current
    val frame = rememberHoloDrawable(HoloAsset.ToastFrame)
    val horizontal = frame.intrinsicPaddingHorizontal(density)
    val vertical = frame.intrinsicPaddingVertical(density)

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 48.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(align = Alignment.CenterHorizontally)
                .padding(horizontal = 32.dp)
                .holoBackground(frame)
                .padding(
                    start = horizontal,
                    end = horizontal,
                    top = vertical,
                    bottom = vertical,
                ),
        ) {
            HoloText(
                text = message,
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = LocalHoloFontFamily.current,
                    fontSize = 14.sp,
                ),
                // The era toast frame is dark in both application themes.
                color = HoloTextColors.Default.primary,
                maxLines = 3,
            )
        }
    }
}
}
