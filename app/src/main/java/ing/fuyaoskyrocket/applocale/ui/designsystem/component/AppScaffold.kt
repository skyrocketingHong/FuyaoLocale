package ing.fuyaoskyrocket.applocale.ui.designsystem.component

import ing.fuyaoskyrocket.applocale.ui.designsystem.lollipop.*
import ing.fuyaoskyrocket.applocale.ui.designsystem.eclair.*
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppControlFamily
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ing.fuyaoskyrocket.applocale.ui.screen.LocalPagerPageScaffold
import ing.fuyaoskyrocket.applocale.ui.screen.PagerPageScaffold
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.BackdropBlurDefaults
import ing.fuyaoskyrocket.applocale.ui.designsystem.LocalBlurProgress
import ing.fuyaoskyrocket.applocale.ui.designsystem.LocalPageContentBackdrop
import ing.fuyaoskyrocket.applocale.ui.designsystem.appBarBackdrop
import ing.fuyaoskyrocket.applocale.ui.designsystem.blurProtectionTint
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop

internal val LocalAppContentPadding = staticCompositionLocalOf<PaddingValues> { PaddingValues() }

/**
 * Theme-aware scaffold. Both styles always run their OWN native scaffold — the
 * structure never swaps when an effect toggles, so content identity and
 * constraints stay stable; more blur only changes what the chrome samples.
 *
 * Background sampling, when the effect is (un)fading: the content slot is
 * recorded into a page backdrop; the top bar draws above that recording and
 * samples it through a background-only layer. The native bottom bar, FAB and
 * snackbar slots stay outside the recording, and the top bar's own opaque
 * background yields by live progress (never by the target boolean, so an exit
 * fade cannot be suddenly covered). Pages consume the innerPadding exactly
 * once — scrollables via contentPadding, fixed states via padding — and never
 * on top of an estimated top-bar height.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    contentWindowInsets: WindowInsets = WindowInsets(0, 0, 0, 0),
    containerColor: Color = AppUiTheme.palette.surface,
    topLevelNavigation: Boolean = false,
    contextual: Boolean = false,
    content: @Composable (PaddingValues) -> Unit,
) {
    val pagerScope = LocalPagerPageScaffold.current
    if (pagerScope != null) {
        PagerPageScaffold(pagerScope, modifier, topBar, bottomBar, snackbarHost, floatingActionButton, contextual, content)
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Lollipop) {
        LollipopScaffold(modifier, topLevelNavigation, contextual, topBar, snackbarHost, containerColor, content, floatingActionButton)
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Eclair) {
        EclairScaffold(modifier, topLevelNavigation, contextual, topBar, bottomBar, snackbarHost, containerColor, content)
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Holo) {
        // The Holo scaffold is a fixed chrome column: no bottom dock slot (the
        // CAB replaces the batch bar), no FAB era equivalent, no sampling.
        ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloScaffold(
            modifier = modifier,
            topLevelNavigation = topLevelNavigation,
            contextual = contextual,
            topBar = topBar,
            snackbarHost = snackbarHost,
            containerColor = containerColor,
            content = content,
        )
        return
    }
    // Recording is wanted while the effect runs — including its exit fade —
    // mirroring the chrome host's capture condition.
    val blurProgress = LocalBlurProgress.current
    val captureWanted = blurProgress > 0.01f
    val pageBackdrop = rememberLayerBackdrop {
        drawRect(containerColor)
        drawContent()
    }
    val tint = blurProtectionTint(AppUiTheme.palette)

    val wrappedTopBar: @Composable () -> Unit = {
        Box {
            // Background-only sampling layer: matchParentSize never joins the
            // wrapper's height measurement, so the real bar decides it.
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .appBarBackdrop(
                        backdrop = pageBackdrop,
                        radius = BackdropBlurDefaults.topBarRadius,
                        fadeHeight = BackdropBlurDefaults.topBarFadeHeight,
                        tint = tint,
                        progress = blurProgress,
                    ),
            )
            topBar()
        }
    }
    val recordedContent: @Composable (PaddingValues) -> Unit = { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (captureWanted) {
                        Modifier.layerBackdrop(pageBackdrop)
                    } else {
                        Modifier
                    },
                ),
        ) {
            CompositionLocalProvider(LocalAppContentPadding provides innerPadding) { content(innerPadding) }
        }
    }

    CompositionLocalProvider(
        LocalPageContentBackdrop provides if (captureWanted) pageBackdrop else null,
    ) {
        if (AppUiTheme.policy.controls == AppControlFamily.Material2) {
            androidx.compose.material.Scaffold(
                modifier = modifier, topBar = wrappedTopBar, bottomBar = bottomBar,
                floatingActionButton = floatingActionButton, snackbarHost = { snackbarHost() },
                contentWindowInsets = contentWindowInsets, backgroundColor = containerColor,
                content = recordedContent,
            )
        } else if (AppUiTheme.policy.controls == AppControlFamily.Miuix) {
            top.yukonga.miuix.kmp.basic.Scaffold(
                modifier = modifier,
                topBar = wrappedTopBar,
                bottomBar = bottomBar,
                floatingActionButton = floatingActionButton,
                snackbarHost = snackbarHost,
                contentWindowInsets = contentWindowInsets,
                containerColor = containerColor,
                content = recordedContent,
            )
        } else {
            androidx.compose.material3.Scaffold(
                modifier = modifier,
                topBar = wrappedTopBar,
                bottomBar = bottomBar,
                floatingActionButton = floatingActionButton,
                snackbarHost = snackbarHost,
                contentWindowInsets = contentWindowInsets,
                containerColor = containerColor,
                content = recordedContent,
            )
        }
    }
}
