package ing.fuyaoskyrocket.applocale.ui.designsystem

import android.app.LocaleManager
import android.os.LocaleList
import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/** The single entry point every appearance change must go through. */
interface AppearanceRequester {
    /** [restoreFocus] identifies the calling control so the transition gate can
     * hand keyboard focus back once the content is interactive again. */
    fun requestTheme(style: AppThemeStyle, restoreFocus: FocusRequester? = null)
    fun requestColorMode(mode: AppColorMode, restoreFocus: FocusRequester? = null)
    fun requestMoreBlur(enabled: Boolean)
    fun requestGlass(enabled: Boolean)
    fun requestAppLocale(tag: String?)

    /** The language sheet finished closing; a pending locale fade may begin. */
    fun confirmAppLocaleSheetClosed()
}

val LocalAppearanceRequester = staticCompositionLocalOf<AppearanceRequester?> { null }

/** One-shot appearance failure pings for whichever page hosts feedback UI. */
val LocalAppearanceFailures = staticCompositionLocalOf<SharedFlow<Unit>?> { null }

/** Phase timings; FastOutSlowIn everywhere for this first implementation. */
private object AppearanceMotion {
    const val FadeOutMillis = 90
    const val FadeInMillis = 150
    const val BackgroundMillis = 240
    const val FrameTimeoutMillis = 500L
    const val LocaleTimeoutMillis = 2500L
}

@Composable
private fun snapAnimations(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f,
            ) == 0f
        }.getOrDefault(false)
    }
}

/** The window background the protective layer shows under the current engines. */
@Composable
private fun protectiveBackground(appliedStyle: AppThemeStyle, darkTheme: Boolean): Color {
    val context = LocalContext.current
    return remember(appliedStyle, darkTheme, context) {
        when (appliedStyle) {
            AppThemeStyle.MATERIAL_LOLLIPOP -> if (darkTheme) Color(0xff303030) else Color(0xffeeeeee)
            AppThemeStyle.ECLAIR, AppThemeStyle.FROYO, AppThemeStyle.GINGERBREAD -> if (darkTheme) Color.Black else Color.White
            AppThemeStyle.MATERIAL_ROUNDED -> if (darkTheme) Color(0xff121212) else Color(0xfffafafa)
            AppThemeStyle.HOLO_HONEYCOMB -> if (darkTheme) Color.Black else Color(0xfff3f3f3)
            AppThemeStyle.MIUIX -> {
                if (darkTheme) {
                    top.yukonga.miuix.kmp.theme.darkColorScheme().background
                } else {
                    top.yukonga.miuix.kmp.theme.lightColorScheme().background
                }
            }

            AppThemeStyle.HOLO_KITKAT ->
                if (darkTheme) Color(0xff000000) else Color(0xffe8e8e8)

            AppThemeStyle.HOLO_ICS ->
                if (darkTheme) Color(0xff000000) else Color(0xfff3f3f3)

            AppThemeStyle.MATERIAL_YOU, AppThemeStyle.MATERIAL3_EXPRESSIVE ->
                if (darkTheme) DarkColorScheme.background else LightColorScheme.background
        }
    }
}

/**
 * The stable appearance tree: the activity-scoped transaction ViewModel drives
 * fade/apply/await/fade-in phases while the runtime content (one Navigation,
 * one AppChromeHost) keeps its composition identity across theme providers via
 * the caller's movable content. The protective background interpolates between
 * engine palettes so no white flash leaks through, and content below half
 * alpha stops accepting pointer and accessibility activation.
 */
@Composable
fun AppAppearanceHost(
    viewModel: AppAppearanceTransitionViewModel,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val resolvedDarkTheme = uiState.applied.colorMode.isDark(darkTheme)
    AppSystemBars(style = uiState.applied.style, darkTheme = resolvedDarkTheme)
    val context = LocalContext.current
    val snap = snapAnimations()

    // Failure pings for the page hosting the appearance controls; buffered so a
    // failure landing right before the page subscribes is not silently doubled.
    val appearanceFailures = remember {
        MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    }

    // Keyboard-gate state: the temporary blocker owns focus while the content is
    // hidden, and the pending requester is the triggering control to re-focus on
    // recovery — a plain in-composition handle, never stored in the ViewModel.
    val focusBlocker = remember { FocusRequester() }
    var pendingRestoreFocus by remember { mutableStateOf<FocusRequester?>(null) }

    val alpha = remember {
        Animatable(if (uiState.phase == AppearancePhase.Idle) 1f else 0f)
    }

    // Phase driver. Snap mode (duration scale 0) goes straight to each target
    // and still walks the same state machine so preferences commit exactly once.
    LaunchedEffect(uiState.phase, uiState.generation) {
        val generation = uiState.generation
        when (uiState.phase) {
            AppearancePhase.Idle -> alpha.snapTo(1f)
            AppearancePhase.FadingOut -> {
                if (snap) {
                    alpha.snapTo(0f)
                } else {
                    alpha.animateTo(
                        0f,
                        tween(AppearanceMotion.FadeOutMillis, easing = FastOutSlowInEasing),
                    )
                }
                viewModel.onFadeOutFinished(generation)
            }

            AppearancePhase.Applying, AppearancePhase.AwaitingFrame ->
                alpha.snapTo(0f)

            AppearancePhase.FadingIn -> {
                if (snap) {
                    alpha.snapTo(1f)
                } else {
                    alpha.animateTo(
                        1f,
                        tween(AppearanceMotion.FadeInMillis, easing = FastOutSlowInEasing),
                    )
                }
                viewModel.onFadeInFinished(generation)
            }
        }
    }

    // Commit executor: the only place real setters run. The start mark goes up
    // BEFORE the setters (so a failure between them can never re-issue the same
    // command); success confirmation follows only after everything ran. The
    // effect stays keyed on phase/generation — not commandIssued — so marking
    // started never restarts or cancels itself.
    LaunchedEffect(uiState.phase, uiState.generation) {
        if (uiState.phase == AppearancePhase.Applying && !uiState.commandIssued) {
            val generation = uiState.generation
            viewModel.markCommandStarted(generation)
            try {
                val requested = uiState.requested
                if (requested.style != uiState.applied.style) {
                    AppThemePreferences.setStyle(context, requested.style)
                }
                if (requested.colorMode != uiState.applied.colorMode) {
                    AppThemePreferences.setColorMode(context, requested.colorMode)
                }
                var waitingKey: String? = null
                if (uiState.hasPendingLocale) {
                    val target = uiState.pendingLocaleTag
                    val localeManager = context.getSystemService(LocaleManager::class.java)
                    val currentTags = localeManager?.applicationLocales?.toLanguageTags().orEmpty()
                    val targetTags = target?.let(LocaleList::forLanguageTags)
                        ?: LocaleList.getEmptyLocaleList()
                    if (currentTags != targetTags.toLanguageTags()) {
                        localeManager?.applicationLocales = targetTags
                    }
                    // Empty string key = follow system; null would read as "no wait".
                    waitingKey = target.orEmpty()
                }
                viewModel.onCommandApplied(generation, waitingKey)
            } catch (cancellation: CancellationException) {
                // A newer generation or host teardown owns the follow-up.
                throw cancellation
            } catch (failure: Exception) {
                // The persisted state may be partially applied: recovery reads
                // the real preference snapshot instead of assuming nothing
                // landed or blindly writing the inverse back. Locale truth is
                // not stored here — the picker reads the live setting.
                viewModel.onCommandFailed(generation, AppAppearanceState.fromPreferences())
                appearanceFailures.tryEmit(Unit)
            }
        }
    }

    // Effect commit executor: moreBlur and the glass dock are field-level
    // toggles outside the theme transaction — their applied target IS the
    // commit value. Only these two effect setters run here, one field at a
    // time (never a whole AppAppearanceState write-back that could clobber a
    // mid-transition style); the equality guard keeps cold start — applied
    // seeded from the already-initialized preferences — from re-committing.
    LaunchedEffect(
        uiState.applied.moreBlur,
        uiState.applied.liquidGlassNavigationBar,
    ) {
        if (AppThemePreferences.moreBlur != uiState.applied.moreBlur) {
            AppThemePreferences.setMoreBlur(context, uiState.applied.moreBlur)
        }
        if (AppThemePreferences.liquidGlassNavigationBar !=
            uiState.applied.liquidGlassNavigationBar
        ) {
            AppThemePreferences.setLiquidGlassNavigationBar(
                context,
                uiState.applied.liquidGlassNavigationBar,
            )
        }
    }

    // Locale confirmation: the live configuration must already reflect the
    // target (or the system primary locale for follow-system) before fading in.
    val configuration = LocalConfiguration.current
    LaunchedEffect(uiState.phase, uiState.waitingConfigurationKey, configuration) {
        val state = viewModel.uiState.value
        if (uiState.phase == AppearancePhase.AwaitingFrame && state.hasPendingLocale) {
            val key = uiState.waitingConfigurationKey
            if (key != null && viewModel.uiState.value.waitingConfigurationKey != null) {
                val targetTag = key.ifBlank {
                    context.getSystemService(LocaleManager::class.java)
                        ?.systemLocales?.get(0)?.toLanguageTag().orEmpty()
                }
                val currentTag = configuration.locales[0].toLanguageTag()
                if (currentTag.equals(targetTag, ignoreCase = true)) {
                    viewModel.onConfigurationReady(uiState.generation)
                }
            }
        }
    }

    // Frame confirmation runs only after the configuration (if any) matched.
    LaunchedEffect(uiState.phase, uiState.generation, uiState.waitingConfigurationKey) {
        if (uiState.phase == AppearancePhase.AwaitingFrame) {
            withFrameNanos { }
            viewModel.onFirstFrame(uiState.generation)
        }
    }

    // Failure recovery so the page can never stay transparent; the locale handoff
    // legitimately waits across an Activity recreation, so it gets a longer leash.
    // A timeout converges exactly like a thrown setter failure: back to the real
    // persisted truth, visibly, with one feedback ping.
    LaunchedEffect(uiState.phase, uiState.generation) {
        if (uiState.phase == AppearancePhase.AwaitingFrame) {
            val timeout = if (uiState.hasPendingLocale) {
                AppearanceMotion.LocaleTimeoutMillis
            } else {
                AppearanceMotion.FrameTimeoutMillis
            }
            delay(timeout)
            if (viewModel.uiState.value.generation == uiState.generation &&
                viewModel.uiState.value.phase == AppearancePhase.AwaitingFrame
            ) {
                viewModel.onCommandFailed(uiState.generation, AppAppearanceState.fromPreferences())
                appearanceFailures.tryEmit(Unit)
            }
        }
    }

    val appliedStyle = uiState.applied.style
    val targetBackground = protectiveBackground(appliedStyle, resolvedDarkTheme)
    val background by animateColorAsState(
        targetValue = targetBackground,
        animationSpec = tween(AppearanceMotion.BackgroundMillis, easing = FastOutSlowInEasing),
        label = "appearanceBackground",
    )
    val contentAlpha = alpha.value

    val requester = remember(viewModel) {
        object : AppearanceRequester {
            override fun requestTheme(style: AppThemeStyle, restoreFocus: FocusRequester?) {
                pendingRestoreFocus = restoreFocus
                viewModel.requestTheme(style)
            }

            override fun requestMoreBlur(enabled: Boolean) = viewModel.requestMoreBlur(enabled)
            override fun requestColorMode(mode: AppColorMode, restoreFocus: FocusRequester?) {
                pendingRestoreFocus = restoreFocus
                viewModel.requestColorMode(mode)
            }
            override fun requestGlass(enabled: Boolean) = viewModel.requestGlass(enabled)
            override fun requestAppLocale(tag: String?) = viewModel.requestAppLocale(tag)
            override fun confirmAppLocaleSheetClosed() =
                viewModel.startPendingLocaleFadeOut()
        }
    }

    CompositionLocalProvider(
        LocalAppearanceRequester provides requester,
        LocalAppearanceFailures provides appearanceFailures,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(background),
        ) {
            val gated: @Composable () -> Unit = {
                // Below half alpha the content stops taking input of any kind:
                // invisible content must not be clickable, reachable through the
                // accessibility tree, or keyboard-activatable.
                val gateInput = contentAlpha < 0.5f
                val focusManager = LocalFocusManager.current
                // One-shot per gate transition (not per recomposition): entering
                // clears the current input/button focus and parks keyboard focus
                // on the temporary blocker; recovery hands focus back to the
                // trigger control when its requester is still attached.
                LaunchedEffect(gateInput) {
                    if (gateInput) {
                        focusManager.clearFocus()
                        runCatching { focusBlocker.requestFocus() }
                    } else {
                        val restore = pendingRestoreFocus
                        pendingRestoreFocus = null
                        restore?.let { runCatching { it.requestFocus() } }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { this.alpha = contentAlpha }
                        .then(
                            if (gateInput) {
                                Modifier
                                    .clearAndSetSemantics { }
                                    .onPreviewKeyEvent(::consumesGatedKeyEvent)
                                    .consumeAllPointers()
                            } else {
                                Modifier
                            },
                        ),
                ) {
                    content()
                    if (gateInput) {
                        // The only focus target while gated: activation keys and
                        // shortcuts route into the gate (and get consumed) instead
                        // of any hidden control. No click semantics of its own, and
                        // it leaves with the gate — never part of steady-state
                        // focus traversal.
                        Box(
                            modifier = Modifier
                                .focusRequester(focusBlocker)
                                .focusable(),
                        )
                    }
                }
            }
            AppThemeProvider(darkTheme = resolvedDarkTheme, style = appliedStyle, content = gated)
        }
    }
}

/**
 * Keys the gated (hidden) content must never act on: confirm, space, tab,
 * traversal arrows, and app-wide ctrl/meta shortcuts. The system back key is
 * deliberately absent — back keeps its existing dispatch.
 */
private fun consumesGatedKeyEvent(event: KeyEvent): Boolean {
    val key = event.key
    val activationKey = key == Key.Enter || key == Key.NumPadEnter ||
        key == Key.Spacebar || key == Key.Tab ||
        key == Key.DirectionUp || key == Key.DirectionDown ||
        key == Key.DirectionLeft || key == Key.DirectionRight
    return (activationKey || event.isCtrlPressed || event.isMetaPressed)
}

/**
 * Blocks every pointer event at the initial pass so gated (mostly faded-out)
 * content cannot be activated; the modifier itself installs no gestures.
 */
private fun Modifier.consumeAllPointers(): Modifier =
    pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                event.changes.forEach { it.consume() }
            }
        }
    }
