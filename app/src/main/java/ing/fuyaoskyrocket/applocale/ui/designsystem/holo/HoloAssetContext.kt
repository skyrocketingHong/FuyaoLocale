package ing.fuyaoskyrocket.applocale.ui.designsystem.holo

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import android.graphics.drawable.Drawable

internal val LocalHoloDrawableTransform = staticCompositionLocalOf<(Int, Drawable, Boolean) -> Unit> {
    { _, _, _ -> }
}

/** Compatibility keys used by the shared Holo widgets; each version resolves its own assets. */
internal val LocalHoloDrawableResolver = staticCompositionLocalOf<(Int, Boolean) -> Int> {
    error("A Holo asset set must be installed by its versioned theme")
}
internal val LocalHoloWindowBackground = staticCompositionLocalOf<Int?> { null }
internal val LocalHoloAssetResolver = staticCompositionLocalOf<(HoloAsset, Boolean) -> Int> {
    error("A versioned Holo asset provider must be installed")
}

@Composable
@DrawableRes
internal fun themedHoloDrawableId(asset: HoloAsset): Int =
    LocalHoloAssetResolver.current(asset, LocalHoloDarkTheme.current)

@Composable
@DrawableRes
internal fun themedHoloDrawableId(@DrawableRes resource: Int): Int =
    LocalHoloDrawableResolver.current(resource, LocalHoloDarkTheme.current)
