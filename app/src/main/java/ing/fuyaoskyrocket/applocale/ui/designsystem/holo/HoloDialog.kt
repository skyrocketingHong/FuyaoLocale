package ing.fuyaoskyrocket.applocale.ui.designsystem.holo

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.LegacyDialogSurface
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/** Holo keeps its fixed-era timing; the shared window owns cancellation and mounting. */
@Composable
internal fun HoloDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onDismissFinished: () -> Unit = {},
    modifier: Modifier = Modifier,
    usePlatformDefaultWidth: Boolean = false,
    content: @Composable () -> Unit,
) = ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.LegacyDialogWindow(
    motion = ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.LegacyDialogMotion(
        HoloDialogDefaultDurMillis, HoloDialogShortDurMillis, HoloDecelerateQuint, HoloDecelerateCubic,
    ),
    visible = visible, onDismiss = onDismiss, onDismissFinished = onDismissFinished,
    modifier = modifier, usePlatformDefaultWidth = usePlatformDefaultWidth, content = content,
)

/** The era alert content: title, 2dp blue title divider, scrollable body, divided button bar. */
@Composable
internal fun HoloAlertScaffold(
    title: String,
    message: String?,
    confirmText: String,
    onConfirm: () -> Unit,
    dismissText: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalHoloMetrics.current
    val textColors = LocalHoloTextColors.current
    val frame = rememberHoloDrawable(HoloAsset.DialogFrame)
    val configuration = LocalConfiguration.current
    val maxDialogWidth = minOf(configuration.screenWidthDp.dp - 16.dp, 560.dp)

    LegacyDialogSurface(
        frame = frame,
        color = holoDialogSurfaceColor(),
        modifier = modifier
            .width(maxDialogWidth)
            .heightIn(max = configuration.screenHeightDp.dp * .85f),
    ) {
        // Title template: icon (unused here) + single-line 18sp title,
        // minHeight 64dp, margins 16dp.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp)
                .heightIn(min = metrics.alertDialogTitleHeight),
            contentAlignment = Alignment.CenterStart,
        ) {
            HoloText(
                text = title,
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = LocalHoloFontFamily.current,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = textColors.primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        // titleDivider: 2dp holo_blue_light, gone without a title area.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Color(0xff33b5e5)),
        )
        if (message != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .heightIn(max = 320.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                HoloText(
                    text = message,
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = LocalHoloFontFamily.current,
                        fontSize = 14.sp,
                    ),
                    color = textColors.secondary,
                )
            }
        }
        // Button bar: negative left, positive right, equal weights, 48dp tall,
        // thin vertical dividers between (Holo.ButtonBar.AlertDialog).
        Row(modifier = Modifier.fillMaxWidth().heightIn(min = metrics.alertDialogButtonBarHeight)) {
            if (dismissText != null) {
                HoloBorderlessButton(
                    text = dismissText,
                    onClick = onDismiss,
                    small = true,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(48.dp)
                        .background(ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme.palette.divider),
                )
            }
            HoloBorderlessButton(
                text = confirmText,
                onClick = onConfirm,
                small = true,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** Opaque face colors sampled from each pinned dialog frame, not from the page background. */
@Composable
internal fun holoDialogSurfaceColor(): Color {
    val dark = LocalHoloDarkTheme.current
    return when (ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme.style) {
        ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemeStyle.HOLO_HONEYCOMB -> if (dark) Color.Black else Color(0xffefefef)
        ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemeStyle.HOLO_KITKAT -> if (dark) Color(0xff282828) else Color(0xfff5f5f5)
        else -> if (dark) Color(0xff282828) else Color(0xfff3f3f3)
    }
}
