package ing.fuyaoskyrocket.applocale.ui.designsystem.eclair

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppBatchSelectionActions
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.*

/** Selection on a pre-ActionBar platform: count and every command live in the bottom panel. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun EclairBatchPanel(
    actions: AppBatchSelectionActions, hasSelection: Boolean, isApplying: Boolean,
    onApply: () -> Unit, modifier: Modifier = Modifier, interactive: Boolean = true,
) {
    Column(modifier.fillMaxWidth().legacyBackground(rememberClassicDrawable(R.drawable.eclair_menu_background)).padding(4.dp)) {
        BasicText(stringResource(R.string.selected_count, actions.count),
            Modifier.padding(horizontal = 6.dp), style = eclairTextStyle(14).copy(color = Color.Black))
        FlowRow(Modifier.fillMaxWidth()) {
            EclairButton(stringResource(R.string.exit_selection), actions.onClose, enabled = interactive && !isApplying)
            EclairButton(stringResource(R.string.select_all), actions.onSelectAll, enabled = interactive && !isApplying)
            EclairButton(stringResource(R.string.clear_selection), actions.onClear, enabled = interactive && !isApplying)
            EclairButton(stringResource(R.string.set_language), onApply, enabled = interactive && hasSelection && !isApplying)
        }
        if (isApplying) EclairProgress(horizontal = true)
    }
}
