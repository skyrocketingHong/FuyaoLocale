package ing.fuyaoskyrocket.applocale.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.data.system.AppIconLoader
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppIcon as AppVectorIcon

/**
 * Async app icon loader — caches converted [ImageBitmap].
 * Never calls PackageManager or Drawable.toBitmap() during composition/recomposition.
 */
@Composable
fun AppIcon(
    packageName: String,
    iconLoader: AppIconLoader,
    modifier: Modifier = Modifier,
    placeholderSize: androidx.compose.ui.unit.Dp = 32.dp,
) {
    var icon by remember(packageName) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(packageName) {
        icon = iconLoader.loadIcon(packageName)
    }
    if (icon != null) {
        Image(
            bitmap = icon!!,
            modifier = modifier,
            contentDescription = null,
        )
    } else {
        // No miuix glyph for the generic app-grid metaphor; the project vector
        // is drawn by the backend icon control (asset exception, see 012 record).
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            AppVectorIcon(
                imageVector = Icons.Outlined.Apps,
                contentDescription = null,
                tint = AppUiTheme.palette.muted,
                modifier = Modifier.size(placeholderSize),
            )
        }
    }
}
