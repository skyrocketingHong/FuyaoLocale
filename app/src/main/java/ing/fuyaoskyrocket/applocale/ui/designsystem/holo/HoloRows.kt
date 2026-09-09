package ing.fuyaoskyrocket.applocale.ui.designsystem.holo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloDrawables
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.rememberHoloDrawable
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.holoBackground
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.holoControlState

/**
 * The Holo preference/settings row: a single-or-two-line row on the theme's
 * list metrics — title 18sp primary, summary 14sp dim, the full-width
 * item_background pressed/focused selector (NOT the checkbox selector), list
 * padding from the active context (8dp lists / 16dp dialogs, chosen by the
 * caller through [horizontalPadding]).
 */
@Composable
internal fun HoloSettingsRow(
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    titleMaxLines: Int = 1,
    summaryMaxLines: Int = 2,
    titleColor: Color = Color.Unspecified,
    summaryColor: Color = Color.Unspecified,
    onClick: (() -> Unit)? = null,
    horizontalPadding: androidx.compose.ui.unit.Dp =
        ing.fuyaoskyrocket.applocale.ui.designsystem.component.appListContentInset(),
    minHeight: androidx.compose.ui.unit.Dp = AppUiTheme.metrics.listPreferredItemHeight,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val textColors = LocalHoloTextColors.current
    val drawable = rememberHoloDrawable(HoloAsset.ItemBackground)
    val interaction = remember { MutableInteractionSource() }
    val controlState = holoControlState(interaction)
    val resolvedTitle = if (titleColor == Color.Unspecified) textColors.primary else titleColor
    val resolvedSummary =
        if (summaryColor == Color.Unspecified) textColors.secondary else summaryColor

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .holoBackground(drawable, controlState)
            .holoListDivider()
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interaction,
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            )
            .padding(start = horizontalPadding, end = horizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            Box(
                modifier = Modifier.size(48.dp, 48.dp),
                contentAlignment = Alignment.Center,
            ) {
                leading()
            }
            Spacer(Modifier.width(12.dp))
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 6.dp),
        ) {
            HoloText(
                text = title,
                style = ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme.textStyles.itemTitle,
                color = resolvedTitle,
                maxLines = titleMaxLines,
                overflow = TextOverflow.Ellipsis,
            )
            if (summary != null) {
                HoloText(
                    text = summary,
                    style = ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme.textStyles.metadata,
                    color = resolvedSummary,
                    maxLines = summaryMaxLines,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            trailing()
        }
    }
}
