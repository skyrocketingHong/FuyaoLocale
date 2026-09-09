package ing.fuyaoskyrocket.applocale.ui.designsystem.holo

import android.graphics.drawable.Drawable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.legacyDrawable

typealias HoloDrawableBounds = ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.LegacyDrawableBounds
internal typealias HoloDrawableNode = ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.LegacyDrawableNode

/** ICS keeps its public bridge while sharing only drawable lifecycle mechanics. */
fun Modifier.holoDrawable(
    drawable: Drawable,
    state: HoloControlState = HoloControlState(),
    levelProvider: () -> Int = { 0 },
    alpha: Float? = null,
    boundsMode: HoloDrawableBounds = HoloDrawableBounds.Fill,
    alignment: Alignment = Alignment.Center,
): Modifier = legacyDrawable(drawable, state, levelProvider, alpha, boundsMode, alignment)

fun Modifier.holoBackground(
    drawable: Drawable,
    state: HoloControlState = HoloControlState(),
    levelProvider: () -> Int = { 0 },
): Modifier = holoDrawable(drawable, state, levelProvider, boundsMode = HoloDrawableBounds.Fill)
