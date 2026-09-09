/* Material 3 geometry and color roles derived from AndroidX Switch.kt, Apache-2.0. */
package ing.fuyaoskyrocket.applocale.ui.designsystem.material

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.rememberSwitchDragState
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.switchDrag

/** The pinned native M3 switch is tap-only; retain its roles/geometry and add continuous drag. */
@Composable
internal fun DraggableMaterialSwitch(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?, modifier: Modifier, enabled: Boolean) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val colors = SwitchDefaults.colors()
    val state = rememberSwitchDragState(checked, MaterialTheme.motionScheme.fastSpatialSpec())
    val diameter by animateDpAsState(if (pressed || state.dragging) 28.dp else if (checked) 24.dp else 16.dp,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(), label = "SwitchHandleSize")
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val travel = with(LocalDensity.current) { 20.dp.toPx() }
    val click = if (onCheckedChange == null) Modifier else Modifier.toggleable(checked, interaction, null,
        enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange)
    Canvas(modifier.sizeIn(minWidth = 52.dp, minHeight = 48.dp).then(click)
        .switchDrag(state, checked, enabled, travel, rtl, onCheckedChange)
        .indication(interaction, ripple(bounded = false, radius = 24.dp))
        .wrapContentSize(Alignment.Center).size(52.dp, 32.dp)) {
        val fraction = state.fraction.coerceIn(0f, 1f)
        val track = lerp(if (enabled) colors.uncheckedTrackColor else colors.disabledUncheckedTrackColor,
            if (enabled) colors.checkedTrackColor else colors.disabledCheckedTrackColor, fraction)
        val thumb = lerp(if (enabled) colors.uncheckedThumbColor else colors.disabledUncheckedThumbColor,
            if (enabled) colors.checkedThumbColor else colors.disabledCheckedThumbColor, fraction)
        val border = lerp(if (enabled) colors.uncheckedBorderColor else colors.disabledUncheckedBorderColor,
            if (enabled) colors.checkedBorderColor else colors.disabledCheckedBorderColor, fraction)
        drawRoundRect(track, cornerRadius = CornerRadius(size.height / 2))
        val stroke = 2.dp.toPx()
        drawRoundRect(border, Offset(stroke / 2, stroke / 2), Size(size.width - stroke, size.height - stroke),
            CornerRadius((size.height - stroke) / 2), style = Stroke(stroke))
        val physical = if (rtl) 1f - fraction else fraction
        drawCircle(thumb, radius = diameter.toPx() / 2, center = Offset(16.dp.toPx() + travel * physical, size.height / 2))
    }
}
