package ing.fuyaoskyrocket.applocale.ui.designsystem.holo

import android.graphics.drawable.AnimationDrawable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.R

/**
 * The indeterminate Holo circular progress: the imported progress_medium_holo
 * layer-list (outer 0→1080°, inner 720→0° rotations) driven by the same
 * animation ProgressBar.java runs on this commit — a linear 0→MAX_LEVEL
 * repeat every 4000ms. The level is read inside the draw phase only.
 */
@Composable
internal fun HoloCircularProgress(modifier: Modifier = Modifier) {
    val drawable = rememberHoloDrawable(HoloAsset.ProgressMedium)
    val transition = rememberInfiniteTransition(label = "holoIndeterminate")
    val levelFraction by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = LocalHoloMetrics.current.indeterminateDurationMillis,
                easing = LinearEasing,
            ),
        ),
        label = "holoIndeterminateLevel",
    )
    Box(
        modifier = modifier
            .size(48.dp)
            .holoDrawable(
                drawable,
                levelProvider = { (levelFraction * HoloMaxLevel).toInt() },
            ),
    )
}

/**
 * The determinate Holo horizontal progress: the progress_horizontal_holo_dark
 * layer-list whose progress layer scales with the level (fraction × 10000).
 */
@Composable
internal fun HoloLinearProgress(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val drawable = rememberHoloDrawable(HoloAsset.ProgressHorizontal)
    val fraction = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(16.dp)
            .holoDrawable(
                drawable,
                levelProvider = { (fraction * HoloMaxLevel).toInt() },
            ),
    )
}

/**
 * The indeterminate Holo horizontal progress: the imported 8-frame
 * animation-list (50ms per frame, looping), started with the composition and
 * stopped on detach by the drawable bridge's handler cleanup.
 */
@Composable
internal fun HoloLinearIndeterminateProgress(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val drawable = rememberHoloDrawable(HoloAsset.ProgressIndeterminateHorizontal)
    LaunchedEffect(drawable) {
        val animation = drawable as? AnimationDrawable ?: return@LaunchedEffect
        animation.start()
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(16.dp)
            .holoDrawable(drawable),
    )
}
