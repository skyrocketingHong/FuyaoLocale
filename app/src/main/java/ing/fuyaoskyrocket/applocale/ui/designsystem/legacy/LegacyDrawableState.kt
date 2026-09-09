package ing.fuyaoskyrocket.applocale.ui.designsystem.legacy

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * The control states the imported AOSP selectors switch on. Mirrors
 * android.view.View drawable states; the mapping into the int-array state set
 * keeps the ORIGINAL selector item order authoritative — matching is done by
 * the framework StateListDrawable, never by re-deriving colours here.
 */
data class LegacyControlState(
    val enabled: Boolean = true,
    val pressed: Boolean = false,
    val focused: Boolean = false,
    val checked: Boolean = false,
    val selected: Boolean = false,
    val activated: Boolean = false,
    /** The app window is focused (compose has no view-level window focus per
     * drawable; the drawable bridge reports true, matching the focused app). */
    val windowFocused: Boolean = true,
)

/** View state set ordering pressed → focused → selected → checked → activated. */
fun LegacyControlState.toStateSet(): IntArray {
    val states = mutableListOf<Int>()
    if (enabled) states += android.R.attr.state_enabled
    if (pressed) states += android.R.attr.state_pressed
    if (focused) states += android.R.attr.state_focused
    if (selected) states += android.R.attr.state_selected
    if (checked) states += android.R.attr.state_checked
    if (activated) states += android.R.attr.state_activated
    if (windowFocused) states += android.R.attr.state_window_focused
    return states.toIntArray()
}

/** Resolves a colour list against this state (fallback = default colour). */
fun LegacyControlState.resolve(list: ColorStateList): Int =
    list.getColorForState(toStateSet(), list.defaultColor)

/** No style inference: each era supplies its already resolved resource ID. */
fun legacyDrawable(context: Context, @DrawableRes resId: Int): Drawable =
    requireNotNull(ContextCompat.getDrawable(context, resId)).mutate()

@Composable
fun rememberLegacyDrawable(@DrawableRes resId: Int): Drawable {
    val context = LocalContext.current
    return remember(resId, context) {
        legacyDrawable(context, resId).apply { state = LegacyControlState().toStateSet() }
    }
}
