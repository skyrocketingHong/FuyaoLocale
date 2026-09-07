package ing.fuyaoskyrocket.applocale.ui.designsystem

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.ui.unit.dp

/**
 * Single source of motion and threshold constants for the UI task packages.
 *
 * These are design initial values pending on-device verification, not values
 * dictated by upstream or Android. Call sites compose them with their own
 * frame timing; this object holds no coroutines, Activity or settings access.
 */
object AppMotion {
    val StateEasing = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)
    const val StateColorMillis = 160
    const val BarVisibilityMillis = 200

    /**
     * The bottom dock's exit (round-7 030): the dock leaves downward over the
     * full bar height, accelerating to the screen edge while fading. Design
     * initial value pending on-device verification — the entrance keeps
     * [BarVisibilityMillis]/[StateEasing] untouched.
     */
    const val DockExitMillis = 220
    val DockExitEasing = CubicBezierEasing(0.4f, 0f, 1f, 1f)
    const val ReturnDampingRatio = 1f
    const val ReturnStiffness = 300f
    const val ProgressThreshold = 0.001f
    const val PixelThreshold = 0.5f
    val ReorderEdgeZone = 48.dp

    /** Per-second limit; call sites multiply by the frame interval in seconds. */
    val ReorderMaxSpeed = 480.dp
}
