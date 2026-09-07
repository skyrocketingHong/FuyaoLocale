package ing.fuyaoskyrocket.applocale.ui.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop

/**
 * The bottom dock's footprint as measured/derived by the host. [occupiedHeight]
 * includes the current system bottom inset exactly once (the dock bars reserve
 * it internally); [listEndPadding] is the TOTAL bottom contentPadding a
 * scrollable list needs — dock reserve plus its trailing breathing room — so a
 * page must not add anything on top of it. Floating feedback (FAB, Snackbar)
 * uses [occupiedHeight] minus whatever the host has already consumed for it,
 * floored at zero.
 */
data class BottomDockMetrics(
    val occupiedHeight: Dp,
    val listEndPadding: Dp,
    val isPresent: Boolean,
)

/**
 * True while the compact [AppChromeHost] owns the bottom edge: top-level pages
 * must skip their own in-slot navigation bar. Wide layouts and detail routes
 * read false (rail stays native; detail has no dock).
 */
val LocalBottomDockOwner = staticCompositionLocalOf<Boolean> { false }

val LocalBottomDockMetrics = staticCompositionLocalOf {
    BottomDockMetrics(occupiedHeight = 0.dp, listEndPadding = 0.dp, isPresent = false)
}

/**
 * The single scene recording layer of the compact window: page content (and its
 * top chrome) are recorded here for every background effect. Effects may only
 * sample from it; nothing records an effect's own output back into it.
 */
val LocalSceneBackdrop = staticCompositionLocalOf<LayerBackdrop?> { null }

/**
 * Shared presence state of the host's bottom dock (glass or standard form).
 * The host writes [isRouteAllowed] from its top-level-destination check; a
 * screen whose own bottom surface takes over the edge (for example selection
 * mode) sets [isSuppressed]. [isVisible] is the derived target;
 * [isPresent] stays true while the exit transition still runs, and is what
 * bottom reservations wait for.
 */
class GlassNavigationBarVisibility(initialPresent: Boolean = false) {
    var isRouteAllowed by mutableStateOf(false)
    var isSuppressed by mutableStateOf(false)
    val isVisible: Boolean get() = isRouteAllowed && !isSuppressed

    /**
     * The single transition source for the dock's presence. Only the host
     * retargets it, from [isVisible]; consumers read [isPresent] instead of
     * writing animation state of their own.
     */
    internal val presence = MutableTransitionState(initialPresent)

    /** True while the dock still occupies the bottom edge, including its exits. */
    val isPresent: Boolean get() = presence.currentState || presence.targetState

    /** Whether the dock may accept interaction right now. */
    val canInteract: Boolean get() = isVisible
}

val LocalGlassNavigationBarVisibility =
    staticCompositionLocalOf<GlassNavigationBarVisibility?> { null }

private fun <T> barVisibilitySpec(): TweenSpec<T> =
    tween(durationMillis = AppMotion.BarVisibilityMillis, easing = AppMotion.StateEasing)

/**
 * The compact window's fixed chrome structure. The Navigation tree exists
 * exactly once inside [content]; the host never swaps the composition by
 * effect or glass booleans — it only toggles recording/effect nodes. The page
 * content is recorded into the scene backdrop when any effect needs samples,
 * and the bottom dock (Liquid Glass or the standard bar) is drawn as an
 * overlay ABOVE the recorded scene, never inside it.
 */
@Composable
fun AppChromeHost(
    isTopLevelDestination: Boolean,
    navigationBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val appearance = AppAppearanceState.fromPreferences()
    val effectsSupported = isEffectRenderingSupported()
    val glassDock = appearance.liquidGlassNavigationBar && effectsSupported

    // The shared presence state must exist before anything reads it: the glass
    // adapter below stays active through the dock's exit, so the bar keeps its
    // glass form (and its samples) until the leaving transition has settled.
    val visibility = remember {
        GlassNavigationBarVisibility(isTopLevelDestination)
            .apply { isRouteAllowed = isTopLevelDestination }
    }
    SideEffect { visibility.isRouteAllowed = isTopLevelDestination }
    // Reading the derived target in composition keeps this scope subscribed to
    // suppression changes made deeper in the tree; the retarget stays in the
    // effect.
    val targetPresent = visibility.isVisible
    LaunchedEffect(targetPresent) {
        visibility.presence.targetState = targetPresent
    }

    // The glass adapter local is non-null only while the glass dock actually
    // renders — top-level presence OR an exit still in flight; the standard
    // dock path leaves it null so glass-drawing widgets fall back to their
    // native rendering. Judging the route alone would swap the bar's form
    // mid-exit.
    val glassActive = glassDock && (isTopLevelDestination || visibility.isPresent)
    // More blur is independent of the glass dock and works in both themes.
    val moreBlurActive = appearance.moreBlur && effectsSupported
    // Interruptible 0..1 progress: retargets from the current value, so a fast
    // off→on→off sequence converges instead of restarting.
    val blurProgress by animateFloatAsState(
        targetValue = if (moreBlurActive) 1f else 0f,
        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
        label = "blurProgress",
    )
    // Recording is wanted by any effect — including an in-flight glass exit or
    // exit fade — and stops only once every effect has settled back to zero.
    val captureWanted = glassActive || moreBlurActive || blurProgress > 0.01f
    // One stable instance for the host's whole lifetime; only the recording
    // modifier and the published locals toggle with captureWanted, so a dock
    // exit never rebuilds the backdrop mid-flight.
    val sceneBackdrop = rememberLayerBackdrop()

    // The dock's real footprint comes from measurement, so both bar forms and
    // future metrics changes stay truthful; the glass form keeps its symmetric
    // trailing breathing room on top of the measured height.
    val density = LocalDensity.current
    var dockHeightDp by remember { mutableStateOf(0.dp) }
    val navBarBottom = WindowInsets.navigationBars
        .only(WindowInsetsSides.Bottom)
        .asPaddingValues()
        .calculateBottomPadding()
    val glassBarBottomMargin = if (navBarBottom != 0.dp) 8.dp + navBarBottom else 36.dp
    val dockTrailingRoom = if (glassActive) glassBarBottomMargin else AppSpacing.lg
    val dockMetrics = if (visibility.isPresent && dockHeightDp > 0.dp) {
        BottomDockMetrics(
            occupiedHeight = dockHeightDp,
            listEndPadding = dockHeightDp + dockTrailingRoom,
            isPresent = true,
        )
    } else {
        BottomDockMetrics(occupiedHeight = 0.dp, listEndPadding = 0.dp, isPresent = false)
    }

    CompositionLocalProvider(
        LocalSceneBackdrop provides if (captureWanted) sceneBackdrop else null,
        LocalGlassBackdrop provides if (glassActive) sceneBackdrop else null,
        LocalGlassNavigationBarVisibility provides visibility,
        LocalBottomDockOwner provides true,
        LocalBottomDockMetrics provides dockMetrics,
        LocalMoreBlurActive provides moreBlurActive,
        LocalBlurProgress provides blurProgress,
    ) {
        Box(modifier = modifier.fillMaxSize()) {
            // SceneLayer: page content + top chrome, recorded for effects.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (captureWanted) {
                            Modifier.layerBackdrop(sceneBackdrop)
                        } else {
                            Modifier
                        },
                    ),
            ) {
                content()
            }
            // BottomDock: above the recorded scene, never part of its samples.
            // Only the dock itself is measured — never this full-screen
            // positioning layer — so the published footprint is the real bar
            // height (already including its bottom inset/margin), not the
            // window height. The full-screen Box stays for overlay stacking.
            Box(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visibleState = visibility.presence,
                    modifier = Modifier.align(Alignment.BottomCenter),
                    enter = slideInVertically(
                        animationSpec = barVisibilitySpec(),
                    ) { it } + fadeIn(
                        animationSpec = barVisibilitySpec(),
                    ),
                    // Round-7 030: the dock LEAVES downward — the full bar height
                    // toward the screen's bottom edge, accelerating and fading —
                    // instead of the library default half-height upward slide.
                    // The glass form and its samples are kept by glassActive
                    // above until this exit settles.
                    exit = slideOutVertically(
                        animationSpec = tween(
                            durationMillis = AppMotion.DockExitMillis,
                            easing = AppMotion.DockExitEasing,
                        ),
                        targetOffsetY = { fullHeight -> fullHeight },
                    ) + fadeOut(
                        animationSpec = tween(
                            durationMillis = AppMotion.DockExitMillis,
                            easing = AppMotion.DockExitEasing,
                        ),
                    ),
                ) {
                    Box(
                        modifier = Modifier.onSizeChanged { size ->
                            dockHeightDp = with(density) { size.height.toDp() }
                        },
                    ) {
                        navigationBar()
                    }
                }
            }
        }
    }
}

/** True while the host's bottom dock still occupies the bottom edge. */
@Composable
fun isGlassNavigationBarVisible(): Boolean =
    LocalBottomDockMetrics.current.isPresent

/**
 * Whether the home screen's batch bar may show its interactive content. Without
 * a host the bottom slot is plain; with one, the content waits until the dock
 * has finished vacating the bottom edge — the slot itself stays reserved
 * either way. The caller combines this with its own selection state.
 */
@Composable
fun shouldShowBatchBar(): Boolean =
    LocalGlassNavigationBarVisibility.current?.isPresent != true

/**
 * The dock's occupied footprint for floating elements (FABs, snackbars); add
 * only what the host has not already reserved underneath the element.
 */
@Composable
fun glassNavigationBarClearance(): Dp = LocalBottomDockMetrics.current.occupiedHeight
