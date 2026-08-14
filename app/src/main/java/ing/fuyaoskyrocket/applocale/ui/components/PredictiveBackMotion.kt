package ing.fuyaoskyrocket.applocale.ui.components

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException

data class PredictiveBackMotion(
    val progress: Float = 0f,
    val fromLeftEdge: Boolean = true,
)

/**
 * Handles back only while a screen owns dismissible internal UI state.
 * Route-level back remains owned by Navigation Compose.
 */
@Composable
fun rememberPredictiveBackMotion(
    enabled: Boolean,
    onBack: () -> Unit,
): PredictiveBackMotion {
    var motion by remember { mutableStateOf(PredictiveBackMotion()) }
    val currentOnBack by rememberUpdatedState(onBack)

    PredictiveBackHandler(enabled = enabled) { events ->
        try {
            events.collect { event ->
                motion = PredictiveBackMotion(
                    progress = event.progress.coerceIn(0f, 1f),
                    fromLeftEdge = event.swipeEdge == BackEventCompat.EDGE_LEFT,
                )
            }
            currentOnBack()
        } catch (_: CancellationException) {
            // A cancelled gesture restores the unchanged screen below.
        } finally {
            motion = PredictiveBackMotion()
        }
    }

    return motion
}

/** A restrained Material-style preview for custom back targets. */
fun Modifier.predictiveBackTransform(motion: PredictiveBackMotion): Modifier =
    graphicsLayer {
        val direction = if (motion.fromLeftEdge) 1f else -1f
        scaleX = 1f - (0.04f * motion.progress)
        scaleY = 1f - (0.04f * motion.progress)
        translationX = direction * 16.dp.toPx() * motion.progress
    }
