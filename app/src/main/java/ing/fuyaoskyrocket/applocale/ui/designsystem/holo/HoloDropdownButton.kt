package ing.fuyaoskyrocket.applocale.ui.designsystem.holo

import android.graphics.Rect
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme

/** A labelled ICS action-bar Spinner; its 9-patch includes the dropdown arrow. */
@Composable
internal fun HoloDropdownButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val background = rememberHoloDrawable(HoloAsset.Spinner)
    val interaction = remember { MutableInteractionSource() }
    val state = holoControlState(interaction)
    val density = LocalDensity.current
    val padding = remember(background) { Rect().also { background.getPadding(it) } }
    Box(
        modifier.heightIn(min = 48.dp).holoBackground(background, state)
            .clickable(interaction, indication = null, role = Role.Button, onClick = onClick)
            .padding(
                start = maxOf(8.dp, with(density) { padding.left.toDp() }),
                end = maxOf(24.dp, with(density) { padding.right.toDp() }),
                top = 8.dp, bottom = 8.dp,
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        HoloText(text, style = AppUiTheme.textStyles.body,
            color = LocalHoloTextColors.current.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
