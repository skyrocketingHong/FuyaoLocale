package ing.fuyaoskyrocket.applocale.ui.designsystem.material2

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun RoundedIconButton(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    outlined: Boolean,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colors
    IconButton(
        onClick = onClick,
        modifier = modifier.size(48.dp).clip(CircleShape)
            .background(if (outlined) Color.Transparent else colors.onSurface.copy(alpha = if (enabled) .06f else .03f))
            .then(if (outlined) Modifier.border(1.dp, colors.onSurface.copy(alpha = if (enabled) .24f else .12f), CircleShape) else Modifier),
        enabled = enabled,
        content = content,
    )
}
