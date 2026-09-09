package ing.fuyaoskyrocket.applocale.ui.designsystem.legacy

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


internal data class LegacyDialogMotion(
    val scaleDurationMillis: Int,
    val alphaDurationMillis: Int,
    val scaleEasing: androidx.compose.animation.core.Easing,
    val alphaEasing: androidx.compose.animation.core.Easing,
    val hiddenScale: Float = 0.9f,
)

internal class LegacyDialogTransition(private val motion: LegacyDialogMotion) {
    var mounted by mutableStateOf(false)
        private set
    var wasShown by mutableStateOf(false)
        private set
    val scale = Animatable(motion.hiddenScale)
    val alpha = Animatable(0f)

    suspend fun show(snap: Boolean) {
        wasShown = true
        mounted = true
        if (snap) {
            scale.snapTo(1f)
            alpha.snapTo(1f)
            return
        }
        coroutineScope {
            launch {
                scale.animateTo(1f, tweenEasing(motion.scaleEasing, motion.scaleDurationMillis))
            }
            alpha.animateTo(1f, tweenEasing(motion.alphaEasing, motion.alphaDurationMillis))
        }
    }

    suspend fun hide(snap: Boolean) {
        if (snap) {
            scale.snapTo(motion.hiddenScale)
            alpha.snapTo(0f)
            mounted = false
            return
        }
        coroutineScope {
            val s = launch {
                scale.animateTo(motion.hiddenScale, tweenEasing(motion.scaleEasing, motion.scaleDurationMillis))
            }
            alpha.animateTo(0f, tweenEasing(motion.alphaEasing, motion.alphaDurationMillis))
            s.join()
        }
        mounted = false
    }

    /** Consumed by the finished-callback guard: one notification per real exit. */
    fun consumeWasShown() {
        wasShown = false
    }
}

private fun <T> tweenEasing(easing: androidx.compose.animation.core.Easing, ms: Int) =
    androidx.compose.animation.core.tween<T>(durationMillis = ms, easing = easing)

@Composable
private fun animatorScaleZero(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f,
            ) == 0f
        }.getOrDefault(false)
    }
}

/** Shared mounting and cancellation mechanics; each era supplies its own motion and content. */
@Composable
internal fun LegacyDialogWindow(
    motion: LegacyDialogMotion,
    visible: Boolean,
    onDismiss: () -> Unit,
    onDismissFinished: () -> Unit = {},
    modifier: Modifier = Modifier,
    usePlatformDefaultWidth: Boolean = false,
    content: @Composable () -> Unit,
) {
    val transition = remember(motion) { LegacyDialogTransition(motion) }
    val snap = animatorScaleZero()
    val latestVisible by rememberUpdatedState(visible)
    val latestOnDismissFinished by rememberUpdatedState(onDismissFinished)

    LaunchedEffect(visible) {
        if (visible) {
            transition.show(snap)
        } else if (transition.mounted) {
            transition.hide(snap)
            // Fires exactly once per real exit: only when a previous show
            // happened and the request is still false after the animation.
            if (transition.wasShown && !latestVisible) {
                transition.consumeWasShown()
                latestOnDismissFinished()
            }
        }
    }

    if (transition.mounted) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = usePlatformDefaultWidth,
            ),
        ) {
            Box(
                modifier = modifier
                    .graphicsLayer {
                        scaleX = transition.scale.value
                        scaleY = transition.scale.value
                        this.alpha = transition.alpha.value
                    },
            ) {
                content()
            }
        }
    }
}
