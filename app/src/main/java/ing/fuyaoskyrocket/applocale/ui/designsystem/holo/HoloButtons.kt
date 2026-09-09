package ing.fuyaoskyrocket.applocale.ui.designsystem.holo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity

/**
 * The Widget.Holo.Button: btn_default_holo_dark 9-patch states at min
 * 48×64dp, single-line 18sp primary text (Widget.Holo.Button →
 * textAppearanceMedium + primary_text_holo_dark). The 9-patch supplies the
 * optical inner padding — the content consumes that padding exactly once via
 * [contentPaddingFrom] and never adds its own horizontal inset on top.
 */
@Composable
internal fun HoloButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val metrics = LocalHoloMetrics.current
    val textColors = LocalHoloTextColors.current
    val drawable = rememberHoloDrawable(HoloAsset.Button)
    val controlState = holoControlState(interactionSource, enabled = enabled)
    val density = LocalDensity.current
    val horizontal = drawable.intrinsicPaddingHorizontal(density)
    val vertical = drawable.intrinsicPaddingVertical(density)

    Box(
        modifier = modifier
            .heightIn(min = metrics.buttonMinHeight)
            .widthIn(min = metrics.buttonMinWidth)
            .holoBackground(drawable, controlState)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(
                start = horizontal,
                end = horizontal,
                top = vertical,
                bottom = vertical,
            ),
        contentAlignment = Alignment.Center,
    ) {
        HoloText(
            text = text,
            style = androidx.compose.ui.text.TextStyle(
                fontFamily = LocalHoloFontFamily.current,
                fontSize = androidx.compose.ui.unit.TextUnit(
                    18f,
                    androidx.compose.ui.unit.TextUnitType.Sp,
                ),
            ),
            color = if (enabled) textColors.primary else textColors.primaryDisabled,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Widget.Holo.Button.Borderless (and .Small): transparent background with the
 * item_background_holo_dark pressed/focused states, 4dp horizontal padding,
 * 14sp primary text in the Small dialog-bar size.
 */
@Composable
internal fun HoloBorderlessButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    small: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val textColors = LocalHoloTextColors.current
    val drawable = rememberHoloDrawable(HoloAsset.ItemBackground)
    val controlState = holoControlState(interactionSource, enabled = enabled)
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .holoBackground(drawable, controlState)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 4.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        HoloText(
            text = text,
            style = if (small) {
                androidx.compose.ui.text.TextStyle(
                    fontFamily = LocalHoloFontFamily.current,
                    fontSize = LocalHoloMetrics.current.borderlessSmallTextSize,
                )
            } else {
                androidx.compose.ui.text.TextStyle(
                    fontFamily = LocalHoloFontFamily.current,
                    fontSize = androidx.compose.ui.unit.TextUnit(
                        18f,
                        androidx.compose.ui.unit.TextUnitType.Sp,
                    ),
                )
            },
            color = if (enabled) textColors.primary else textColors.primaryDisabled,
            maxLines = 2,
            textAlign = TextAlign.Center,
        )
    }
}

/** Reads the 9-patch optical padding in dp (content insets consume it once). */
internal fun android.graphics.drawable.Drawable.intrinsicPaddingHorizontal(
    density: androidx.compose.ui.unit.Density,
): androidx.compose.ui.unit.Dp {
    val rect = android.graphics.Rect()
    getPadding(rect)
    return with(density) { ((rect.left + rect.right) / 2f).toDp() }
}

internal fun android.graphics.drawable.Drawable.intrinsicPaddingVertical(
    density: androidx.compose.ui.unit.Density,
): androidx.compose.ui.unit.Dp {
    val rect = android.graphics.Rect()
    getPadding(rect)
    return with(density) { ((rect.top + rect.bottom) / 2f).toDp() }
}
