package ing.fuyaoskyrocket.applocale.ui.appinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing

/**
 * Compact M3 action strip. All three primary app actions stay on one row;
 * labels remain visible without forcing wide text buttons to wrap.
 */
@Composable
fun AppActionGroup(
    onOpen: () -> Unit,
    onForceStop: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.sm),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        AppActionItem(
            label = stringResource(R.string.open),
            modifier = Modifier.weight(1f),
        ) {
            FilledTonalIconButton(onClick = onOpen) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                    contentDescription = stringResource(R.string.open),
                )
            }
        }
        AppActionItem(
            label = stringResource(R.string.close),
            modifier = Modifier.weight(1f),
        ) {
            OutlinedIconButton(onClick = onForceStop) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.close),
                )
            }
        }
        AppActionItem(
            label = stringResource(R.string.settings),
            modifier = Modifier.weight(1f),
        ) {
            OutlinedIconButton(onClick = onSettings) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = stringResource(R.string.settings),
                )
            }
        }
    }
}

@Composable
private fun AppActionItem(
    label: String,
    modifier: Modifier = Modifier,
    button: @Composable () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        button()
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
        )
    }
}
