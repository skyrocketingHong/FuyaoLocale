/*
 * Portions adapted from AOSP android-5.0.0_r1 Ripple.java / RippleBackground.java.
 * Copyright (C) 2014 The Android Open Source Project. Apache License 2.0.
 * Source and notices: docs/android-themes/lollipop/reference/framework/.
 */
package ing.fuyaoskyrocket.applocale.ui.designsystem.lollipop

import androidx.compose.animation.core.*
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.*
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.PointerInputModifierNode
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.node.invalidateDraw
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.*

/** Fixed source equations; no dependency on the host OS's RippleDrawable. */
internal object LollipopRippleTiming {
    fun enterMillis(radiusPx: Float, density: Float) = (1000 * sqrt(radiusPx / 1024f * density) + .5f).toInt()
    fun exitMillis(radiusPx: Float, density: Float) = (1000 * sqrt(radiusPx / 4424f * density) + .5f).toInt()
    val exitEasing = Easing { 1f - 400.0.pow(-it * 1.4).toFloat() }
    const val enterDelay = 80
    const val opacityMillis = 333
}

internal class LollipopRippleState(start: Offset) {
    var position by mutableStateOf(start)
    val gravity = Animatable(0f)
    val opacity = Animatable(1f)
    var job: Job? = null
    suspend fun enter(radius: Float, density: Float) {
        gravity.animateTo(1f, tween(LollipopRippleTiming.enterMillis(radius, density),
            LollipopRippleTiming.enterDelay, LinearEasing))
    }
    suspend fun exit(radius: Float, density: Float) = coroutineScope {
        launch { gravity.animateTo(1f, tween(LollipopRippleTiming.exitMillis(radius, density),
            easing = LollipopRippleTiming.exitEasing)) }
        opacity.animateTo(0f, tween(LollipopRippleTiming.opacityMillis, easing = LinearEasing))
    }
}

internal data class LollipopRipple(val color: Color, val bounded: Boolean = true) : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode = RippleNode(interactionSource, color, bounded)
}

private class RippleNode(val source: InteractionSource, val color: Color, val bounded: Boolean) : Modifier.Node(), DrawModifierNode, PointerInputModifierNode {
    val ripples = mutableStateListOf<LollipopRippleState>()
    val active = mutableMapOf<PressInteraction.Press, LollipopRippleState>()
    val focus = mutableSetOf<FocusInteraction.Focus>()
    val background = Animatable(0f)
    var backgroundJob: Job? = null
    var radius = 0f
    var density = 1f

    override fun onPointerEvent(pointerEvent: PointerEvent, pass: PointerEventPass, bounds: IntSize) {
        if (pass != PointerEventPass.Main) return
        val pointer = pointerEvent.changes.firstOrNull { it.pressed } ?: return
        // Ripple.move() follows View.drawableHotspotChanged while held; observing
        // coordinates never consumes a gesture or commits a control action.
        active.values.forEach { it.position = pointer.position }
    }

    override fun onCancelPointerInput() {
        active.keys.toList().forEach(::release)
        animateBackground()
    }

    override fun onAttach() {
        coroutineScope.launch {
            source.interactions.collect { event ->
                when (event) {
                    is PressInteraction.Press -> if (ripples.size < 10) {
                        val ripple = LollipopRippleState(event.pressPosition)
                        active[event] = ripple; ripples += ripple
                        ripple.job = launch { ripple.enter(radius, density) }
                    }
                    is PressInteraction.Release -> release(event.press)
                    is PressInteraction.Cancel -> release(event.press)
                    is FocusInteraction.Focus -> focus += event
                    is FocusInteraction.Unfocus -> focus -= event.focus
                }
                if (event is PressInteraction || event is FocusInteraction) animateBackground()
            }
        }
    }

    private fun release(press: PressInteraction.Press) {
        val ripple = active.remove(press) ?: return
        ripple.job?.cancel()
        ripple.job = coroutineScope.launch {
            try { ripple.exit(radius, density) } finally { ripples.remove(ripple) }
        }
    }

    private fun animateBackground() {
        backgroundJob?.cancel()
        backgroundJob = coroutineScope.launch {
            if (active.isNotEmpty() || focus.isNotEmpty()) {
                background.animateTo(1f, tween(100, easing = LinearEasing))
            } else {
                val influence = ((radius - 40f * density) / (200f * density)).coerceIn(0f, 1f)
                val velocity = 1.5f + 3f * influence
                val inflection = (1000f * (1f - background.value) / (3f + velocity) + .5f).toInt().coerceAtLeast(0)
                if (inflection > 0) {
                    val target = color.alpha * (background.value + inflection * velocity * influence / 1000f)
                    background.animateTo(target, tween(inflection, easing = LinearEasing))
                }
                background.animateTo(0f, tween((333 - inflection).coerceAtLeast(0), easing = LinearEasing))
            }
        }
    }

    override fun onDetach() {
        backgroundJob?.cancel(); ripples.forEach { it.job?.cancel() }
        active.clear(); ripples.clear(); focus.clear()
    }

    override fun ContentDrawScope.draw() {
        radius = hypot(size.width / 2f, size.height / 2f)
        this@RippleNode.density = density
        drawContent()
        fun androidx.compose.ui.graphics.drawscope.DrawScope.paintRipples() {
            drawCircle(color, radius, center, alpha = background.value)
            val layerPaint = Paint().apply { alpha = color.alpha / 2f }
            drawContext.canvas.saveLayer(Rect(-radius, -radius, size.width + radius, size.height + radius), layerPaint)
            ripples.forEach { ripple ->
                val start = if (ripple.position.x.isFinite() && ripple.position.y.isFinite()) ripple.position else center
                val delta = start - center
                val distance = delta.getDistance()
                val clamped = if (distance > radius && distance > 0f) center + delta * (radius / distance) else start
                val t = ripple.gravity.value
                drawCircle(color.copy(alpha = 1f), radius * t, clamped + (center - clamped) * t, alpha = ripple.opacity.value)
            }
            drawContext.canvas.restore()
        }
        if (bounded) clipRect { paintRipples() } else paintRipples()
    }
}
