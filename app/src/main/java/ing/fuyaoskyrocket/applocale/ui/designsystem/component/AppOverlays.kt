package ing.fuyaoskyrocket.applocale.ui.designsystem.component

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemePreferences
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemeStyle
import ing.fuyaoskyrocket.applocale.ui.designsystem.LocalGlassBackdrop
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
    if (AppThemePreferences.style == AppThemeStyle.MIUIX) {
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
                AlertDialog(
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
class AppSnackbarHostState {
    internal val materialState = SnackbarHostState()
    internal val miuixState = top.yukonga.miuix.kmp.basic.SnackbarHostState()

    suspend fun showSnackbar(message: String) {
        if (AppThemePreferences.style == AppThemeStyle.MIUIX) {
            miuixState.showSnackbar(message)
        } else {
            materialState.showSnackbar(message)
        }
    }
}

@Composable
fun rememberAppSnackbarHostState(): AppSnackbarHostState = remember { AppSnackbarHostState() }

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
    if (AppThemePreferences.style == AppThemeStyle.MIUIX) {
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
    if (AppThemePreferences.style == AppThemeStyle.MIUIX) {
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
                            top.yukonga.miuix.kmp.basic.DropdownImpl(
                                text = item.text,
                                optionSize = items.size,
                                isSelected = item.selected,
                                index = index,
                            ) {
                                item.onClick()
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
