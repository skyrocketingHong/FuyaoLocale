package ing.fuyaoskyrocket.applocale.ui.designsystem.legacy

import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

/**
 * The drawable control-state source of truth: pointer press and keyboard/semantic
 * focus come from one [InteractionSource] so the selector layer can never
 * disagree with the gesture layer. Checked/selected/activated stay owned by
 * the caller's model and merge in here; window focus stays true (the app
 * window is focused whenever its drawables render).
 */
@Composable
fun legacyControlState(
    source: InteractionSource,
    enabled: Boolean = true,
    checked: Boolean = false,
    selected: Boolean = false,
    activated: Boolean = false,
): LegacyControlState {
    val pressed by source.collectIsPressedAsState()
    val focused by source.collectIsFocusedAsState()
    return LegacyControlState(
        enabled = enabled,
        pressed = pressed,
        focused = focused,
        checked = checked,
        selected = selected,
        activated = activated,
    )
}
