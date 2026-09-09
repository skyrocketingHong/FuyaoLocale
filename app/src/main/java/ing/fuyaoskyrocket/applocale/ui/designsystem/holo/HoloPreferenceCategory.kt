package ing.fuyaoskyrocket.applocale.ui.designsystem.holo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ing.fuyaoskyrocket.applocale.R

/** Widget.Holo.TextView.ListSeparator: 14sp bold, 8dp side inset, era divider. */
@Composable
internal fun HoloPreferenceCategory(title: String, modifier: Modifier = Modifier) {
    val background = rememberHoloDrawable(HoloAsset.CategoryDivider)
    val locale = LocalConfiguration.current.locales[0]
    val density = LocalDensity.current
    val padding = remember(background) { android.graphics.Rect().also { background.getPadding(it) } }
    Box(
        modifier.fillMaxWidth().heightIn(min = with(density) { background.intrinsicHeight.coerceAtLeast(0).toDp() })
            .holoBackground(background).padding(
                start = ing.fuyaoskyrocket.applocale.ui.designsystem.component.appListContentInset(),
                end = ing.fuyaoskyrocket.applocale.ui.designsystem.component.appListContentInset(),
                top = with(density) { padding.top.toDp() },
                bottom = with(density) { padding.bottom.toDp() },
            )
            .semantics { heading() },
        contentAlignment = Alignment.CenterStart,
    ) {
        HoloText(
            text = if (LocalHoloMetrics.current.categoryUppercase) title.uppercase(locale) else title,
            style = TextStyle(fontFamily = LocalHoloFontFamily.current, fontSize = 14.sp),
            fontWeight = FontWeight.Bold,
            color = LocalHoloTextColors.current.secondary,
        )
    }
}
