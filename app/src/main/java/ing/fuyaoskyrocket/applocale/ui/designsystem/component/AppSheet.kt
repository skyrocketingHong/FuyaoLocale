package ing.fuyaoskyrocket.applocale.ui.designsystem.component

import ing.fuyaoskyrocket.applocale.ui.designsystem.lollipop.*
import ing.fuyaoskyrocket.applocale.ui.designsystem.eclair.*
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppControlFamily
import androidx.compose.foundation.layout.Box
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
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.LocalGlassBackdrop
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloDialog
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloText
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.LocalHoloMetrics
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.LocalHoloTextColors
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.LocalHoloFontFamily
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.rememberHoloDrawable
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloAsset
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
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
    if (AppUiTheme.policy.controls == AppControlFamily.Material2) {
        ing.fuyaoskyrocket.applocale.ui.designsystem.material2.RoundedModalSheet(
            visible, onDismiss, modifier, title, onDismissFinished, content,
        )
        return
    }
    if (AppUiTheme.motion.picker == ing.fuyaoskyrocket.applocale.ui.designsystem.AppPickerPresentation.Material2014Dialog) {
        CompositionLocalProvider(LocalGlassBackdrop provides null) {
            LollipopDialog(visible, onDismiss, { if (!latestVisible) latestOnDismissFinished() }) {
                LollipopChoiceContent(title) { content() }
            }
        }
        return
    }
    if (AppUiTheme.motion.picker == ing.fuyaoskyrocket.applocale.ui.designsystem.AppPickerPresentation.ClassicDialog) {
        CompositionLocalProvider(LocalGlassBackdrop provides null) {
            EclairDialog(visible, onDismiss, { if (!latestVisible) latestOnDismissFinished() }) {
                EclairChoiceContent(title) { content() }
            }
        }
        return
    }
    if (AppUiTheme.motion.picker == ing.fuyaoskyrocket.applocale.ui.designsystem.AppPickerPresentation.HoneycombDialog) {
        CompositionLocalProvider(LocalGlassBackdrop provides null) {
            HoloDialog(visible, onDismiss, { if (!latestVisible) latestOnDismissFinished() }) {
                ing.fuyaoskyrocket.applocale.ui.designsystem.honeycomb.HoneycombChoiceContent(title) { content() }
            }
        }
        return
    }
    if (AppUiTheme.motion.picker == ing.fuyaoskyrocket.applocale.ui.designsystem.AppPickerPresentation.HoloDialog) {
        // The era presentation of every catalog picker (round-9 043 §三): a
        // floating list dialog — no drag handle, no rounded bottom sheet. The
        // public contract (visible/onDismiss/onDismissFinished) is unchanged;
        // content lists scroll inside the capped window.
        CompositionLocalProvider(LocalGlassBackdrop provides null) {
            HoloDialog(
                visible = visible,
                onDismiss = onDismiss,
                onDismissFinished = {
                    if (!latestVisible) latestOnDismissFinished()
                },
            ) {
                HoloChoiceDialogContent(title = title) {
                    content()
                }
            }
        }
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Miuix) {
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


/**
 * The catalog dialog's shared frame: title (era dialog title style), the 2dp
 * blue divider, and the content constrained to a share of the screen so long
 * catalogs scroll inside the window with the title staying put.
 */
@Composable
private fun HoloChoiceDialogContent(
    title: String?,
    content: @Composable () -> Unit,
) {
    val metrics = LocalHoloMetrics.current
    val textColors = LocalHoloTextColors.current
    val frame = rememberHoloDrawable(HoloAsset.DialogFrame)
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val maxDialogWidth = minOf(configuration.screenWidthDp.dp - 16.dp, 560.dp)
    ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.LegacyDialogSurface(
        frame = frame,
        color = ing.fuyaoskyrocket.applocale.ui.designsystem.holo.holoDialogSurfaceColor(),
        modifier = Modifier
            .width(maxDialogWidth)
            .heightIn(max = configuration.screenHeightDp.dp * .85f),
    ) {
        if (title != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp)
                    .heightIn(min = metrics.alertDialogTitleHeight),
                contentAlignment = androidx.compose.ui.Alignment.CenterStart,
            ) {
                HoloText(
                    text = title,
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = LocalHoloFontFamily.current,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = textColors.primary,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(androidx.compose.ui.graphics.Color(0xff33b5e5)),
            )
        }
        Box(
            modifier = Modifier
                .weight(1f, fill = false)
                .heightIn(max = (configuration.screenHeightDp.dp * 0.75f))
                .padding(bottom = 8.dp),
        ) {
            CompositionLocalProvider(
                ing.fuyaoskyrocket.applocale.ui.designsystem.holo.LocalHoloListPadding provides 16.dp,
            ) {
                content()
            }
        }
    }
}


/**
 * The picker window's content height policy (round-9 043 §三.3): the modern
 * bottom sheets keep their 0.9-screen fraction; the Holo dialog owns the cap,
 * so its branch contributes only a defensive maximum and short catalogs wrap.
 */
@Composable
fun holoSheetHeightModifier(): Modifier =
    if (AppUiTheme.policy.picker != ing.fuyaoskyrocket.applocale.ui.designsystem.AppPickerPresentation.NativeBottomSheet) {
        Modifier
    } else {
        Modifier.fillMaxHeight(0.9f)
    }

/**
 * The catalog list inside the window: the modern sheet stretches its window,
 * the Holo dialog wraps short catalogs (the 0.75-screen cap lives in the
 * dialog frame), so the list only fills under the modern styles.
 */
@Composable
fun holoSheetListFillModifier(): Modifier =
    if (AppUiTheme.policy.picker != ing.fuyaoskyrocket.applocale.ui.designsystem.AppPickerPresentation.NativeBottomSheet) {
        Modifier
    } else {
        Modifier.fillMaxHeight()
    }
