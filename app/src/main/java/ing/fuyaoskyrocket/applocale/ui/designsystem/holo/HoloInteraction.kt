package ing.fuyaoskyrocket.applocale.ui.designsystem.holo

import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.runtime.Composable
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.legacyControlState

@Composable
fun holoControlState(
    source: InteractionSource,
    enabled: Boolean = true,
    checked: Boolean = false,
    selected: Boolean = false,
    activated: Boolean = false,
): HoloControlState = legacyControlState(source, enabled, checked, selected, activated)
