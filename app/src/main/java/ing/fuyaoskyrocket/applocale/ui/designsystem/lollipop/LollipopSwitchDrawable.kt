/* AOSP Switch.java geometry, android-5.0.0_r1. Copyright 2014 AOSP, Apache-2.0. */
package ing.fuyaoskyrocket.applocale.ui.designsystem.lollipop

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import kotlin.math.max
import kotlin.math.roundToInt

/** Layout is measured from the original nine-patches, including optical insets. */
internal class LollipopSwitchDrawable(private val thumb: Drawable, private val track: Drawable) : Drawable(), Drawable.Callback {
    private val thumbPadding = Rect().also { thumb.getPadding(it) }
    private val trackPadding = Rect().also { track.getPadding(it) }
    private val optical = thumb.opticalInsets
    private val thumbWidth = thumb.intrinsicWidth - thumbPadding.left - thumbPadding.right
    override fun getIntrinsicWidth() = 2 * thumbWidth + max(trackPadding.left, optical.left) + max(trackPadding.right, optical.right)
    override fun getIntrinsicHeight() = max(thumb.intrinsicHeight, track.intrinsicHeight)
    val thumbTravelPx get() = (intrinsicWidth - thumbWidth - trackPadding.left - trackPadding.right - optical.left - optical.right).coerceAtLeast(1)
    init { thumb.callback = this; track.callback = this }
    override fun isStateful() = true
    override fun onStateChange(state: IntArray): Boolean { thumb.state = state; track.state = state; return true }
    override fun onLevelChange(level: Int) = true
    override fun jumpToCurrentState() { thumb.jumpToCurrentState(); track.jumpToCurrentState() }
    override fun draw(canvas: Canvas) {
        val leftInset = max(0, optical.left - trackPadding.left)
        val rightInset = max(0, optical.right - trackPadding.right)
        val left = bounds.left + leftInset
        val right = bounds.right - rightInset
        val top = bounds.top
        val bottom = bounds.bottom
        val position = if (layoutDirection == android.util.LayoutDirection.RTL) 1f - level / 10000f else level / 10000f
        val thumbLeft = left + (position * thumbTravelPx).roundToInt() + trackPadding.left
        track.setBounds(left + leftInset, top + max(0, optical.top - trackPadding.top),
            right - rightInset, bottom - max(0, optical.bottom - trackPadding.bottom))
        thumb.setBounds(thumbLeft - thumbPadding.left, top, thumbLeft + thumbWidth + thumbPadding.right, bottom)
        track.draw(canvas); thumb.draw(canvas)
    }
    override fun setAlpha(alpha: Int) { thumb.alpha = alpha; track.alpha = alpha }
    override fun setColorFilter(colorFilter: ColorFilter?) { thumb.colorFilter = colorFilter; track.colorFilter = colorFilter }
    @Deprecated("Drawable opacity is no longer used") override fun getOpacity() = PixelFormat.TRANSLUCENT
    override fun invalidateDrawable(who: Drawable) = invalidateSelf()
    override fun scheduleDrawable(who: Drawable, what: Runnable, at: Long) = scheduleSelf(what, at)
    override fun unscheduleDrawable(who: Drawable, what: Runnable) = unscheduleSelf(what)
}
