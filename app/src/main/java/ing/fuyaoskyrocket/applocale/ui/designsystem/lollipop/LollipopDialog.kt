package ing.fuyaoskyrocket.applocale.ui.designsystem.lollipop

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.*

// Animation.Material.Dialog points to popup_enter/exit_material: alpha only, no scale.
internal val LollipopDialogMotion = LegacyDialogMotion(0, 150, LinearEasing,
    Easing { val rest = 1f - it; 1f - rest * rest * rest }, hiddenScale = 1f)

@Composable
internal fun LollipopDialog(visible: Boolean, onDismiss: () -> Unit, onDismissFinished: () -> Unit = {},
    content: @Composable () -> Unit) {
    LegacyDialogWindow(LollipopDialogMotion, visible, onDismiss, onDismissFinished, content = content)
}

@Composable
internal fun LollipopDialogFrame(title: String?, content: @Composable ColumnScope.() -> Unit) {
    val config = LocalConfiguration.current
    val colors = LocalLollipopColors.current
    val parentTheme = ing.fuyaoskyrocket.applocale.ui.designsystem.LocalAppUiTheme.current
    val dialogTheme = ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiThemeValues(
        parentTheme.palette.copy(background = colors.floating, surface = colors.floating, secondarySurface = colors.floating),
        parentTheme.textStyles, parentTheme.style, parentTheme.metrics, parentTheme.policy)
    CompositionLocalProvider(LocalLollipopDialogContent provides true,
        LocalLollipopContentColor provides androidx.compose.ui.graphics.Color.Unspecified,
        ing.fuyaoskyrocket.applocale.ui.designsystem.LocalAppUiTheme provides dialogTheme) {
        Column(Modifier.width(minOf(560.dp, (config.screenWidthDp - 32).coerceAtLeast(1).dp))
            .heightIn(max = config.screenHeightDp.dp * .85f)
            .shadow(ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme.elevation.dialog, RoundedCornerShape(2.dp)).clip(RoundedCornerShape(2.dp)).background(colors.floating, RoundedCornerShape(2.dp))) {
            if (title != null) BasicText(title, Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 18.dp, bottom = 8.dp),
                style = lollipopTextStyle(20, FontWeight.Medium).copy(color = colors.foreground))
            content()
        }
    }
}

@Composable
internal fun LollipopAlertContent(title: String, message: String, confirmText: String, onConfirm: () -> Unit,
    dismissText: String?, onDismiss: () -> Unit) {
    LollipopDialogFrame(title) {
        BasicText(message, Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 18.dp, bottom = 8.dp),
            style = lollipopTextStyle(16).copy(color = LocalLollipopColors.current.foreground))
        FlowRow(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End) {
            if (dismissText != null) LollipopButton(dismissText, onDismiss, borderless = true)
            LollipopButton(confirmText, onConfirm, borderless = true)
        }
    }
}

@Composable
internal fun LollipopChoiceContent(title: String?, content: @Composable () -> Unit) {
    LollipopDialogFrame(title) { Box(Modifier.weight(1f, fill = false).padding(bottom = 8.dp)) { content() } }
}
