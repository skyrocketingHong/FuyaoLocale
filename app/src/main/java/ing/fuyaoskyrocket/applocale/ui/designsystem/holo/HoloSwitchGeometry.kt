package ing.fuyaoskyrocket.applocale.ui.designsystem.holo

import kotlin.math.max

internal data class HoloSwitchGeometry(val width: Float, val thumbWidth: Float) {
    val travel: Float get() = (width - thumbWidth).coerceAtLeast(0f)
    fun thumbLeft(fraction: Float, rtl: Boolean): Float = travel *
        (if (rtl) 1f - fraction.coerceIn(0f, 1f) else fraction.coerceIn(0f, 1f))
}

/** Include text and nine-patch padding in measurement; draw to these same bounds. */
internal fun holoSwitchGeometry(minWidth: Float, labelWidth: Float, textPadding: Float,
    nativeThumbWidth: Float, thumbPadding: Float, trackPadding: Float): HoloSwitchGeometry {
    val body = max(labelWidth + 2 * textPadding, nativeThumbWidth - thumbPadding)
    val thumb = body + thumbPadding
    return HoloSwitchGeometry(max(minWidth, 2 * body + max(trackPadding, thumbPadding)), thumb)
}
