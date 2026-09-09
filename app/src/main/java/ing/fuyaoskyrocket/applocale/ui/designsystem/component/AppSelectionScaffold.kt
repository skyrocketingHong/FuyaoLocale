package ing.fuyaoskyrocket.applocale.ui.designsystem.component

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.SelectionPresentation
import ing.fuyaoskyrocket.applocale.ui.designsystem.shouldShowBatchBar

/** Selection intent, independent of contextual bars and bottom action panels. */
data class AppSelectionState(
    val count: Int,
    val applying: Boolean,
    val onClose: () -> Unit,
    val onSelectAll: () -> Unit,
    val onClear: () -> Unit,
    val onApply: () -> Unit,
)

@Composable
fun AppSelectionScaffold(
    selection: AppSelectionState?,
    topBar: @Composable () -> Unit,
    snackbarHostState: AppSnackbarHostState,
    containerColor: Color = AppUiTheme.palette.background,
    content: @Composable (PaddingValues) -> Unit,
) {
    var lastSelection by remember { mutableStateOf<AppSelectionState?>(null) }
    SideEffect { if (selection != null && lastSelection != selection) lastSelection = selection }
    val renderSelection = selection ?: lastSelection
    val contextBar = AppUiTheme.components.selection == SelectionPresentation.ContextBar
    val dockVacated = shouldShowBatchBar()
    val pageActive = ing.fuyaoskyrocket.applocale.ui.screen.LocalTopLevelPageActive.current
    val visible = remember { MutableTransitionState(false) }
    LaunchedEffect(selection != null, contextBar, dockVacated, pageActive) {
        visible.targetState = selection != null && !contextBar && dockVacated && pageActive
    }
    val transition = rememberTransition(visible, label = "SelectionPanel")
    val duration = AppUiTheme.motion.batchEnterMillis
    val progress by transition.animateFloat(transitionSpec = { tween(duration) }, label = "SelectionPanelProgress") {
        if (it) 1f else 0f
    }
    val keepPanel = !contextBar && (selection != null || visible.currentState || visible.targetState || !visible.isIdle)
    LaunchedEffect(keepPanel, selection) {
        if (!keepPanel && selection == null) lastSelection = null
    }
    val density = LocalDensity.current
    var panelHeight by remember { mutableStateOf(0.dp) }
    AppScaffold(
        topLevelNavigation = true,
        contextual = selection != null,
        topBar = {
            if (selection == null) topBar()
            else SelectionTopAppBar(selection.count, selection.onClose, selection.onSelectAll, selection.onClear,
                onApply = if (contextBar) selection.onApply else null, isApplying = selection.applying)
        },
        bottomBar = {
            if (keepPanel && renderSelection != null) Box(Modifier.onSizeChanged { panelHeight = with(density) { it.height.toDp() } }) {
                BatchBottomAppBar(
                    hasSelection = selection != null && selection.count > 0,
                    isApplying = renderSelection.applying,
                    onClick = renderSelection.onApply,
                    presentationProgress = { progress },
                    interactive = selection != null && dockVacated && pageActive,
                    selectionActions = AppBatchSelectionActions(renderSelection.count, renderSelection.onClose, renderSelection.onSelectAll, renderSelection.onClear),
                )
            }
        },
        snackbarHost = { AppSnackbarHost(snackbarHostState, hostSlotBottom = if (keepPanel) panelHeight else 0.dp) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = containerColor,
        content = content,
    )
}
