package ing.fuyaoskyrocket.applocale.ui.designsystem.holo

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ing.fuyaoskyrocket.applocale.R

/**
 * The holo single-line text field: the imported edit_text_holo_dark 9-patch
 * (underline states) behind a BasicTextField. Editing state
 * (selection/composition) is held as [TextFieldValue] here and only synced to
 * the external String value — the Chinese IME never gets its composition
 * rebuilt mid-typing. The 9-patch supplies the optical padding once.
 */
@Composable
internal fun HoloTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    hintText: String? = null,
    fieldDescription: String? = null,
    imeAction: ImeAction = ImeAction.Search,
    onImeAction: (() -> Unit)? = null,
    backgroundRes: HoloAsset = HoloAsset.EditText,
) {
    val interaction = remember { MutableInteractionSource() }
    val drawable = rememberHoloDrawable(backgroundRes)
    val textColors = LocalHoloTextColors.current
    var focused by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf(TextFieldValue(value)) }
    if (editing.text != value) {
        // External change (clear button, restore): adopt the new text and
        // reset the caret — our own edits echo back with text already equal.
        editing = TextFieldValue(value)
    }
    val controlState = holoControlState(interaction, enabled = enabled, activated = focused)
    val density = LocalDensity.current
    val horizontal = drawable.intrinsicPaddingHorizontal(density)
    val vertical = drawable.intrinsicPaddingVertical(density)

    BasicTextField(
        value = editing,
        onValueChange = { next ->
            editing = next
            if (next.text != value) onValueChange(next.text)
        },
        modifier = modifier
            .fillMaxWidth()
            .holoBackground(drawable, controlState)
            .onFocusChanged { focused = it.isFocused }
            .then(
                if (fieldDescription != null) {
                    Modifier.semantics { contentDescription = fieldDescription }
                } else {
                    Modifier
                },
            )
            .padding(start = horizontal, end = horizontal, top = vertical, bottom = vertical),
        enabled = enabled,
        singleLine = true,
        textStyle = TextStyle(
            fontFamily = LocalHoloFontFamily.current,
            fontSize = LocalHoloMetrics.current.inputTextSize,
            color = if (enabled) textColors.primary else textColors.primaryDisabled,
        ),
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onSearch = { onImeAction?.invoke() },
            onDone = { onImeAction?.invoke() },
            onGo = { onImeAction?.invoke() },
            onSend = { onImeAction?.invoke() },
        ),
        cursorBrush = SolidColor(if (LocalHoloMetrics.current.inputUsesPrimaryCursor) textColors.primary else LocalHoloControlColors.current.activatedHighlight),
        interactionSource = interaction,
        decorationBox = { inner ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (editing.text.isEmpty() && hintText != null) {
                    androidx.compose.foundation.text.BasicText(
                        text = hintText,
                        style = TextStyle(
                            fontFamily = LocalHoloFontFamily.current,
                            fontSize = LocalHoloMetrics.current.inputTextSize,
                            color = textColors.hint,
                        ),
                        maxLines = 1,
                    )
                }
                inner()
            }
        },
    )
}

/**
 * The SearchView-style field: the search text field 9-patches (focused →
 * selected variant), blue underline glow, same single-line editing rules.
 */
@Composable
internal fun HoloSearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    fieldDescription: String = hint,
    onSearch: () -> Unit = {},
) {
    HoloTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        hintText = hint,
        fieldDescription = fieldDescription,
        imeAction = ImeAction.Search,
        onImeAction = onSearch,
        backgroundRes = HoloAsset.SearchField,
    )
}
