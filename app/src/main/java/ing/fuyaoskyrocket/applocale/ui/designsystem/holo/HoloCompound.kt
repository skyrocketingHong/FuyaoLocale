package ing.fuyaoskyrocket.applocale.ui.designsystem.holo

import androidx.compose.ui.draw.clipToBounds
import kotlin.math.roundToInt
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.rememberSwitchDragState
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.switchDrag
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import ing.fuyaoskyrocket.applocale.R
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.rememberTextMeasurer

/**
 * The intrinsic-size compound-button glyphs. Drawn at their density-scaled
 * intrinsic size inside a 48dp touch/metrics slot, so large text scales the
 * layout, never the glyph.
 */
@Composable
internal fun HoloCompoundGlyph(
    drawableRes: HoloAsset,
    controlState: HoloControlState,
    modifier: Modifier = Modifier,
) {
    val drawable = rememberHoloDrawable(drawableRes)
    // The drawable is the background: the glyph draws at its intrinsic size,
    // centered in the 48dp metrics slot.
    Box(
        modifier = modifier
            .size(48.dp)
            .holoDrawable(
                drawable,
                controlState,
                boundsMode = HoloDrawableBounds.Intrinsic,
                alignment = Alignment.Center,
            ),
    )
}

@Composable
internal fun HoloCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val controlState = holoControlState(interactionSource, enabled = enabled, checked = checked)
    // A null callback renders the glyph as a row attachment: no toggleable,
    // no second click target — the owning row keeps the real action.
    val base = if (onCheckedChange != null) {
        modifier.toggleable(
            value = checked,
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            role = Role.Checkbox,
            onValueChange = onCheckedChange,
        )
    } else {
        modifier
    }
    HoloCompoundGlyph(HoloAsset.Checkbox, controlState, base)
}

@Composable
internal fun HoloRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val controlState = holoControlState(interactionSource, enabled = enabled, checked = selected)
    val base = if (onClick != null) {
        modifier.selectable(
            selected = selected,
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            role = Role.RadioButton,
            onClick = onClick,
        )
    } else {
        modifier
    }
    HoloCompoundGlyph(HoloAsset.RadioButton, controlState, base)
}

/**
 * The Widget.Holo.CompoundButton.Switch: the imported switch track/thumb
 * selectors with the ON/OFF text (capital_on/capital_off resolved from the
 * system locale exactly like the framework) drawn inside the thumb,
 * thumbTextPadding 12dp, track min width 96dp. Tap and drag each report
 * exactly one final value; a cancelled drag snaps back to the caller's model.
 */
@Composable
internal fun HoloSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val metrics = LocalHoloMetrics.current
    val colors = LocalHoloTextColors.current
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val track = rememberHoloDrawable(HoloAsset.SwitchTrack)
    val thumb = rememberHoloDrawable(HoloAsset.SwitchThumb)
    val textOn = stringResource(R.string.holo_ics_switch_on)
    val textOff = stringResource(R.string.holo_ics_switch_off)
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val labelStyle = androidx.compose.ui.text.TextStyle(
        fontFamily = ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.legacyFontFamily(LocalHoloFontFamily.current), fontSize = 14.sp)
    val labels = listOf(textMeasurer.measure(textOn, labelStyle), textMeasurer.measure(textOff, labelStyle))
    val thumbPadding = remember(thumb) { android.graphics.Rect().also { thumb.getPadding(it) } }
    val trackPadding = remember(track) { android.graphics.Rect().also { track.getPadding(it) } }
    val geometry = holoSwitchGeometry(
        with(density) { metrics.switchMinWidth.toPx() }, labels.maxOf { it.size.width }.toFloat(),
        with(density) { metrics.switchThumbTextPadding.toPx() }, thumb.intrinsicWidth.coerceAtLeast(0).toFloat(),
        (thumbPadding.left + thumbPadding.right).toFloat(), (trackPadding.left + trackPadding.right).toFloat())
    val heightPx = maxOf(track.intrinsicHeight, thumb.intrinsicHeight,
        labels.maxOf { it.size.height } + thumbPadding.top + thumbPadding.bottom)
    val width = with(density) { geometry.width.toDp() }
    val height = with(density) { heightPx.toDp() }
    val drag = rememberSwitchDragState(checked)
    val visualChecked by remember(drag) { androidx.compose.runtime.derivedStateOf { drag.fraction >= .5f } }
    val controlState = holoControlState(interactionSource, enabled = enabled, checked = visualChecked)
        .let { it.copy(pressed = it.pressed || drag.dragging) }
    val click = if (onCheckedChange == null) Modifier else Modifier.toggleable(checked, interactionSource, null,
        enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange)
    Box(modifier.sizeIn(minWidth = width, minHeight = maxOf(48.dp, height)).then(click)
        .switchDrag(drag, checked, enabled, geometry.travel, rtl, onCheckedChange), contentAlignment = Alignment.Center) {
        Box(Modifier.size(width, height).clipToBounds().holoBackground(track, controlState),
            contentAlignment = androidx.compose.ui.AbsoluteAlignment.TopLeft) {
            Box(Modifier.absoluteOffset { androidx.compose.ui.unit.IntOffset(geometry.thumbLeft(drag.fraction, rtl).roundToInt(), 0) }
                .size(with(density) { geometry.thumbWidth.toDp() }, height)
                .holoBackground(thumb, controlState), contentAlignment = Alignment.Center) {
                BasicText(if (visualChecked) textOn else textOff,
                    style = labelStyle.copy(color = if (enabled) colors.primary else colors.primaryDisabled), maxLines = 1)
            }
        }
    }
}
