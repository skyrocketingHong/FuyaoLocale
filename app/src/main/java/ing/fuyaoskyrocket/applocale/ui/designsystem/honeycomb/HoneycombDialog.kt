package ing.fuyaoskyrocket.applocale.ui.designsystem.honeycomb

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.*
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.*
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.*

/** API 11 alert_dialog_holo: 60dp title, 32dp content margins, original four-pixel strong divider. */
@Composable
internal fun HoneycombAlertContent(
    title: String, message: String, confirmText: String, onConfirm: () -> Unit,
    dismissText: String?, onDismiss: () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val colors = LocalHoloTextColors.current
    LegacyDialogSurface(rememberHoloDrawable(HoloAsset.DialogFrame), holoDialogSurfaceColor(),
        Modifier.width(minOf(configuration.screenWidthDp.dp - 16.dp, 560.dp))
            .heightIn(max = configuration.screenHeightDp.dp * 0.85f)) {
        HoneycombDialogTitle(title)
        Box(Modifier.fillMaxWidth().weight(1f, false).verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 32.dp)) {
            HoloText(message, style = TextStyle(fontFamily = HoneycombFontFamily, fontSize = 16.sp), color = colors.primary)
        }
        HoloDivider(Modifier.padding(horizontal = 16.dp))
        Row(Modifier.fillMaxWidth().heightIn(min = 54.dp).padding(horizontal = 2.dp)) {
            // Button1 (positive) precedes button2 (negative) in the original API 11 layout.
            HoloBorderlessButton(confirmText, onConfirm, Modifier.weight(1f), small = false)
            if (dismissText != null) {
                Box(Modifier.width(1.dp).height(38.dp).align(Alignment.CenterVertically)
                    .holoBackground(rememberHoloDrawable(HoloAsset.VerticalDivider)))
                HoloBorderlessButton(dismissText, onDismiss, Modifier.weight(1f), small = false)
            }
        }
    }
}

@Composable
private fun HoneycombDialogTitle(title: String) {
    Box(Modifier.fillMaxWidth().heightIn(min = 60.dp).padding(horizontal = 32.dp), contentAlignment = Alignment.CenterStart) {
        HoloText(title, style = TextStyle(fontFamily = HoneycombFontFamily, fontSize = 18.sp), color = LocalHoloTextColors.current.primary)
    }
    Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(4.dp)
        .legacyBackground(rememberLegacyDrawable(R.drawable.honeycomb_divider_strong_holo)))
}

@Composable
internal fun HoneycombChoiceContent(title: String?, content: @Composable () -> Unit) {
    val configuration = LocalConfiguration.current
    val theme = LocalAppUiTheme.current
    LegacyDialogSurface(rememberHoloDrawable(HoloAsset.DialogFrame), holoDialogSurfaceColor(),
        Modifier.width(minOf(configuration.screenWidthDp.dp - 16.dp, 560.dp))
            .heightIn(max = configuration.screenHeightDp.dp * .85f)) {
        if (title != null) HoneycombDialogTitle(title)
        Box(Modifier.weight(1f, fill = false).heightIn(max = configuration.screenHeightDp.dp * 0.70f).padding(bottom = 8.dp)) {
            CompositionLocalProvider(
                LocalHoloListPadding provides 16.dp,
                LocalAppUiTheme provides AppUiThemeValues(
                    theme.palette, theme.textStyles.copy(itemTitle = theme.textStyles.itemTitle.copy(fontSize = 18.sp)),
                    theme.style, theme.metrics.copy(listPreferredItemPaddingHorizontal = 16.dp), theme.policy,
                ), content = content,
            )
        }
    }
}
