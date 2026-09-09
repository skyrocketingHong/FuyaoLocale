package ing.fuyaoskyrocket.applocale.ui.designsystem.lollipop

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.*

@Composable
internal fun LollipopSearchField(value: String, onValueChange: (String) -> Unit, hint: String,
    modifier: Modifier = Modifier, onSearch: () -> Unit = {}) {
    val interaction = remember { MutableInteractionSource() }
    val state = legacyControlState(interaction)
    val colors = LocalLollipopColors.current
    val ink = LocalLollipopContentColor.current.takeOrElse { colors.foreground }
    val supporting = LocalLollipopContentColor.current.takeOrElse { colors.secondary }
    val drawable = rememberLollipopDrawable(if (state.focused) R.drawable.lollipop_textfield_activated_mtrl_alpha else R.drawable.lollipop_textfield_default_mtrl_alpha)
    drawable.setTint((if (state.focused) colors.accent else supporting).toArgb())
    BasicTextField(value, onValueChange, modifier.heightIn(min = 48.dp).semantics { contentDescription = hint },
        textStyle = lollipopTextStyle(18).copy(color = ink), singleLine = true,
        interactionSource = interaction, cursorBrush = SolidColor(colors.accent),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        decorationBox = { field ->
            Box(Modifier.fillMaxWidth().padding(4.dp).legacyBackground(drawable, state).padding(vertical = 8.dp), contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) BasicText(hint, style = lollipopTextStyle(18).copy(color = supporting.copy(alpha = .7f)))
                field()
            }
        })
}
