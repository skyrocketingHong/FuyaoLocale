package ing.fuyaoskyrocket.applocale.ui.designsystem.holo

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.LegacyDialogSurface
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import kotlin.math.max
import kotlin.math.min

/** One entry of the Holo anchored menu. */
internal data class HoloMenuItem(
    val text: String,
    val selected: Boolean = false,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)

/**
 * Positions the menu below its anchor, clamped to the screen edges: the era
 * ListPopupWindow rule (dropDownVerticalOffset 0), never a negative magic
 * offset for one device.
 */
private object BelowAnchorPositionProvider : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val x = anchorBounds.left.coerceIn(0, max(0, windowSize.width - popupContentSize.width))
        val y = if (anchorBounds.bottom + popupContentSize.height <= windowSize.height) {
            anchorBounds.bottom
        } else {
            max(0, anchorBounds.top - popupContentSize.height)
        }
        return IntOffset(x, y)
    }
}

/**
 * The Holo anchored menu: menu_dropdown_panel_holo_dark 9-patch panel, list
 * rows on the item background selector (48dp minimum height), single-choice
 * rows carrying the era radio indicator; Back and outside taps close the
 * popup without running any action; disabled items never activate.
 */
@Composable
internal fun HoloMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    items: List<HoloMenuItem>,
) {
    if (!expanded) return
    // The focusable popup owns keyboard focus while open; the system back
    // gesture still routes here through the activity's dispatcher.
    BackHandler(enabled = true, onBack = onDismiss)
    Popup(
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true),
        popupPositionProvider = BelowAnchorPositionProvider,
    ) {
        val panel = rememberHoloDrawable(HoloAsset.MenuPanel)
        val textColors = LocalHoloTextColors.current
        val maxHeight = (LocalConfiguration.current.screenHeightDp.dp - 48.dp).coerceAtLeast(48.dp)
        LegacyDialogSurface(frame = panel, color = holoDialogSurfaceColor(),
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .widthIn(min = 196.dp, max = 320.dp)
                .heightIn(max = maxHeight)
        ) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(vertical = 8.dp),
        ) {
            items.forEach { item ->
                val selector = rememberHoloDrawable(HoloAsset.ItemBackground)
                val interaction = remember { MutableInteractionSource() }
                val controlState = holoControlState(interaction, enabled = item.enabled)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .holoBackground(selector, controlState)
                        .selectable(
                            selected = item.selected,
                            interactionSource = interaction,
                            indication = null,
                            enabled = item.enabled,
                            role = Role.Button,
                            onClick = item.onClick,
                        )
                        .padding(start = 12.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HoloText(
                        text = item.text,
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = LocalHoloFontFamily.current,
                            fontSize = LocalHoloMetrics.current.menuTextSize,
                        ),
                        color = if (item.enabled) {
                            textColors.primary
                        } else {
                            textColors.primaryDisabled
                        },
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                        if (item.selected) {
                            val radio = rememberHoloDrawable(HoloAsset.RadioButton)
                            Box(Modifier.size(32.dp).holoDrawable(
                                radio, HoloControlState(enabled = item.enabled, checked = true),
                                boundsMode = HoloDrawableBounds.Intrinsic,
                            ))
                        }
                    }
                }
            }
        }
        }
    }
}
