package ing.fuyaoskyrocket.applocale.ui.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemePreferences
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemeStyle
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.BackdropBlurDefaults
import ing.fuyaoskyrocket.applocale.ui.designsystem.LocalBlurProgress
import ing.fuyaoskyrocket.applocale.ui.designsystem.LocalPageContentBackdrop
import ing.fuyaoskyrocket.applocale.ui.designsystem.appBarBackdrop
import ing.fuyaoskyrocket.applocale.ui.designsystem.blurProtectionTint
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop

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
    content: @Composable (PaddingValues) -> Unit,
) {
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
            content(innerPadding)
        }
    }

    CompositionLocalProvider(
        LocalPageContentBackdrop provides if (captureWanted) pageBackdrop else null,
    ) {
        if (AppThemePreferences.style == AppThemeStyle.MIUIX) {
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
