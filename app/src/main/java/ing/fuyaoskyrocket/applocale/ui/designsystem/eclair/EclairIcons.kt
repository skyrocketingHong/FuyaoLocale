package ing.fuyaoskyrocket.applocale.ui.designsystem.eclair

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSymbol
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.*

@DrawableRes
internal fun eclairSymbolResource(symbol: AppSymbol): Int = when (symbol) {
    AppSymbol.Search -> R.drawable.eclair_ic_menu_search
    AppSymbol.Menu -> R.drawable.eclair_ic_menu_more
    AppSymbol.Back -> R.drawable.eclair_ic_menu_back
    AppSymbol.Close -> R.drawable.eclair_ic_menu_close_clear_cancel
    AppSymbol.Refresh -> R.drawable.eclair_ic_menu_refresh
    AppSymbol.Settings -> R.drawable.eclair_ic_menu_preferences
    AppSymbol.Pin, AppSymbol.PinActive -> R.drawable.eclair_ic_menu_star
    AppSymbol.Check, AppSymbol.SelectAll -> R.drawable.eclair_ic_menu_mark
    AppSymbol.Sort -> R.drawable.eclair_ic_menu_sort_alphabetically
    AppSymbol.Forward -> R.drawable.eclair_ic_menu_forward
    AppSymbol.Delete -> R.drawable.eclair_ic_menu_delete
    AppSymbol.Info -> R.drawable.eclair_ic_menu_info_details
    AppSymbol.Add -> R.drawable.eclair_ic_menu_add
}

@Composable
internal fun EclairSymbolIcon(symbol: AppSymbol, contentDescription: String?, modifier: Modifier = Modifier) {
    Box(modifier.size(32.dp).then(if (contentDescription != null) Modifier.semantics {
        this.contentDescription = contentDescription
    } else Modifier).legacyDrawable(rememberClassicDrawable(eclairSymbolResource(symbol)),
        boundsMode = LegacyDrawableBounds.Fit, alignment = Alignment.Center))
}
