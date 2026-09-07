package ing.fuyaoskyrocket.applocale.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemePreferences
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemeStyle
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppButton
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppLinearProgressIndicator
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSurface

/**
 * Selection-mode bottom bar. Material You keeps the M3 bottom app bar and the Miuix
 * style renders a miuix surface with the miuix button.
 * The selection count remains in the contextual top app bar, leaving enough
 * width here for an unclipped primary action in every supported language.
 *
 * Presentation (round-5 021): the whole native bar — container plate included —
 * fades and slides in through [presentationProgress] (read inside the graphics
 * layer only, so the animation costs no per-frame recomposition here);
 * [interactive] is the real business gate (selection mode AND the host dock has
 * vacated), never the alpha value. The bar's height never follows its content
 * state: the action button always participates in measurement and the applying
 * progress cross-fades inside the same box, so nothing jumps the last list row.
 */
@Composable
fun BatchBottomAppBar(
    hasSelection: Boolean,
    isApplying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    presentationProgress: () -> Float = { 1f },
    interactive: Boolean = true,
) {
    val presentationModifier = modifier.graphicsLayer {
        val progress = presentationProgress()
        alpha = progress
        translationY = PresentationSlideIn.toPx() * (1f - progress)
    }
    if (AppThemePreferences.style == AppThemeStyle.MIUIX) {
        AppSurface(modifier = presentationModifier) {
            BatchBarContent(
                hasSelection = hasSelection,
                isApplying = isApplying,
                presentationProgress = presentationProgress,
                interactive = interactive,
                onClick = onClick,
                // Insets and paddings sit on the constant box rather than the
                // button, so the container height is identical in every state.
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
            )
        }
    } else {
        BottomAppBar(modifier = presentationModifier) {
            BatchBarContent(
                hasSelection = hasSelection,
                isApplying = isApplying,
                presentationProgress = presentationProgress,
                interactive = interactive,
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.lg),
            )
        }
    }
}

/** The presentation slide-in offset; the bar itself keeps its full measured height. */
private val PresentationSlideIn = 8.dp

/** The button/progress swap duration while a batch apply starts or ends. */
private const val ApplyCrossFadeMillis = 120

/**
 * Constant-size host for the batch action: the button keeps its measured size in
 * every state and the progress indicator cross-fades over the same box while
 * applying (weights only — the container never moves). The progress indicator is
 * composed only while applying or while its own exit fade is still visible, so no
 * idle animation runs behind an invisible bar.
 */
@Composable
private fun BatchBarContent(
    hasSelection: Boolean,
    isApplying: Boolean,
    presentationProgress: () -> Float,
    interactive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val buttonWeight by animateFloatAsState(
        targetValue = if (isApplying) 0f else 1f,
        animationSpec = tween(ApplyCrossFadeMillis, easing = FastOutSlowInEasing),
        label = "batchApplyCrossFade",
    )
    // Crossing-derived (not per-frame): flips once each way around half
    // presentation, gating semantics and clicks for the half-hidden bar.
    val presentationInteractive by remember {
        derivedStateOf { presentationProgress() >= 0.5f }
    }
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        AppButton(
            text = stringResource(R.string.set_language),
            onClick = onClick,
            // Inert but still measured while hidden, applying, or gated by the
            // dock hand-off: disabled semantics carry the state, no invisible
            // hit area and no keyboard activation.
            enabled = hasSelection && interactive && !isApplying && presentationInteractive,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { alpha = buttonWeight },
        )
        if (isApplying || buttonWeight < 0.99f) {
            AppLinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { alpha = 1f - buttonWeight },
            )
        }
    }
}
