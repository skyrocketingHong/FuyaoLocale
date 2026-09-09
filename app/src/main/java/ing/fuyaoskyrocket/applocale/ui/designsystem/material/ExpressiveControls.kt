package ing.fuyaoskyrocket.applocale.ui.designsystem.material

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

internal enum class ExpressiveButtonKind { Filled, Tonal, Outlined, Text }

/** Uses the native pressed-shape interpolation rather than a static rounded M3 button. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ExpressiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    kind: ExpressiveButtonKind,
) {
    val shapes = ButtonDefaults.shapesFor(48.dp)
    val padding = ButtonDefaults.contentPaddingFor(48.dp)
    val buttonModifier = modifier.heightIn(min = 48.dp)
    when (kind) {
        ExpressiveButtonKind.Filled -> Button(onClick, shapes, buttonModifier, enabled, contentPadding = padding) { Text(text) }
        ExpressiveButtonKind.Tonal -> FilledTonalButton(onClick, shapes, buttonModifier, enabled, contentPadding = padding) { Text(text) }
        ExpressiveButtonKind.Outlined -> OutlinedButton(onClick, shapes, buttonModifier, enabled, contentPadding = padding) { Text(text) }
        ExpressiveButtonKind.Text -> TextButton(onClick, shapes, buttonModifier, enabled, contentPadding = padding) { Text(text) }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ExpressiveIconButton(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    content: @Composable () -> Unit,
) {
    IconButton(onClick, shapes = IconButtonDefaults.shapes(), modifier = modifier, enabled = enabled, content = content)
}
