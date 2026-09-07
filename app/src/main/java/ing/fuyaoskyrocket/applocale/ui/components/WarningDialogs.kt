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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppLayout
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppAlertDialog
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppButton
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppCard
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppCircularProgressIndicator
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppIcon
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppText
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppTextButton

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
        AppCard(
            modifier = Modifier
                .padding(AppSpacing.xl)
                .widthIn(max = AppLayout.warningMaxWidth)
                .fillMaxWidth(),
            containerColor = AppUiTheme.palette.secondarySurface,
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                // No miuix warning glyph; the project vector is drawn by the
                // backend icon control (asset exception).
                AppIcon(
                    imageVector = Icons.Outlined.WarningAmber,
                    contentDescription = null,
                    tint = AppUiTheme.palette.accent,
                )
                AppText(
                    text = stringResource(R.string.permissions_required),
                    style = AppUiTheme.textStyles.pageTitle,
                )
                AppText(
                    text = stringResource(R.string.shizuku_required),
                    style = AppUiTheme.textStyles.body,
                    color = AppUiTheme.palette.muted,
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    AppTextButton(
                        text = stringResource(R.string.open_shizuku),
                        onClick = onOpenShizuku,
                    )
                    AppButton(
                        text = stringResource(R.string.request_shizuku_permission),
                        onClick = onRequestPermission,
                    )
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
            AppCircularProgressIndicator()
            AppText(
                text = stringResource(R.string.connecting_to_shizuku),
                style = AppUiTheme.textStyles.itemTitle,
            )
            AppText(
                text = stringResource(R.string.connecting_to_shizuku_description),
                style = AppUiTheme.textStyles.body,
                color = AppUiTheme.palette.muted,
            )
            AppTextButton(
                text = stringResource(R.string.open_shizuku),
                onClick = onOpenShizuku,
            )
        }
    }
}

/**
 * System-app confirmation for batch changes. Stays in composition while hidden;
 * [onDismissFinished] runs once after the window has actually closed, letting the
 * caller hand over to the next overlay without two windows overlapping.
 */
@Composable
fun SystemDialogWarn(
    visible: Boolean,
    onClickContinue: () -> Unit,
    onClickCancel: () -> Unit,
    onDismissFinished: () -> Unit = {},
) {
    AppAlertDialog(
        visible = visible,
        title = stringResource(R.string.warning),
        message = stringResource(R.string.warning_system_apps),
        confirmText = stringResource(R.string.proceed),
        onConfirm = onClickContinue,
        dismissText = stringResource(R.string.cancel),
        onDismiss = onClickCancel,
        onDismissFinished = onDismissFinished,
    )
}
