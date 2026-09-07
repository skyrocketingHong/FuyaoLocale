package ing.fuyaoskyrocket.applocale.ui.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.drawBackdrop

/** True while the more-blur preference is on AND the device can run the shaders. */
val LocalMoreBlurActive = staticCompositionLocalOf { false }

/**
 * The host's interruptible effect progress (0..1): retargets on toggle over
 * 240ms FastOutSlowIn. Radii and tints scale by it; the recording layers stay
 * alive until it settles back to zero.
 */
val LocalBlurProgress = staticCompositionLocalOf { 1f }

/**
 * Per-page recording of the scrollable content ONLY (the scaffold records its
 * content slot; top bar, dock, FAB and snackbar stay outside). The top chrome
 * samples this — never the scene that already contains itself.
 */
val LocalPageContentBackdrop = staticCompositionLocalOf<LayerBackdrop?> { null }

/** Blur radii and fade extents in dp — device-tunable first values, not specs. */
object BackdropBlurDefaults {
    val topBarRadius = 20.dp
    val topBarFadeHeight = 8.dp
    val bottomBarRadius = 20.dp

    /** Protection tint over the blurred samples: lighter in dark mode. */
    const val LightTintAlpha = 0.76f
    const val DarkTintAlpha = 0.64f
}

/**
 * A background-only blurred sample layer for chrome bars: draws the recorded
 * [backdrop] behind the node with a constant-radius blur (radius × [progress]),
 * softens the protection tint along its bottom [fadeHeight]. The blurred
 * sample remains opaque: erasing it would reveal the original sharp list
 * beneath the title. Only the background uses the system blur RenderEffect;
 * foreground text and icons are drawn afterwards. With [progress] at zero the node
 * renders nothing extra and the caller's normal background shows through.
 */
@Composable
fun Modifier.appBarBackdrop(
    backdrop: LayerBackdrop?,
    radius: Dp,
    fadeHeight: Dp,
    tint: Color,
    progress: Float,
): Modifier {
    if (backdrop == null || progress <= 0.01f) return this
    val density = LocalDensity.current
    val radiusPx = with(density) { radius.toPx() }
    val fadePx = with(density) { fadeHeight.toPx() }
    return this
        .drawBackdrop(
            backdrop = backdrop,
            shape = { RectangleShape },
            // Native full-resolution blur avoids the multi-level downsample /
            // RuntimeShader cascade on this shallow, full-width surface.
            effects = {
                val amount = (radiusPx * progress).coerceAtLeast(0.1f)
                padding = amount * 2f
                renderEffect = BlurEffect(null, amount, amount, TileMode.Clamp)
            },
            onDrawBehind = { drawRect(tint.copy(alpha = 1f)) },
            onDrawSurface = {
                if (fadePx <= 0f) {
                    // Degenerate gradient range (bottom bar): flat protection
                    // tint instead of relying on startY == endY behaviour.
                    drawRect(color = tint, alpha = progress)
                } else {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(tint, tint.copy(alpha = tint.alpha * 0.9f)),
                            startY = (size.height - fadePx).coerceAtLeast(0f),
                            endY = size.height,
                        ),
                        alpha = progress,
                    )
                }
            },
        )
}

/** The protection tint colour for chrome backgrounds under the active palette. */
@Composable
fun blurProtectionTint(palette: AppPalette): Color {
    val alpha = if (palette.background.luminance() < 0.5f) {
        BackdropBlurDefaults.DarkTintAlpha
    } else {
        BackdropBlurDefaults.LightTintAlpha
    }
    return palette.background.copy(alpha = alpha)
}
