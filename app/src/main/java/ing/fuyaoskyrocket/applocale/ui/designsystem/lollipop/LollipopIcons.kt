package ing.fuyaoskyrocket.applocale.ui.designsystem.lollipop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSymbol
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.*

/** Framework material glyphs plus fixed-tag application-command images; see SOURCE_DIFFERENCES. */
internal fun lollipopSymbolResource(symbol: AppSymbol) = when (symbol) {
    AppSymbol.Search -> R.drawable.lollipop_ic_menu_search_material
    AppSymbol.Menu -> R.drawable.lollipop_ic_menu_moreoverflow_material
    AppSymbol.Back -> R.drawable.lollipop_ic_ab_back_material
    AppSymbol.Close -> R.drawable.lollipop_ic_clear_material
    AppSymbol.Refresh -> R.drawable.lollipop_ic_menu_refresh
    AppSymbol.Settings -> R.drawable.lollipop_ic_settings
    AppSymbol.Pin, AppSymbol.PinActive -> R.drawable.lollipop_btn_star_material
    AppSymbol.Check -> R.drawable.lollipop_ic_cab_done_mtrl_alpha
    AppSymbol.SelectAll -> R.drawable.lollipop_ic_menu_selectall_material
    AppSymbol.Sort -> R.drawable.lollipop_ic_menu_sort_by_size
    AppSymbol.Forward -> R.drawable.lollipop_ic_menu_forward
    AppSymbol.Delete -> R.drawable.lollipop_ic_menu_delete
    AppSymbol.Info -> R.drawable.lollipop_ic_menu_info_details
    AppSymbol.Add -> R.drawable.lollipop_ic_menu_add
}

@Composable
internal fun LollipopSymbolIcon(symbol: AppSymbol, description: String?, modifier: Modifier = Modifier) {
    val state = LegacyControlState(checked = symbol == AppSymbol.PinActive)
    val drawable = rememberLollipopDrawable(lollipopSymbolResource(symbol), state)
    if (symbol != AppSymbol.Pin && symbol != AppSymbol.PinActive) drawable.setTint(LocalLollipopContentColor.current.takeOrElse { LocalLollipopColors.current.secondary }.toArgb())
    Box(modifier.size(24.dp).then(if (description != null) Modifier.semantics { contentDescription = description } else Modifier)
        .legacyDrawable(drawable, state, boundsMode = LegacyDrawableBounds.Fit))
}
