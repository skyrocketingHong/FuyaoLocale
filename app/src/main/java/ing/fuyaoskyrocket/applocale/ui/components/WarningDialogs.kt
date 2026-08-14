package ing.fuyaoskyrocket.applocale.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppLayout
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing

@Composable
fun ShizukuRequiredWarning(
    onRequestPermission: () -> Unit,
    onOpenShizuku: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier
                .padding(AppSpacing.xl)
                .widthIn(max = AppLayout.warningMaxWidth)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                Icon(
                    imageVector = Icons.Outlined.WarningAmber,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(R.string.permissions_required),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = stringResource(R.string.shizuku_required),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    TextButton(onClick = onOpenShizuku) {
                        Text(stringResource(R.string.open_shizuku))
                    }
                    FilledTonalButton(onClick = onRequestPermission) {
                        Text(stringResource(R.string.request_shizuku_permission))
                    }
                }
            }
        }
    }
}

/**
 * Non-blocking state shown while a permission that was already granted is
 * reconnecting after process recreation or a configuration change.
 */
@Composable
fun ShizukuConnectingState(
    onOpenShizuku: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            modifier = Modifier.padding(AppSpacing.xl),
        ) {
            CircularProgressIndicator()
            Text(
                text = stringResource(R.string.connecting_to_shizuku),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.connecting_to_shizuku_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = onOpenShizuku) {
                Text(stringResource(R.string.open_shizuku))
            }
        }
    }
}

@Composable
fun SystemDialogWarn(
    onClickContinue: () -> Unit,
    onClickCancel: () -> Unit,
) {
    AlertDialog(
        icon = {
            Icon(
                imageVector = Icons.Outlined.WarningAmber,
                contentDescription = null,
            )
        },
        text = { Text(stringResource(R.string.warning_system_apps)) },
        title = { Text(stringResource(R.string.warning)) },
        onDismissRequest = { onClickCancel() },
        confirmButton = {
            TextButton(onClick = { onClickContinue() }) {
                Text(stringResource(R.string.proceed))
            }
        },
        dismissButton = {
            TextButton(onClick = { onClickCancel() }) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
