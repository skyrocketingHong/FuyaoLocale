package ing.fuyaoskyrocket.applocale.ui.designsystem.legacy

import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt

/**
 * Draws an Android [Drawable] (StateList/NinePatch/Layer/Rotate/Scale as
 * inflated by the resource pipeline) as a Compose draw layer.
 *
 * The shared drawable lifecycle contract:
 * - one drawable instance per site ([rememberLegacyDrawable]) so selector state
 *   is never shared between rows;
 * - state/level are applied every draw; optional host alpha composites outside
 *   the drawable so original selector/nine-patch alpha is never overwritten;
 *   Drawable-initiated
 *   invalidations route back through the node's invalidateDraw;
 * - schedule/unschedule post to the main handler; detach removes pending
 *   posts and clears the callback so a disposed node can never invalidate;
 * - bounds follow [LegacyDrawableBounds]: 9-patch/layer backgrounds fill the
 *   size, intrinsic-sized glyphs (checkbox, radio, icons) keep intrinsic
 *   density-scaled dimensions aligned per [alignment];
 * - the draw scope's layout direction is forwarded so asymmetric assets
 *   mirror correctly in RTL.
 */
internal class LegacyDrawableNode(
    var drawable: Drawable,
    var state: LegacyControlState,
    var levelProvider: () -> Int,
    var alpha: Float?,
    var boundsMode: LegacyDrawableBounds,
    var alignment: Alignment,
) : Modifier.Node(), DrawModifierNode {

    override val shouldAutoInvalidate: Boolean get() = false

    private val handler = Handler(Looper.getMainLooper())

    val callback = object : Drawable.Callback {
        override fun invalidateDrawable(who: Drawable) {
            if (isAttached) invalidateDraw()
        }

        override fun scheduleDrawable(who: Drawable, what: Runnable, time: Long) {
            if (isAttached) handler.postAtTime(what, who, time)
        }

        override fun unscheduleDrawable(who: Drawable, what: Runnable) {
            handler.removeCallbacks(what, who)
        }
    }

    override fun onAttach() {
        super.onAttach()
        drawable.callback = callback
    }

    override fun onDetach() {
        releaseDrawable()
        super.onDetach()
    }

    fun releaseDrawable() {
        handler.removeCallbacksAndMessages(drawable)
        drawable.callback = null
    }

    override fun ContentDrawScope.draw() {
        val intSize = IntSize(size.width.roundToInt(), size.height.roundToInt())
        // Capture before the Drawable receiver: Drawable exposes its own Int
        // layoutDirection and IntArray state properties, which must not shadow
        // the scope's enum / the node's control state.
        val composeDirection = layoutDirection
        val controlState = state
        val drawAlpha = ((this@LegacyDrawableNode.alpha ?: 1f) * 255f).roundToInt().coerceIn(0, 255)
        drawable.apply {
            setLayoutDirection(
                if (composeDirection == androidx.compose.ui.unit.LayoutDirection.Rtl) {
                    android.util.LayoutDirection.RTL
                } else {
                    android.util.LayoutDirection.LTR
                },
            )
            setState(controlState.toStateSet())
            setLevel(levelProvider())
            val target = when (boundsMode) {
                LegacyDrawableBounds.Fill -> intSize
                LegacyDrawableBounds.Horizontal ->
                    IntSize(intSize.width, intrinsicHeight.coerceAtLeast(1))
                LegacyDrawableBounds.Intrinsic -> {
                    val w = intrinsicWidth.takeIf { it > 0 } ?: intSize.width
                    val h = intrinsicHeight.takeIf { it > 0 } ?: intSize.height
                    IntSize(w, h)
                }
                LegacyDrawableBounds.Fit -> fitLegacyDrawableBounds(
                    IntSize(intrinsicWidth.coerceAtLeast(1), intrinsicHeight.coerceAtLeast(1)), intSize,
                )
            }
            val aligned = alignment.align(
                size = target,
                space = intSize,
                layoutDirection = composeDirection,
            )
            setBounds(aligned.x, aligned.y, aligned.x + target.width, aligned.y + target.height)
            val canvas = drawContext.canvas.nativeCanvas
            if (drawAlpha < 255) {
                // setAlpha(255) is not neutral for DrawableContainer: it marks
                // the container as owning child alpha and replaces XML values
                // such as Material's disabled=.3, track=.3 and divider=.12.
                val save = canvas.saveLayerAlpha(bounds.left.toFloat(), bounds.top.toFloat(),
                    bounds.right.toFloat(), bounds.bottom.toFloat(), drawAlpha)
                draw(canvas)
                canvas.restoreToCount(save)
            } else {
                draw(canvas)
            }
        }
        drawContent()
    }
}

/** How the drawable's bounds relate to the modifier's size. */
enum class LegacyDrawableBounds { Fill, Intrinsic, Horizontal, Fit }

internal fun fitLegacyDrawableBounds(intrinsic: IntSize, available: IntSize): IntSize {
    val scale = minOf(1f, available.width.toFloat() / intrinsic.width.coerceAtLeast(1),
        available.height.toFloat() / intrinsic.height.coerceAtLeast(1))
    return IntSize((intrinsic.width * scale).roundToInt(), (intrinsic.height * scale).roundToInt())
}

internal class LegacyDrawableElement(
    val drawable: Drawable,
    val state: LegacyControlState,
    val levelProvider: () -> Int,
    val alpha: Float?,
    val boundsMode: LegacyDrawableBounds,
    val alignment: Alignment,
) : ModifierNodeElement<LegacyDrawableNode>() {
    override fun create(): LegacyDrawableNode =
        LegacyDrawableNode(drawable, state, levelProvider, alpha, boundsMode, alignment)

    override fun update(node: LegacyDrawableNode) {
        val drawableChanged = node.drawable !== drawable
        if (drawableChanged) {
            node.releaseDrawable()
        }
        node.drawable = drawable
        node.state = state
        node.levelProvider = levelProvider
        node.alpha = alpha
        node.boundsMode = boundsMode
        node.alignment = alignment
        if (node.isAttached && drawableChanged) {
            drawable.callback = node.callback
        }
        if (node.isAttached) node.invalidateDraw()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LegacyDrawableElement) return false
        return drawable === other.drawable && state == other.state &&
            levelProvider === other.levelProvider && alpha == other.alpha &&
            boundsMode == other.boundsMode && alignment == other.alignment
    }

    override fun hashCode(): Int {
        var result = System.identityHashCode(drawable)
        result = 31 * result + state.hashCode()
        result = 31 * result + System.identityHashCode(levelProvider)
        result = 31 * result + (alpha?.hashCode() ?: 0)
        result = 31 * result + boundsMode.hashCode()
        result = 31 * result + alignment.hashCode()
        return result
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "legacyDrawable"
        properties["drawable"] = drawable
        properties["state"] = state
    }
}

/**
 * Renders [drawable] behind the content of the modified node. Use a
 * per-site instance from [rememberLegacyDrawable].
 */
fun Modifier.legacyDrawable(
    drawable: Drawable,
    state: LegacyControlState = LegacyControlState(),
    levelProvider: () -> Int = { 0 },
    alpha: Float? = null,
    boundsMode: LegacyDrawableBounds = LegacyDrawableBounds.Fill,
    alignment: Alignment = Alignment.Center,
): Modifier = this then LegacyDrawableElement(
    drawable = drawable,
    state = state,
    levelProvider = levelProvider,
    alpha = alpha,
    boundsMode = boundsMode,
    alignment = alignment,
)

/**
 * Convenience for a drawable that fills the whole node (9-patch backgrounds,
 * layer-list progress bars, list selectors).
 */
fun Modifier.legacyBackground(
    drawable: Drawable,
    state: LegacyControlState = LegacyControlState(),
    levelProvider: () -> Int = { 0 },
): Modifier = legacyDrawable(drawable, state, levelProvider, boundsMode = LegacyDrawableBounds.Fill)
