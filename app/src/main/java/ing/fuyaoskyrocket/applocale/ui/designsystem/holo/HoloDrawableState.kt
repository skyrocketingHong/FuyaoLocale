package ing.fuyaoskyrocket.applocale.ui.designsystem.holo

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.toStateSet as legacyStateSet
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.resolve as legacyResolve
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.legacyDrawable
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.rememberLegacyDrawable

typealias HoloControlState = ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.LegacyControlState

fun HoloControlState.toStateSet(): IntArray = legacyStateSet()

fun HoloControlState.resolve(list: ColorStateList): Int = legacyResolve(list)

/** Loads the fixed-version ColorStateList by resource id. */
fun holoColorList(context: Context, resId: Int): ColorStateList =
    requireNotNull(ContextCompat.getColorStateList(context, resId)) {
        "holo color list $resId missing"
    }

/**
 * Creates an isolated, mutable drawable instance for one composition site.
 * Every caller gets its own constant state (mutate) so pressing one row can
 * never flip another row's selector — the per-instance rule of 040.
 */
fun holoDrawable(context: Context, @DrawableRes resId: Int): Drawable =
    legacyDrawable(context, resId)

/** Composition-scoped [holoDrawable] factory. */
@Composable
fun rememberHoloDrawable(@DrawableRes resId: Int): Drawable {
    val themedId = themedHoloDrawableId(resId)
    return rememberPreparedHoloDrawable(themedId)
}

@Composable
internal fun rememberHoloDrawable(asset: HoloAsset): Drawable = rememberPreparedHoloDrawable(themedHoloDrawableId(asset))

@Composable
private fun rememberPreparedHoloDrawable(@DrawableRes resource: Int): Drawable {
    val drawable = rememberLegacyDrawable(resource)
    val transform = LocalHoloDrawableTransform.current
    val dark = LocalHoloDarkTheme.current
    return remember(drawable, transform, dark) {
        transform(resource, drawable, dark)
        drawable
    }
}
