package ing.fuyaoskyrocket.applocale.ui.designsystem.eclair

import androidx.compose.animation.core.Easing
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.*

private val EclairDecelerate = Easing { fraction -> 1f - (1f - fraction) * (1f - fraction) }

@Composable
internal fun EclairDialog(
    visible: Boolean, onDismiss: () -> Unit, onDismissFinished: () -> Unit = {}, content: @Composable () -> Unit,
) {
    val duration = integerResource(classicResource(R.integer.eclair_config_shortAnimTime))
    LegacyDialogWindow(LegacyDialogMotion(duration, duration, EclairDecelerate, EclairDecelerate),
        visible, onDismiss, onDismissFinished, content = content)
}

/** Solid content sits inside the original panel insets; the frame keeps its own shadow. */
@Composable
internal fun EclairAlertContent(
    title: String, message: String, confirmText: String, onConfirm: () -> Unit,
    dismissText: String?, onDismiss: () -> Unit,
) {
    val configuration = LocalConfiguration.current
    Column(Modifier.width(minOf(configuration.screenWidthDp.dp - 16.dp, 560.dp))
        .heightIn(max = configuration.screenHeightDp.dp * .85f)) {
        EclairDialogTitle(title)
        LegacyDialogSurface(rememberClassicDrawable(R.drawable.eclair_popup_center_dark), classicDialogDarkFace,
            Modifier.weight(1f, fill = false)) {
            Box(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 14.dp, vertical = 12.dp)) {
                BasicText(message, style = eclairTextStyle(18).copy(color = Color.White))
            }
        }
        LegacyDialogSurface(rememberClassicDrawable(R.drawable.eclair_popup_bottom_medium), classicDialogButtonFace) {
            Row(Modifier.fillMaxWidth().heightIn(min = 54.dp).padding(horizontal = 2.dp, vertical = 4.dp)) {
                EclairButton(confirmText, onConfirm, Modifier.weight(1f))
                if (dismissText != null) EclairButton(dismissText, onDismiss, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun EclairDialogTitle(title: String) {
    LegacyDialogSurface(rememberClassicDrawable(R.drawable.eclair_popup_top_dark), classicDialogDarkFace) {
        Box(Modifier.fillMaxWidth().heightIn(min = 54.dp).padding(horizontal = 10.dp, vertical = 8.dp),
            contentAlignment = Alignment.CenterStart) {
            BasicText(title, style = eclairTextStyle(22, true).copy(color = Color.White))
        }
    }
}

/** The original light list face is opaque; decorations cannot draw beyond its content bounds. */
@Composable
internal fun EclairChoiceContent(title: String?, content: @Composable () -> Unit) {
    val configuration = LocalConfiguration.current
    Column(Modifier.width(minOf(configuration.screenWidthDp.dp - 16.dp, 560.dp))
        .heightIn(max = configuration.screenHeightDp.dp * .85f)) {
        if (title != null) EclairDialogTitle(title)
        LegacyDialogSurface(rememberClassicDrawable(if (title == null) R.drawable.eclair_popup_full_bright
            else R.drawable.eclair_popup_bottom_bright), Color.White, Modifier.weight(1f, fill = false)) {
            EclairAppTheme(darkTheme = false, dialog = true, content = content)
        }
    }
}
