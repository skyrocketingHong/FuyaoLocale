package ing.fuyaoskyrocket.applocale.ui.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

/**
 * Shared component metrics and text roles used by the theme-aware wrappers.
 *
 * The text roles resolve through the neutral [AppUiTheme] roles at composition
 * time; they must not be cached in top-level values because the style can
 * switch while the process is alive. Corner radii are plain constants — the
 * squircle rendering happens in the component wrappers, not here.
 */
object AppComponentDefaults {
    val rowCornerRadius = 16.dp
    val sectionCornerRadius = 28.dp

    @Composable
    fun titleStyle(): TextStyle = AppUiTheme.textStyles.itemTitle

    @Composable
    fun sectionTitleStyle(): TextStyle = AppUiTheme.textStyles.pageTitle

    @Composable
    fun bodyStyle(): TextStyle = AppUiTheme.textStyles.body

    @Composable
    fun metadataStyle(): TextStyle = AppUiTheme.textStyles.metadata

    @Composable
    fun labelStyle(): TextStyle = AppUiTheme.textStyles.label
}
