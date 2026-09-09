package ing.fuyaoskyrocket.applocale.ui.designsystem.eclair

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.*

@Composable
internal fun EclairSearchField(value: String, onValueChange: (String) -> Unit, hint: String, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val state = legacyControlState(interaction)
    val drawable = rememberClassicDrawable(R.drawable.eclair_edit_text)
    val rect = remember(drawable) { android.graphics.Rect().also { drawable.getPadding(it) } }
    val density = LocalDensity.current
    BasicTextField(
        value, onValueChange,
        modifier.heightIn(min = 48.dp).semantics { contentDescription = hint },
        textStyle = eclairTextStyle(18).copy(color = eclairColor(R.color.eclair_primary_text_light, state)),
        singleLine = true, interactionSource = interaction, cursorBrush = SolidColor(Color.Black),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        decorationBox = { field ->
            Box(Modifier.fillMaxWidth().legacyBackground(drawable, state)
                .padding(with(density) { PaddingValues(rect.left.toDp(), rect.top.toDp(), rect.right.toDp(), rect.bottom.toDp()) }),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.isEmpty()) BasicText(hint, style = eclairTextStyle(18).copy(color = Color(0xff808080)))
                field()
            }
        },
    )
}
