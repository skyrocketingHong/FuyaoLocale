package ing.fuyaoskyrocket.applocale.ui.designsystem

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Phases of one appearance transaction; see plans/round-4/015-preference-transitions.md. */
enum class AppearancePhase { Idle, FadingOut, Applying, AwaitingFrame, FadingIn }

data class AppAppearanceUiState(
    val phase: AppearancePhase = AppearancePhase.Idle,
    val generation: Long = 0,
    val requested: AppAppearanceState = AppAppearanceState.fromPreferences(),
    val applied: AppAppearanceState = AppAppearanceState.fromPreferences(),
    val hasPendingLocale: Boolean = false,
    val pendingLocaleTag: String? = null,
    val commandIssued: Boolean = false,
    val waitingConfigurationKey: String? = null,
)

/**
 * Activity-scoped appearance transaction state: only values live here — no
 * Context, View, Composable lambda or NavController. The real side effects
 * (preference setters, LocaleManager) are executed by the host at the commit
 * step; this model only sequences them. It survives configuration recreation,
 * not process death.
 */
class AppAppearanceTransitionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AppAppearanceUiState())
    val uiState: StateFlow<AppAppearanceUiState> = _uiState.asStateFlow()

    // ------------------------------------------------------------ requests

    fun requestTheme(style: AppThemeStyle) {
        val current = _uiState.value
        if (style == current.requested.style && current.phase == AppearancePhase.Idle &&
            !current.hasPendingLocale
        ) {
            return
        }
        // An un-committed older target is replaced field-by-field; a committed
        // one has already started a transition the new request supersedes via a
        // new generation.
        _uiState.update { state ->
            state.copy(
                requested = state.requested.copy(style = style),
                generation = state.generation + 1,
                phase = AppearancePhase.FadingOut,
            )
        }
    }

    fun requestMoreBlur(enabled: Boolean) {
        // Effect toggles commit immediately (their own layers animate); no
        // fade transaction is started.
        _uiState.update { state ->
            if (enabled == state.requested.moreBlur) {
                state
            } else {
                state.copy(
                    requested = state.requested.copy(moreBlur = enabled),
                    applied = state.applied.copy(moreBlur = enabled),
                )
            }
        }
    }

    fun requestGlass(enabled: Boolean) {
        _uiState.update { state ->
            if (enabled == state.requested.liquidGlassNavigationBar) {
                state
            } else {
                state.copy(
                    requested = state.requested.copy(liquidGlassNavigationBar = enabled),
                    applied = state.applied.copy(liquidGlassNavigationBar = enabled),
                )
            }
        }
    }

    fun requestAppLocale(tag: String?) {
        val current = _uiState.value
        if (current.hasPendingLocale && current.pendingLocaleTag == tag) {
            // Repeated taps on the same pending target issue no second command.
            return
        }
        _uiState.update { state ->
            state.copy(
                hasPendingLocale = true,
                pendingLocaleTag = tag,
                generation = state.generation + 1,
                // The fade-out only starts once the picker sheet has actually
                // closed; the host calls startPendingLocaleFadeOut then.
                phase = state.phase,
            )
        }
    }

    /** The picker window has finished closing; now the page may fade out. */
    fun startPendingLocaleFadeOut() {
        _uiState.update { state ->
            if (state.hasPendingLocale && state.phase == AppearancePhase.Idle) {
                state.copy(phase = AppearancePhase.FadingOut)
            } else {
                state
            }
        }
    }

    // -------------------------------------------------------------- events

    fun onFadeOutFinished(generation: Long) {
        _uiState.update { state ->
            if (generation != state.generation || state.phase != AppearancePhase.FadingOut) {
                state
            } else {
                state.copy(phase = AppearancePhase.Applying, commandIssued = false)
            }
        }
    }

    fun markCommandStarted(generation: Long) {
        // Only flips the dedup flag — never the phase — so the host executor
        // marking started cannot restart or cancel itself mid-command.
        _uiState.update { state ->
            if (generation != state.generation || state.phase != AppearancePhase.Applying) {
                state
            } else {
                state.copy(commandIssued = true)
            }
        }
    }

    fun onCommandApplied(generation: Long, waitingConfigurationKey: String?) {
        _uiState.update { state ->
            if (generation != state.generation || state.phase != AppearancePhase.Applying) {
                state
            } else {
                state.copy(
                    applied = state.requested,
                    waitingConfigurationKey = waitingConfigurationKey,
                    phase = AppearancePhase.AwaitingFrame,
                )
            }
        }
    }

    fun onCommandFailed(generation: Long, actualAppearance: AppAppearanceState) {
        _uiState.update { state ->
            if (generation != state.generation) {
                state
            } else {
                // Correct only what this failed transaction owns — the style —
                // to the real persisted value; the two effect fields keep the
                // latest requested/applied targets (their independent executor
                // commits them, and this older snapshot must not clobber them).
                // Pending language targets are dropped; the picker shows the
                // live locale truth on its next read.
                state.copy(
                    requested = state.requested.copy(style = actualAppearance.style),
                    applied = state.applied.copy(style = actualAppearance.style),
                    phase = AppearancePhase.Idle,
                    hasPendingLocale = false,
                    pendingLocaleTag = null,
                    commandIssued = false,
                    waitingConfigurationKey = null,
                )
            }
        }
    }

    fun onConfigurationReady(generation: Long) {
        _uiState.update { state ->
            if (generation != state.generation || state.phase != AppearancePhase.AwaitingFrame) {
                state
            } else {
                state.copy(waitingConfigurationKey = null)
            }
        }
    }

    fun onFirstFrame(generation: Long) {
        _uiState.update { state ->
            if (generation != state.generation || state.phase != AppearancePhase.AwaitingFrame) {
                state
            } else if (state.hasPendingLocale && state.waitingConfigurationKey != null) {
                // Locale still unconfirmed: keep waiting for the configuration.
                state
            } else {
                state.copy(phase = AppearancePhase.FadingIn)
            }
        }
    }

    fun onFadeInFinished(generation: Long) {
        _uiState.update { state ->
            if (generation != state.generation) {
                state
            } else {
                state.copy(
                    phase = AppearancePhase.Idle,
                    hasPendingLocale = false,
                    pendingLocaleTag = null,
                    commandIssued = false,
                    waitingConfigurationKey = null,
                )
            }
        }
    }
}
