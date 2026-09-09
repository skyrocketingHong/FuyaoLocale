package ing.fuyaoskyrocket.applocale.ui.designsystem.material2

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.ui.designsystem.*

private val RoundedLight = lightColors(
    primary = Color(0xff1a73e8), primaryVariant = Color(0xff174ea6), secondary = Color(0xff1a73e8),
    background = Color(0xfffafafa), surface = Color.White, error = Color(0xffb3261e),
    onPrimary = Color.White, onSecondary = Color.White, onBackground = Color(0xff202124), onSurface = Color(0xff202124),
)
private val RoundedDark = darkColors(
    primary = Color(0xff8ab4f8), primaryVariant = Color(0xff669df6), secondary = Color(0xff8ab4f8),
    background = Color(0xff121212), surface = Color(0xff202124), error = Color(0xfff28b82),
    onPrimary = Color(0xff12233d), onSecondary = Color(0xff12233d), onBackground = Color(0xffe8eaed), onSurface = Color(0xffe8eaed),
)

/** Native Material 2: fixed colors and shape theming, independent of Material 3 tonal schemes. */
@Composable
internal fun RoundedMaterialTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    val shapeTokens = themeShapes(ThemeVariant.MATERIAL_ROUNDED)
    MaterialTheme(
        colors = if (darkTheme) RoundedDark else RoundedLight,
        typography = Typography().let { base ->
            base.copy(h6 = base.h6.copy(fontWeight = FontWeight.Medium), subtitle1 = base.subtitle1.copy(fontWeight = FontWeight.Medium))
        },
        shapes = Shapes(
            small = shapeTokens.button.roundedShape(),
            medium = RoundedCornerShape(shapeTokens.card.radius),
            large = RoundedCornerShape(shapeTokens.dialog.radius),
        ),
    ) {
        val colors = MaterialTheme.colors
        val type = MaterialTheme.typography
        val quiet = if (darkTheme) Color(0xff303134) else Color(0xfff1f3f4)
        val palette = AppPalette(
            colors.background, colors.onBackground, colors.surface, colors.onSurface,
            quiet, colors.onSurface, colors.onSurface.copy(alpha = .68f), colors.primary, colors.onPrimary,
            colors.primary.copy(alpha = .12f), colors.onSurface, quiet, colors.onSurface.copy(alpha = .68f),
            if (darkTheme) Color(0xff601410) else Color(0xfffce8e6),
            if (darkTheme) Color(0xfff6aea9) else Color(0xffa50e0e), colors.onSurface.copy(alpha = .12f),
        )
        CompositionLocalProvider(LocalAppUiTheme provides AppUiThemeValues(
            palette, AppTextStyles(type.h6, type.subtitle1, type.body2, type.caption, type.caption),
            ThemeVariant.MATERIAL_ROUNDED,
            AppUiMetrics.Modern.copy(toolbarHeight = 56.dp, tabHeight = 48.dp),
            AppPresentationPolicy(AppControlFamily.Material2, AppNavigationPresentation.AdaptiveBottomOrRail,
                AppPickerPresentation.NativeBottomSheet, false, true, false),
        ), content = content)
    }
}
