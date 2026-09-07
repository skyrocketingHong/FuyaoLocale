package ing.fuyaoskyrocket.applocale.ui.languagepicker

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.LayoutDirection
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppMotion

/**
 * A language group is a child destination; search cancellation is not. The
 * [content] receives each layer's projected groupId plus whether that layer may
 * run business callbacks right now — during entry, exit, or a predictive seek
 * both layers stay non-interactive so clicks, pins and keyboard activation
 * cannot fire twice or hit the outgoing page.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LanguageGroupTransition(
    groupId: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    backEnabled: Boolean = true,
    content: @Composable (groupId: String?, interactive: Boolean) -> Unit,
) {
    val transitionState = remember { SeekableTransitionState(groupId) }
    val transition = rememberTransition(transitionState, label = "Language group")
    var seeking by remember { mutableStateOf(false) }
    val direction = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1 else 1

    // Runs for a toolbar/selection return as well as forward navigation. On a
    // cancelled gesture the unchanged model animates back from the seek fraction.
    LaunchedEffect(groupId, seeking) {
        if (!seeking) transitionState.animateTo(groupId)
    }
    val imeVisible = WindowInsets.isImeVisible
    val latestGroupId by rememberUpdatedState(groupId)
    val latestOnBack by rememberUpdatedState(onBack)
    PredictiveBackHandler(enabled = backEnabled && groupId != null && !imeVisible) { events ->
        // Commit only if the gesture still targets the group it started from;
        // an external state change mid-gesture must not receive a stale return.
        val startGroupId = latestGroupId
        try {
            seeking = true
            events.collect { event ->
                transitionState.seekTo(event.progress.coerceIn(0f, 1f), targetState = null)
            }
            if (latestGroupId == startGroupId) latestOnBack()
        } finally {
            // Cancellation never mutates the picker selection or parent scroll.
            seeking = false
        }
    }
    transition.AnimatedContent(
        modifier = modifier,
        transitionSpec = {
            val forward = targetState != null
            val sign = if (forward) direction else -direction
            (slideInHorizontally(tween(280, easing = AppMotion.StateEasing)) { sign * it / 5 } +
                fadeIn(tween(220))) togetherWith
                (slideOutHorizontally(tween(280, easing = AppMotion.StateEasing)) { -sign * it / 5 } +
                    fadeOut(tween(180)))
        },
    ) { visibleGroup ->
        // Every state must agree: comparing only isRunning would leave the
        // outgoing page clickable on the frame where the model already moved
        // but the animation has not started yet.
        val settled = !seeking && !transition.isRunning &&
            transition.currentState == groupId && transition.targetState == groupId
        val interactive = settled && visibleGroup == groupId
        val focusManager = LocalFocusManager.current
        // Clear keyboard focus once per transition, not per frame, so Enter
        // cannot activate a row that is already leaving the screen.
        LaunchedEffect(interactive) {
            if (!interactive) focusManager.clearFocus(force = true)
        }
        Box(
            modifier = if (interactive) {
                Modifier
            } else {
                // Pointer blocking ahead of the rows plus an emptied semantics
                // subtree; the business callbacks stay gated as the last line.
                Modifier
                    .clearAndSetSemantics { }
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                event.changes.forEach { it.consume() }
                            }
                        }
                    }
            },
        ) {
            content(visibleGroup, interactive)
        }
    }
}
