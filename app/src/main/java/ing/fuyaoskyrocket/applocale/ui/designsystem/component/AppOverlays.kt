package ing.fuyaoskyrocket.applocale.ui.designsystem.component

import ing.fuyaoskyrocket.applocale.ui.designsystem.lollipop.*
import ing.fuyaoskyrocket.applocale.ui.designsystem.eclair.*
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppControlFamily
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.LocalGlassBackdrop
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloDialog
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloMenu
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloMenuItem
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloToastHost
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloToastQueue
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloAlertScaffold
import ing.fuyaoskyrocket.applocale.ui.designsystem.glassNavigationBarClearance
import ing.fuyaoskyrocket.applocale.ui.designsystem.isGlassNavigationBarVisible

/**
 * Theme-aware alert dialog. The Miuix style shows the native miuix window dialog
 * with its own button row; the other styles keep the Material 3 alert dialog.
 *
 * The wrapper stays in composition while hidden so the exit survives: [onDismiss]
 * only asks the parent to flip [visible] back to false, and [onDismissFinished]
 * runs once after the dialog has actually finished closing (never for a dialog
 * that was never shown).
 */
@Composable
fun AppAlertDialog(
    visible: Boolean,
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    dismissText: String? = null,
    onDismiss: () -> Unit,
    onDismissFinished: () -> Unit = {},
) {
    val latestVisible by rememberUpdatedState(visible)
    val latestOnDismissFinished by rememberUpdatedState(onDismissFinished)
    // Once a dismiss is in flight the latest visible is false; reject every
    // confirm/cancel entry from then on. Business actions are triggered by the
    // buttons only, never by the finished callback.
    val guardedConfirm: () -> Unit = { if (latestVisible) onConfirm() }
    val guardedDismiss: () -> Unit = { if (latestVisible) onDismiss() }
    if (AppUiTheme.policy.controls == AppControlFamily.Lollipop) {
        CompositionLocalProvider(LocalGlassBackdrop provides null) {
            LollipopDialog(visible, guardedDismiss, { if (!latestVisible) latestOnDismissFinished() }) {
                LollipopAlertContent(title, message, confirmText, guardedConfirm, dismissText, guardedDismiss)
            }
        }
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Eclair) {
        CompositionLocalProvider(LocalGlassBackdrop provides null) {
            EclairDialog(visible, guardedDismiss, { if (!latestVisible) latestOnDismissFinished() }) {
                EclairAlertContent(title, message, confirmText, guardedConfirm, dismissText, guardedDismiss)
            }
        }
        return
    }
    if (AppUiTheme.policy.picker == ing.fuyaoskyrocket.applocale.ui.designsystem.AppPickerPresentation.HoneycombDialog) {
        CompositionLocalProvider(LocalGlassBackdrop provides null) {
            HoloDialog(visible, guardedDismiss, { if (!latestVisible) latestOnDismissFinished() }) {
                ing.fuyaoskyrocket.applocale.ui.designsystem.honeycomb.HoneycombAlertContent(
                    title, message, confirmText, guardedConfirm, dismissText, guardedDismiss,
                )
            }
        }
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Holo) {
        // The era alert: floating dialog frame, 2dp title divider, divided
        // button bar (negative left / positive right); outside/back only
        // request dismissal, the finished callback fires once after the exit.
        CompositionLocalProvider(LocalGlassBackdrop provides null) {
            HoloDialog(
                visible = visible,
                onDismiss = guardedDismiss,
                onDismissFinished = {
                    if (!latestVisible) latestOnDismissFinished()
                },
            ) {
                HoloAlertScaffold(
                    title = title,
                    message = message,
                    confirmText = confirmText,
                    onConfirm = guardedConfirm,
                    dismissText = dismissText,
                    onDismiss = guardedDismiss,
                )
            }
        }
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Miuix) {
        // The miuix window owns its mounting: it keeps the window alive until the
        // exit animation ends, cancels that exit when show turns true again, and
        // skips onDismissFinished for a cancelled close.
        top.yukonga.miuix.kmp.window.WindowDialog(
            show = visible,
            title = title,
            summary = message,
            onDismissRequest = guardedDismiss,
            onDismissFinished = {
                if (!latestVisible) latestOnDismissFinished()
            },
        ) {
            // The dialog lives in its own window; see AppModalBottomSheet for why
            // the glass backdrop is cleared here.
            CompositionLocalProvider(LocalGlassBackdrop provides null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AppSpacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    if (dismissText != null) {
                        AppFilledTonalButton(
                            text = dismissText,
                            onClick = guardedDismiss,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    AppButton(
                        text = confirmText,
                        onClick = guardedConfirm,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    } else {
        // The Material alert dialog has no dismiss-finished callback: the window
        // is removed the moment visible turns false, so notify once from an effect
        // after the recomposition that dropped it. The gate keeps a dialog that
        // was never shown silent.
        var wasShown by remember { mutableStateOf(false) }
        LaunchedEffect(visible) {
            if (visible) {
                wasShown = true
            } else if (wasShown) {
                wasShown = false
                latestOnDismissFinished()
            }
        }
        if (visible) {
            // Dialogs render in their own window; see AppModalBottomSheet for why the glass
            // backdrop is cleared here.
            CompositionLocalProvider(LocalGlassBackdrop provides null) {
                if (AppUiTheme.policy.controls == AppControlFamily.Material2) {
                    androidx.compose.material.AlertDialog(
                        onDismissRequest = guardedDismiss,
                        title = { androidx.compose.material.Text(title) },
                        text = { androidx.compose.material.Text(message) },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(AppUiTheme.shapes.dialog.radius),
                        confirmButton = { androidx.compose.material.TextButton(onClick = guardedConfirm) { androidx.compose.material.Text(confirmText) } },
                        dismissButton = dismissText?.let { label -> { androidx.compose.material.TextButton(onClick = guardedDismiss) { androidx.compose.material.Text(label) } } },
                    )
                } else AlertDialog(
                    title = { Text(text = title) },
                    text = { Text(text = message) },
                    onDismissRequest = guardedDismiss,
                    confirmButton = {
                        androidx.compose.material3.TextButton(onClick = guardedConfirm) {
                            Text(text = confirmText)
                        }
                    },
                    dismissButton = dismissText?.let { label ->
                        {
                            androidx.compose.material3.TextButton(onClick = guardedDismiss) {
                                Text(text = label)
                            }
                        }
                    },
                )
            }
        }
    }
}

/**
 * Snackbar state that fans messages out to whichever host implementation the
 * active theme renders, keeping the plain [showSnackbar] call sites theme-agnostic.
 */
class AppSnackbarHostState(internal var controls: AppControlFamily = AppControlFamily.Material3) {
    internal val materialState = SnackbarHostState()
    internal val material2State = androidx.compose.material.SnackbarHostState()
    internal val miuixState = top.yukonga.miuix.kmp.basic.SnackbarHostState()
    internal val holoQueue = HoloToastQueue()
    internal val lollipopQueue = ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.LegacyMessageQueue()
    internal val eclairQueue = ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.LegacyMessageQueue()

    suspend fun showSnackbar(message: String) {
        // Suspends until the active backend finished presenting the message;
        // the holo queue completes waiters even when its host dies mid-show.
        when (controls) {
            AppControlFamily.Miuix -> miuixState.showSnackbar(message)
            AppControlFamily.Lollipop -> lollipopQueue.show(message)
            AppControlFamily.Eclair -> eclairQueue.show(message)
            AppControlFamily.Holo -> holoQueue.show(message)
            AppControlFamily.Material2 -> material2State.showSnackbar(message)
            AppControlFamily.Material3 -> materialState.showSnackbar(message)
        }
    }
}

@Composable
fun rememberAppSnackbarHostState(): AppSnackbarHostState {
    val controls = AppUiTheme.policy.controls
    val state = remember { AppSnackbarHostState(controls) }
    SideEffect { state.controls = controls }
    return state
}

/**
 * Theme-aware snackbar host: miuix snackbar for the Miuix style, Material 3 otherwise.
 *
 * [hostSlotBottom] is the bottom-slot height the host scaffold has already reserved
 * below this host (the home screen's batch bar); the glass clearance only adds the
 * remainder of the glass footprint on top of it, never the full clearance twice.
 */
@Composable
fun AppSnackbarHost(
    state: AppSnackbarHostState,
    modifier: Modifier = Modifier,
    hostSlotBottom: Dp = 0.dp,
) {
    // The floating Liquid Glass tab bar would cover a bottom-anchored snackbar.
    val clearanceModifier = if (isGlassNavigationBarVisible()) {
        modifier.padding(bottom = maxOf(0.dp, glassNavigationBarClearance() - hostSlotBottom))
    } else {
        modifier
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Material2) {
        androidx.compose.material.SnackbarHost(state.material2State, clearanceModifier)
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Lollipop) {
        LollipopToastHost(state.lollipopQueue, clearanceModifier)
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Eclair) {
        EclairToastHost(state.eclairQueue, clearanceModifier)
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Holo) {
        HoloToastHost(queue = state.holoQueue, modifier = clearanceModifier)
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Miuix) {
        top.yukonga.miuix.kmp.basic.SnackbarHost(
            state = state.miuixState,
            modifier = clearanceModifier,
        )
    } else {
        androidx.compose.material3.SnackbarHost(
            hostState = state.materialState,
            modifier = clearanceModifier,
        )
    }
}

/** One entry of an [AppDropdownMenu]. */
data class AppDropdownItem(
    val text: String,
    val selected: Boolean = false,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)

/**
 * Theme-aware dropdown menu anchored to the caller's layout position. The Miuix style
 * shows the native miuix list popup with its dropdown rows; the other styles keep
 * the Material 3 dropdown menu.
 */
@Composable
fun AppDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    items: List<AppDropdownItem>,
) {
    if (AppUiTheme.policy.controls == AppControlFamily.Material2) {
        CompositionLocalProvider(LocalGlassBackdrop provides null) {
            androidx.compose.material.DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
                items.forEach { item ->
                    androidx.compose.material.DropdownMenuItem(onClick = item.onClick, enabled = item.enabled) {
                        androidx.compose.material.Text(item.text, Modifier.weight(1f))
                        if (item.selected) androidx.compose.material.Icon(Icons.Outlined.Check, null)
                    }
                }
            }
        }
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Lollipop) {
        CompositionLocalProvider(LocalGlassBackdrop provides null) { LollipopMenu(expanded, onDismiss, items) }
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Eclair) {
        CompositionLocalProvider(LocalGlassBackdrop provides null) { EclairMenu(expanded, onDismiss, items) }
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Holo) {
        CompositionLocalProvider(LocalGlassBackdrop provides null) {
            HoloMenu(
                expanded = expanded,
                onDismiss = onDismiss,
                items = items.map { item ->
                    HoloMenuItem(
                        text = item.text,
                        selected = item.selected,
                        enabled = item.enabled,
                        onClick = item.onClick,
                    )
                },
            )
        }
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Miuix) {
        top.yukonga.miuix.kmp.window.WindowListPopup(
            show = expanded,
            onDismissRequest = onDismiss,
        ) {
            // The popup lives in its own window; see AppModalBottomSheet for why
            // the glass backdrop is cleared here.
            CompositionLocalProvider(LocalGlassBackdrop provides null) {
                top.yukonga.miuix.kmp.basic.ListPopupColumn {
                    items.forEachIndexed { index, item ->
                        androidx.compose.runtime.key(index) {
                            Box(if (item.enabled) Modifier else Modifier
                                .alpha(.38f).clearAndSetSemantics {
                                    contentDescription = item.text
                                    disabled()
                                }) {
                            top.yukonga.miuix.kmp.basic.DropdownImpl(
                                text = item.text,
                                optionSize = items.size,
                                isSelected = item.selected,
                                index = index,
                            ) {
                                if (item.enabled) item.onClick()
                            }
                            }
                        }
                    }
                }
            }
        }
    } else {
        CompositionLocalProvider(LocalGlassBackdrop provides null) {
            androidx.compose.material3.DropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismiss,
            ) {
                items.forEach { item ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(text = item.text) },
                        onClick = item.onClick,
                        enabled = item.enabled,
                        trailingIcon = if (item.selected) {
                            {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                )
                            }
                        } else {
                            null
                        },
                    )
                }
            }
        }
    }
}
