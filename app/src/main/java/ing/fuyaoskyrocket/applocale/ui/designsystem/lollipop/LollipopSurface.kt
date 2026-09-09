package ing.fuyaoskyrocket.applocale.ui.designsystem.lollipop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme

/** App-owned paper surface: era widgets and their source assets remain independent. */
@Composable
internal fun LollipopSurface(modifier: Modifier, color: Color, contentColor: Color,
    shape: Shape = RoundedCornerShape(AppUiTheme.shapes.card.radius), elevation: Dp = 0.dp, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLollipopContentColor provides contentColor) {
        Box(modifier.shadow(elevation, shape).clip(shape).background(color), propagateMinConstraints = true) { content() }
    }
}
