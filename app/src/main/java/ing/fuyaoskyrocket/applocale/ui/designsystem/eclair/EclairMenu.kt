package ing.fuyaoskyrocket.applocale.ui.designsystem.eclair

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppDropdownItem
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.*

internal val LocalEclairMenuContent = staticCompositionLocalOf { false }
internal val LocalEclairMenuDismiss = staticCompositionLocalOf<() -> Unit> { {} }

@Composable
internal fun EclairMenuCommand(text: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    enabled: Boolean = true, symbol: ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSymbol? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val dismissMenu = LocalEclairMenuDismiss.current
    val state = legacyControlState(interaction, enabled)
    Column(modifier.widthIn(min = 96.dp).heightIn(min = 64.dp)
        .legacyBackground(rememberClassicDrawable(R.drawable.eclair_menuitem_background), state)
        .clickable(interaction, null, enabled, role = Role.Button) {
            if (symbol != ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSymbol.Menu) dismissMenu()
            onClick()
        }
        .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
    ) {
        if (symbol != null) EclairSymbolIcon(symbol, null)
        BasicText(text, style = eclairTextStyle(14).copy(color = eclairColor(classicMenuTextResource, state)))
    }
}

@Composable
internal fun EclairToolbarButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    enabled: Boolean = true, symbol: ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSymbol? = null,
) {
    if (LocalEclairMenuContent.current) EclairMenuCommand(text, onClick, modifier, enabled, symbol)
    else EclairButton(text, onClick, modifier, enabled)
}

@Composable
internal fun EclairMenu(expanded: Boolean, onDismiss: () -> Unit, items: List<AppDropdownItem>) {
    if (!expanded) return
    val configuration = LocalConfiguration.current
    val dismissParent = LocalEclairMenuDismiss.current
    Popup(alignment = Alignment.TopEnd, onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true),
    ) {
        LegacyDialogSurface(rememberClassicDrawable(R.drawable.eclair_menu_background), classicMenuFace,
            Modifier.widthIn(min = 220.dp, max = minOf(360.dp, configuration.screenWidthDp.dp - 16.dp))
                .heightIn(max = configuration.screenHeightDp.dp * .7f)) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(4.dp)) {
            items.forEach { item ->
                key(item.text) {
                    val interaction = remember { MutableInteractionSource() }
                    val state = legacyControlState(interaction, enabled = item.enabled, selected = item.selected)
                    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        .legacyBackground(rememberClassicDrawable(R.drawable.eclair_menu_selector), state)
                        .clickable(interaction, null, enabled = item.enabled, role = Role.Button) { if (expanded) { onDismiss(); dismissParent(); item.onClick() } }
                        .semantics { selected = item.selected }.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicText(item.text, Modifier.weight(1f), style = eclairTextStyle(18).copy(color = eclairColor(classicMenuTextResource, state)))
                        if (item.selected) EclairRadioButton(true, null)
                    }
                }
            }
        }
        }
    }
}
