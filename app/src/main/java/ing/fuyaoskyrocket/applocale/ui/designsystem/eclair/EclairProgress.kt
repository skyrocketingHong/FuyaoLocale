package ing.fuyaoskyrocket.applocale.ui.designsystem.eclair

import android.graphics.drawable.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.*

/** Imported rotation uses discrete levels: 12 frames, 100ms each, as in the source XML. */
@Composable
internal fun EclairProgress(modifier: Modifier = Modifier, horizontal: Boolean = false, progress: Float? = null) {
    val dark = LocalEclairColors.current.dark
    val froyo = LocalClassicEra.current == ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemeStyle.FROYO
    val drawable = rememberClassicDrawable(when {
        horizontal && progress != null -> R.drawable.eclair_progress_horizontal
        horizontal -> if (froyo) R.drawable.froyo_progress_indeterminate_horizontal else R.drawable.eclair_progress_indeterminate_horizontal
        dark -> R.drawable.eclair_progress_medium_white
        else -> R.drawable.eclair_progress_medium
    })
    DisposableEffect(drawable) {
        (drawable as? Animatable)?.start()
        onDispose { (drawable as? Animatable)?.stop() }
    }
    // Horizontal drawables have their own frame animation or a determinate
    // level; only the circular RotateDrawable needs the Compose frame clock.
    val fraction = if (horizontal) 0f else {
        val transition = rememberInfiniteTransition(label = "eclairProgress")
        val frame by transition.animateFloat(0f, 1f,
            infiniteRepeatable(tween(1200, easing = LinearEasing)), label = "eclairProgressFrame")
        frame
    }
    Box(modifier.then(if (horizontal) Modifier.fillMaxWidth().height(20.dp) else Modifier.size(48.dp))
        .semantics { progressBarRangeInfo = if (progress == null) ProgressBarRangeInfo.Indeterminate
            else ProgressBarRangeInfo(progress.coerceIn(0f, 1f), 0f..1f) }
        .legacyDrawable(drawable, levelProvider = {
            if (horizontal) ((progress ?: 0f).coerceIn(0f, 1f) * 10000).toInt()
            else ((fraction * 12).toInt() * 10000 / 12)
        }))
}
