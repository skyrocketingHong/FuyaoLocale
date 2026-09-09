package ing.fuyaoskyrocket.applocale.ui.designsystem.holo

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import ing.fuyaoskyrocket.applocale.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The Widget.Holo.ActionMode contextual bar: cab_background_top_holo_dark
 * 9-patch at the action bar height, a close affordance (ic_menu_close), the
 * selected-count title in ActionMode title style, then the action row — the
 * primary business action as a bordered-less ICS text button, the rest behind
 * the overflow. `isApplying` swaps the action row for the era circular
 * progress, gated exactly like the modern batch bar.
 */
@Composable
internal fun HoloContextualBar(
    title: String,
    onClose: () -> Unit,
    primaryAction: (@Composable () -> Unit)? = null,
    overflow: (@Composable () -> Unit)? = null,
    isApplying: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalHoloMetrics.current
    val textColors = LocalHoloTextColors.current
    val background = rememberHoloDrawable(HoloAsset.CabBackground)
    val closeLabel = stringResource(R.string.exit_selection)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(metrics.actionBarHeight)
            .holoBackground(background),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HoloActionButton(onClick = onClose, modifier = Modifier.semantics { contentDescription = closeLabel }) {
            HoloAssetIcon(HoloAsset.CabClose, null)
        }
        androidx.compose.foundation.text.BasicText(
            text = title,
            style = androidx.compose.ui.text.TextStyle(
                fontFamily = LocalHoloFontFamily.current,
                fontSize = 18.sp,
                color = textColors.primary,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp),
        )
        if (isApplying) {
            HoloCircularProgress(modifier = Modifier.padding(end = 12.dp))
        } else {
            primaryAction?.invoke()
            overflow?.invoke()
        }
    }
}
