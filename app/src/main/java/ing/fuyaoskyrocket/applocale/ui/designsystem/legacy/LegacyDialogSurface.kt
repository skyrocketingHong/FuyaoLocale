package ing.fuyaoskyrocket.applocale.ui.designsystem.legacy

import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

/** Android View backgrounds apply these insets automatically; Compose draw modifiers do not. */
@Composable
internal fun rememberLegacyContentPadding(drawable: Drawable): PaddingValues {
    val density = LocalDensity.current
    val direction = LocalLayoutDirection.current
    val padding = remember(drawable, direction) {
        drawable.setLayoutDirection(if (direction == LayoutDirection.Rtl) android.util.LayoutDirection.RTL else android.util.LayoutDirection.LTR)
        Rect().also { drawable.getPadding(it) }
    }
    return with(density) { PaddingValues.Absolute(padding.left.toDp(), padding.top.toDp(), padding.right.toDp(), padding.bottom.toDp()) }
}

/** Preserve the versioned frame/shadow, then bound and paint its content area independently. */
@Composable
internal fun LegacyDialogSurface(
    frame: Drawable,
    color: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier.legacyBackground(frame).padding(rememberLegacyContentPadding(frame))) {
        Column(Modifier.fillMaxWidth().clipToBounds().background(color.copy(alpha = 1f)), content = content)
    }
}
