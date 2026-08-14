package ing.fuyaoskyrocket.applocale.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing

/**
 * M3 [BottomAppBar] shown during multi-selection mode.
 * The selection count remains in the contextual top app bar, leaving enough
 * width here for an unclipped primary action in every supported language.
 */
@Composable
fun BatchBottomAppBar(
    hasSelection: Boolean,
    isApplying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BottomAppBar(modifier = modifier) {
        if (isApplying) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Button(
                onClick = onClick,
                enabled = hasSelection,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.lg),
            ) {
                Text(stringResource(R.string.set_language))
            }
        }
    }
}
