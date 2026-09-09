package ing.fuyaoskyrocket.applocale.ui.designsystem.lollipop

import android.graphics.drawable.Animatable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.legacyDrawable

/** Original vector geometry, property animators and path interpolators from API 21. */
@Composable
internal fun LollipopProgress(modifier: Modifier = Modifier, horizontal: Boolean = false, progress: Float? = null) {
    val drawable = rememberLollipopDrawable(if (horizontal && progress != null) R.drawable.lollipop_progress_horizontal_material
        else if (horizontal) R.drawable.lollipop_progress_indeterminate_horizontal_material else R.drawable.lollipop_progress_medium_material)
    DisposableEffect(drawable) {
        (drawable as? Animatable)?.start()
        onDispose { (drawable as? Animatable)?.stop() }
    }
    Box(modifier.then(if (horizontal) Modifier.fillMaxWidth().height(16.dp) else Modifier.size(48.dp))
        .semantics { progressBarRangeInfo = if (progress == null) ProgressBarRangeInfo.Indeterminate else ProgressBarRangeInfo(progress.coerceIn(0f, 1f), 0f..1f) }
        .legacyDrawable(drawable, levelProvider = { ((progress ?: 0f).coerceIn(0f, 1f) * 10000).toInt() }))
}
