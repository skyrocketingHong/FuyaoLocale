package ing.fuyaoskyrocket.applocale.ui.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemePreferences
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemeStyle
import ing.fuyaoskyrocket.applocale.ui.designsystem.LocalGlassBackdrop
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.layout.BottomSheetDefaults

/**
 * Theme-aware modal bottom sheet. The Miuix style shows the native miuix window
 * bottom sheet; the other styles keep the Material 3 modal sheet.
 *
 * The wrapper stays in composition while hidden so the exit animation survives:
 * [onDismiss] only asks the parent to flip [visible] back to false, and
 * [onDismissFinished] runs once after the window has actually finished closing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppModalBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    onDismissFinished: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val latestVisible by rememberUpdatedState(visible)
    val latestOnDismissFinished by rememberUpdatedState(onDismissFinished)
    if (AppThemePreferences.style == AppThemeStyle.MIUIX) {
        // The miuix window owns its mounting: it keeps the window alive until the
        // exit animation ends, cancels that exit when show turns true again, and
        // skips onDismissFinished for a cancelled close.
        top.yukonga.miuix.kmp.window.WindowBottomSheet(
            show = visible,
            modifier = modifier,
            title = title,
            onDismissRequest = onDismiss,
            onDismissFinished = {
                if (!latestVisible) latestOnDismissFinished()
            },
            // Round-7 029: no host-level horizontal inset — the native default
            // 24dp stacked with the content's own 16/4 frame margins into a
            // 40dp double inset. The content frames itself; only the (zero)
            // vertical default is kept verbatim.
            insideMargin = DpSize(0.dp, BottomSheetDefaults.insideMargin.height),
            content = {
                // The sheet lives in its own window; see the Material branch below.
                CompositionLocalProvider(LocalGlassBackdrop provides null) {
                    content()
                }
            },
        )
    } else {
        // mounted=false: the Material sheet window is not composed at all.
        // visible=false with mounted=true: the native sheet plays its exit.
        var mounted by remember { mutableStateOf(false) }
        // A fresh mount is auto-shown by ModalBottomSheet itself once its anchors
        // exist; track it so the coordinator does not stack a competing show job.
        var justMounted by remember { mutableStateOf(false) }
        val sheetState = androidx.compose.material3.rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        )
        // Drives the sheet towards the requested visibility. This job may be
        // cancelled when a native dismiss path (scrim tap, swipe, back) or a user
        // drag takes over the sheet state's drag mutex; releasing the window is
        // owned by the observer below, which survives that cancellation.
        LaunchedEffect(visible, mounted) {
            when {
                !mounted -> {
                    if (visible) {
                        justMounted = true
                        mounted = true
                    }
                }
                visible -> {
                    if (justMounted) {
                        justMounted = false
                    } else {
                        // A reopen after a dismiss: the native auto-show effect has
                        // already run for this mount and will not fire again.
                        sheetState.show()
                    }
                }
                else -> {
                    justMounted = false
                    sheetState.hide()
                }
            }
        }
        // Releases the window exactly once after the sheet settles hidden while
        // unwanted, regardless of which coroutine animated it there.
        LaunchedEffect(mounted) {
            if (!mounted) return@LaunchedEffect
            snapshotFlow {
                SheetSettlePhase(
                    hidden = sheetState.currentValue == SheetValue.Hidden,
                    animating = sheetState.isAnimationRunning,
                    wanted = latestVisible,
                )
            }.collect { phase ->
                if (!phase.wanted && !phase.animating) {
                    if (phase.hidden) {
                        if (!latestVisible) {
                            mounted = false
                            latestOnDismissFinished()
                        }
                    } else {
                        // Settled expanded although the parent asked to close (a
                        // gesture took over mid-exit): honour the parent's intent.
                        launch { sheetState.hide() }
                    }
                }
            }
        }
        if (mounted) {
            androidx.compose.material3.ModalBottomSheet(
                onDismissRequest = onDismiss,
                modifier = modifier,
                sheetState = sheetState,
            ) {
                // The sheet lives in its own window, where the wallpaper backdrop recorded
                // in the main window would refract with the wrong coordinates. Clearing the
                // backdrop makes the glass widgets fall back to their Material rendering.
                CompositionLocalProvider(LocalGlassBackdrop provides null) {
                    if (title != null) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                // Match the horizontal inset of the sheet's row content.
                                .padding(
                                    start = AppSpacing.lg,
                                    end = AppSpacing.lg,
                                    bottom = AppSpacing.lg,
                                ),
                        )
                    }
                    content()
                }
            }
        }
    }
}

private data class SheetSettlePhase(
    val hidden: Boolean,
    val animating: Boolean,
    val wanted: Boolean,
)
