package ing.fuyaoskyrocket.applocale.ui.designsystem.material2

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.LocalGlassBackdrop

/** Native M2 sheet in a full-window dialog, retaining the shared controlled-close contract. */
@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun RoundedModalSheet(
    visible: Boolean, onDismiss: () -> Unit, modifier: Modifier, title: String?,
    onDismissFinished: () -> Unit, content: @Composable () -> Unit,
) {
    val wanted by rememberUpdatedState(visible)
    val dismiss by rememberUpdatedState(onDismiss)
    val finished by rememberUpdatedState(onDismissFinished)
    var mounted by remember { mutableStateOf(false) }
    var programmaticHide by remember { mutableStateOf(false) }
    val state = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true,
        confirmValueChange = { next ->
            if (next == ModalBottomSheetValue.Hidden && wanted && !programmaticHide) {
                dismiss()
                false // The parent flips visible, then the single controlled hide runs.
            } else true
        },
    )
    LaunchedEffect(visible, mounted) {
        when {
            !mounted && visible -> mounted = true
            mounted && visible -> state.show()
            mounted -> {
                programmaticHide = true
                try { state.hide() } finally { programmaticHide = false }
            }
        }
    }
    LaunchedEffect(mounted) {
        if (!mounted) return@LaunchedEffect
        snapshotFlow { Triple(wanted, state.currentValue, state.targetValue) }.collect { (requested, current, target) ->
            if (!requested && current == ModalBottomSheetValue.Hidden && target == ModalBottomSheetValue.Hidden) {
                mounted = false
                finished()
            }
        }
    }
    if (mounted) Dialog(onDismissRequest = { if (wanted) dismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        CompositionLocalProvider(LocalGlassBackdrop provides null) {
            val height = LocalConfiguration.current.screenHeightDp.dp * .9f
            ModalBottomSheetLayout(
                sheetState = state,
                sheetGesturesEnabled = wanted,
                modifier = modifier.fillMaxSize().imePadding(),
                sheetShape = RoundedCornerShape(topStart = AppUiTheme.shapes.dialog.radius, topEnd = AppUiTheme.shapes.dialog.radius),
                sheetElevation = AppUiTheme.elevation.dialog,
                sheetContent = {
                    Column(Modifier.fillMaxWidth().heightIn(max = height).navigationBarsPadding()) {
                        if (title != null) Text(title, Modifier.padding(16.dp), style = MaterialTheme.typography.h6)
                        content()
                    }
                },
            ) { Box(Modifier.fillMaxSize()) }
        }
    }
}
