package ing.fuyaoskyrocket.applocale.ui.designsystem.lollipop

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppDropdownItem
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.LegacyDialogTransition

@Composable
internal fun LollipopMenu(expanded: Boolean, onDismiss: () -> Unit, items: List<AppDropdownItem>) {
    val transition = remember { LegacyDialogTransition(LollipopDialogMotion) }
    LaunchedEffect(expanded) { if (expanded) transition.show(false) else if (transition.mounted) transition.hide(false) }
    if (!transition.mounted) return
    val config = LocalConfiguration.current
    val colors = LocalLollipopColors.current
    val currentExpanded by rememberUpdatedState(expanded)
    CompositionLocalProvider(LocalLollipopContentColor provides androidx.compose.ui.graphics.Color.Unspecified) {
    Popup(alignment = Alignment.TopEnd, onDismissRequest = onDismiss, properties = PopupProperties(focusable = true)) {
        Column(Modifier.widthIn(min = 196.dp, max = minOf(360.dp, (config.screenWidthDp - 32).coerceAtLeast(1).dp))
            .heightIn(max = config.screenHeightDp.dp * .7f).graphicsLayer { alpha = transition.alpha.value }
            .shadow(ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme.elevation.menu, RoundedCornerShape(2.dp)).clip(RoundedCornerShape(2.dp)).background(ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme.palette.background, RoundedCornerShape(2.dp))
            .padding(vertical = 8.dp).verticalScroll(rememberScrollState())) {
            items.forEach { item ->
                key(item.text) {
                    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        .clickable(enabled = item.enabled) { if (currentExpanded) { onDismiss(); item.onClick() } }
                        .semantics { selected = item.selected }.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        BasicText(item.text, Modifier.weight(1f).padding(vertical = 8.dp), style = lollipopTextStyle(16).copy(color = colors.foreground.copy(alpha = if (item.enabled) 1f else .38f)))
                        if (item.selected) LollipopRadioButton(true, null)
                    }
                }
            }
        }
    }
    }
}
