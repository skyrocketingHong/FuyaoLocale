package ing.fuyaoskyrocket.applocale.ui.designsystem.lollipop

import android.graphics.drawable.InsetDrawable
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.*
import kotlin.math.cos
import kotlin.math.PI
import java.util.Locale

/** Widget.Material.Button: 48dp target, 36dp visible surface, 2dp corners, 1+2dp elevation. */
@Composable
internal fun LollipopButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    enabled: Boolean = true, borderless: Boolean = false) {
    val colors = LocalLollipopColors.current
    val interaction = remember { MutableInteractionSource() }
    val state = legacyControlState(interaction, enabled = enabled)
    val z by animateFloatAsState(if (!enabled || borderless) 0f else if (state.pressed) 3f else 1f,
        tween(100, if (state.pressed) 0 else 100, Easing { (cos((it + 1f) * PI) / 2 + .5).toFloat() }), label = "2014 button elevation")
    val drawable = rememberLollipopDrawable(R.drawable.lollipop_btn_default_mtrl_shape)
    val inner = remember(drawable) { requireNotNull((drawable as InsetDrawable).drawable) }
    val labelColor = if (borderless) LocalLollipopContentColor.current.takeOrElse { colors.accent } else colors.foreground
    Box(modifier.heightIn(min = 48.dp).widthIn(min = if (borderless) 64.dp else 88.dp)
        .clickable(interaction, null, enabled = enabled, role = Role.Button, onClick = onClick), contentAlignment = Alignment.Center) {
        Box(Modifier.matchParentSize().padding(horizontal = if (borderless) 0.dp else 4.dp, vertical = 6.dp)
            .shadow(z.dp, RoundedCornerShape(2.dp)).clip(RoundedCornerShape(2.dp))
            .then(if (borderless) Modifier else Modifier.legacyBackground(inner, state))
            .indication(interaction, LollipopRipple(colors.ripple)))
        BasicText(text.uppercase(Locale.getDefault()), Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            style = lollipopTextStyle(14, FontWeight.Medium).copy(color = labelColor.copy(alpha = labelColor.alpha * if (enabled) 1f else .3f)))
    }
}

@Composable
internal fun LollipopActionButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
    content: @Composable () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Box(modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
        .clickable(interaction, LollipopRipple(LocalLollipopColors.current.ripple, bounded = false),
            enabled = enabled, role = Role.Button, onClick = onClick), contentAlignment = Alignment.Center) {
        Box(Modifier.graphicsLayer { alpha = if (enabled) 1f else .3f }) { content() }
    }
}

@Composable
internal fun LollipopCheckbox(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier, enabled: Boolean = true) = LollipopCheckGlyph(checked, enabled, modifier,
    R.drawable.lollipop_btn_check_material_anim, onCheckedChange = onCheckedChange)

@Composable
internal fun LollipopRadioButton(selected: Boolean, onClick: (() -> Unit)?,
    modifier: Modifier = Modifier, enabled: Boolean = true) = LollipopCheckGlyph(selected, enabled, modifier,
    R.drawable.lollipop_btn_radio_material_anim, onClick = onClick, radio = true)

@Composable
private fun LollipopCheckGlyph(checked: Boolean, enabled: Boolean, modifier: Modifier, id: Int,
    onCheckedChange: ((Boolean) -> Unit)? = null, onClick: (() -> Unit)? = null, radio: Boolean = false) {
    val interaction = remember { MutableInteractionSource() }
    val state = legacyControlState(interaction, enabled = enabled, checked = checked)
    val drawable = rememberLollipopDrawable(id, state)
    val indication = LollipopRipple(LocalLollipopColors.current.ripple, bounded = false)
    val click = when {
        radio && onClick != null -> Modifier.selectable(checked, interaction, indication, enabled, Role.RadioButton, onClick)
        !radio && onCheckedChange != null -> Modifier.toggleable(checked, interaction, indication, enabled, Role.Checkbox, onCheckedChange)
        else -> Modifier
    }
    Box(modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp).then(click)
        .legacyDrawable(drawable, state, boundsMode = LegacyDrawableBounds.Intrinsic))
}

/** Switch.java's 250ms thumb translation plus the original 12-frame drawable transition. */
@Composable
internal fun LollipopSwitch(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier, enabled: Boolean = true) {
    val interaction = remember { MutableInteractionSource() }
    val state = legacyControlState(interaction, enabled = enabled, checked = checked)
    val thumb = rememberLollipopDrawable(R.drawable.lollipop_switch_thumb_material_anim, state)
    val track = rememberLollipopDrawable(R.drawable.lollipop_switch_track_material, state)
    val drawable = remember(thumb, track) { LollipopSwitchDrawable(thumb, track) }
    val position = remember { Animatable(if (checked) 1f else 0f) }
    var dragging by remember { mutableStateOf(false) }
    var dragPosition by remember { mutableFloatStateOf(position.value) }
    val latestCallback by rememberUpdatedState(onCheckedChange)
    val latestChecked by rememberUpdatedState(checked)
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val density = LocalDensity.current
    LaunchedEffect(checked, dragging) {
        if (!dragging) position.animateTo(if (checked) 1f else 0f, tween(250,
            easing = Easing { (cos((it + 1f) * PI) / 2 + .5).toFloat() }))
    }
    val scope = rememberCoroutineScope()
    val dragModifier = if (onCheckedChange == null || !enabled) Modifier else Modifier.pointerInput(drawable, rtl) {
        val tracker = VelocityTracker()
        detectHorizontalDragGestures(
            onDragStart = { dragging = true; dragPosition = position.value; tracker.resetTracking() },
            onDragCancel = { dragging = false },
            onDragEnd = {
                val velocity = tracker.calculateVelocity().x * if (rtl) -1f else 1f
                val target = if (kotlin.math.abs(velocity) > with(density) { 50.dp.toPx() }) velocity > 0 else dragPosition > .5f
                if (target != latestChecked) latestCallback?.invoke(target)
                dragging = false
            },
            onHorizontalDrag = { change, delta ->
                change.consume()
                tracker.addPosition(change.uptimeMillis, change.position)
                dragPosition = (dragPosition + delta * (if (rtl) -1f else 1f) / drawable.thumbTravelPx).coerceIn(0f, 1f)
                scope.launch { position.snapTo(dragPosition) }
            })
    }
    Box(modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp).then(dragModifier)
        .then(if (onCheckedChange != null) Modifier.toggleable(checked, interaction,
            LollipopRipple(LocalLollipopColors.current.ripple, false), enabled, Role.Switch, onCheckedChange) else Modifier)
        .legacyDrawable(drawable, state, levelProvider = { (position.value * 10000).toInt() }, boundsMode = LegacyDrawableBounds.Intrinsic))

}

@Composable
internal fun LollipopDropdownButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val state = legacyControlState(interaction)
    Box(modifier.heightIn(min = 48.dp)
        .legacyBackground(rememberLollipopDrawable(R.drawable.lollipop_spinner_background_material), state)
        .clickable(interaction, LollipopRipple(LocalLollipopColors.current.ripple), onClick = onClick)
        .padding(start = 8.dp, end = 32.dp), contentAlignment = Alignment.CenterStart) {
        BasicText(text, style = lollipopTextStyle(16).copy(color = LocalLollipopColors.current.foreground))
    }
}
