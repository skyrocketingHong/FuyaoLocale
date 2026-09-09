package ing.fuyaoskyrocket.applocale.ui.designsystem.eclair

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.*
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemeStyle

@Composable
internal fun EclairDropdownButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    if (LocalEclairMenuContent.current) {
        EclairMenuCommand(text, onClick, modifier)
        return
    }
    val background = if (LocalClassicEra.current == AppThemeStyle.FROYO) R.drawable.froyo_btn_dropdown else R.drawable.eclair_btn_dropdown
    EclairButtonFrame(onClick, modifier, backgroundRes = background) { state ->
        BasicText(text, style = eclairTextStyle(14).copy(color = eclairColor(R.color.eclair_primary_text_light, state)))
    }
}

/** Original Widget.Button uses primary_text_light even in Theme (dark). */
@Composable
internal fun EclairButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    if (LocalEclairMenuContent.current) {
        EclairMenuCommand(text, onClick, modifier, enabled)
        return
    }
    EclairButtonFrame(onClick, modifier, enabled) { state ->
        BasicText(text, style = eclairTextStyle(14).copy(
            color = eclairColor(R.color.eclair_primary_text_light, state), textAlign = TextAlign.Center,
        ))
    }
}

@Composable
internal fun EclairButtonFrame(
    onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
    @androidx.annotation.DrawableRes backgroundRes: Int = R.drawable.eclair_btn_default,
    content: @Composable (LegacyControlState) -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val state = legacyControlState(interaction, enabled)
    val drawable = rememberClassicDrawable(backgroundRes)
    val rect = remember(drawable) { android.graphics.Rect().also { drawable.getPadding(it) } }
    val density = LocalDensity.current
    Box(
        modifier.heightIn(min = 48.dp).widthIn(min = 48.dp)
            .legacyBackground(drawable, state)
            .clickable(interaction, null, enabled, role = Role.Button, onClick = onClick)
            .padding(with(density) { PaddingValues(rect.left.toDp(), rect.top.toDp(), rect.right.toDp(), rect.bottom.toDp()) }),
        contentAlignment = Alignment.Center,
    ) { content(state) }
}

@Composable
internal fun EclairCheckbox(
    checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?, modifier: Modifier = Modifier, enabled: Boolean = true,
) {
    val interaction = remember { MutableInteractionSource() }
    val state = legacyControlState(interaction, enabled, checked)
    val target = if (onCheckedChange == null) modifier else modifier.toggleable(
        checked, interaction, null, enabled, Role.Checkbox, onCheckedChange,
    )
    EclairCompoundGlyph(R.drawable.eclair_btn_check, state, target)
}

@Composable
internal fun EclairRadioButton(
    selected: Boolean, onClick: (() -> Unit)?, modifier: Modifier = Modifier, enabled: Boolean = true,
) {
    val interaction = remember { MutableInteractionSource() }
    val state = legacyControlState(interaction, enabled, checked = selected)
    val target = if (onClick == null) modifier else modifier.selectable(
        selected, interaction, null, enabled, Role.RadioButton, onClick,
    )
    EclairCompoundGlyph(R.drawable.eclair_btn_radio, state, target)
}

@Composable
private fun EclairCompoundGlyph(id: Int, state: LegacyControlState, modifier: Modifier) {
    Box(modifier.size(48.dp).legacyDrawable(
        rememberClassicDrawable(id), state, boundsMode = LegacyDrawableBounds.Intrinsic, alignment = Alignment.Center,
    ))
}
