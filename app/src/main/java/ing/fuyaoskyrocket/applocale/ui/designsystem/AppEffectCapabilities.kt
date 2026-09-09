package ing.fuyaoskyrocket.applocale.ui.designsystem

import androidx.compose.runtime.Composable
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.shader.isRenderEffectSupported

/**
 * The single answer to whether the background effects (more blur, Liquid
 * Glass dock) can render on this device. minSdk 33 is not sufficient proof:
 * RenderEffect and the runtime shader must both check out. The chrome host
 * renderers and the about-page switches must share this result; it is never
 * hard-coded to pass acceptance, and an unsupported device falls back to the
 * native solid rendering while the saved preference keeps its real value.
 */
fun isEffectRenderingSupported(): Boolean =
    isRenderEffectSupported() && isRuntimeShaderSupported()

/**
 * The ONE answer to "is this backdrop effect actually rendering": the active
 * style must allow it (Holo never does — round-9 045), the user must have it
 * on, and the device must support it. Every consumer asks here instead of
 * re-deriving the conjunction.
 */
@Composable
fun isBackdropEffectActive(requested: Boolean): Boolean =
    LocalAppUiTheme.current.policy.supportsBackdropEffects &&
        requested &&
        isEffectRenderingSupported()
