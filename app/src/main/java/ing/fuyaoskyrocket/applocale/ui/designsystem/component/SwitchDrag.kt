package ing.fuyaoskyrocket.applocale.ui.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch

internal fun switchDragFraction(start: Float, delta: Float, travel: Float, rtl: Boolean): Float =
    if (travel <= 0f) start else (start + delta / travel * if (rtl) -1f else 1f).coerceIn(0f, 1f)

/** Continuous movement belongs to the control; only release submits a boolean change. */
@Stable
internal class SwitchDragState(initialChecked: Boolean) {
    val position = Animatable(if (initialChecked) 1f else 0f)
    var dragging by mutableStateOf(false)
    var draggedFraction by mutableFloatStateOf(position.value)
    val fraction: Float get() = if (dragging) draggedFraction else position.value
}

@Composable
internal fun rememberSwitchDragState(checked: Boolean, animationSpec: FiniteAnimationSpec<Float> = tween(180)): SwitchDragState {
    val state = remember { SwitchDragState(checked) }
    LaunchedEffect(checked, state.dragging) {
        if (!state.dragging) state.position.animateTo(if (checked) 1f else 0f, animationSpec)
    }
    return state
}

@Composable
internal fun Modifier.switchDrag(
    state: SwitchDragState,
    checked: Boolean,
    enabled: Boolean,
    travel: Float,
    rtl: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
): Modifier {
    val latestChecked by rememberUpdatedState(checked)
    val callback by rememberUpdatedState(onCheckedChange)
    val scope = rememberCoroutineScope()
    return if (!enabled || onCheckedChange == null) this else pointerInput(state, enabled, travel, rtl) {
        detectHorizontalDragGestures(
            onDragStart = {
                state.draggedFraction = state.position.value
                state.dragging = true
            },
            onHorizontalDrag = { change, delta ->
                change.consume()
                state.draggedFraction = switchDragFraction(state.draggedFraction, delta, travel, rtl)
            },
            onDragEnd = {
                val target = state.draggedFraction >= .5f
                scope.launch {
                    state.position.snapTo(state.draggedFraction)
                    if (target != latestChecked) callback?.invoke(target)
                    state.dragging = false
                }
            },
            onDragCancel = {
                scope.launch {
                    state.position.snapTo(state.draggedFraction)
                    state.dragging = false
                }
            },
        )
    }
}
