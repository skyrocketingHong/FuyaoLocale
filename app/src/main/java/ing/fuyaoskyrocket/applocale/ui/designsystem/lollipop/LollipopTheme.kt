package ing.fuyaoskyrocket.applocale.ui.designsystem.lollipop

import android.content.Context
import android.view.ContextThemeWrapper
import androidx.annotation.DrawableRes
import androidx.compose.foundation.LocalIndication
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.*
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.*

internal val LollipopFont = FontFamily(
    Font(R.font.lollipop_roboto_regular), Font(R.font.lollipop_roboto_medium, FontWeight.Medium),
    Font(R.font.lollipop_roboto_bold, FontWeight.Bold),
)
@Composable
internal fun lollipopTextStyle(size: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
    fontFamily = legacyFontFamily(LollipopFont), fontSize = size.sp, fontWeight = weight,
    fontSynthesis = androidx.compose.ui.text.font.FontSynthesis.Weight,
    platformStyle = PlatformTextStyle(includeFontPadding = true),
)
internal data class LollipopColors(
    val dark: Boolean, val primary: Color, val primaryDark: Color, val foreground: Color,
    val secondary: Color, val accent: Color, val ripple: Color, val floating: Color,
)
internal val LocalLollipopContentColor = staticCompositionLocalOf { Color.Unspecified }
internal val LocalLollipopColors = staticCompositionLocalOf<LollipopColors> { error("Lollipop theme missing") }
internal val LocalLollipopDrawableContext = staticCompositionLocalOf<Context> { error("Lollipop drawable context missing") }

/** Only resource inflation uses this context; business callbacks retain their activity context. */
@Composable
internal fun rememberLollipopDrawable(@DrawableRes id: Int, initialState: LegacyControlState = LegacyControlState()) =
    LocalLollipopDrawableContext.current.let { context -> remember(id, context) {
        legacyDrawable(context, id).apply { state = initialState.toStateSet(); jumpToCurrentState() }
    } }

@Composable
internal fun LollipopAppTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val drawableContext = remember(context, darkTheme) {
        ContextThemeWrapper(context, if (darkTheme) R.style.LollipopAppDrawablesDark else R.style.LollipopAppDrawablesLight)
    }
    val background = colorResource(if (darkTheme) R.color.lollipop_background_material_dark else R.color.lollipop_background_material_light)
    val foreground = colorResource(if (darkTheme) R.color.lollipop_primary_text_material_dark else R.color.lollipop_primary_text_material_light)
    val secondary = colorResource(if (darkTheme) R.color.lollipop_secondary_text_material_dark else R.color.lollipop_secondary_text_material_light)
    val accent = colorResource(if (darkTheme) R.color.lollipop_accent_material_dark else R.color.lollipop_accent_material_light)
    val ripple = colorResource(if (darkTheme) R.color.lollipop_ripple_material_dark else R.color.lollipop_ripple_material_light)
    val colors = LollipopColors(darkTheme,
        colorResource(R.color.material_classic_primary),
        colorResource(R.color.material_classic_primary_dark),
        foreground, secondary, accent, ripple,
        colorResource(if (darkTheme) R.color.lollipop_background_floating_material_dark else R.color.lollipop_background_floating_material_light))
    val palette = AppPalette(background, foreground, colors.floating, foreground, colors.floating, foreground,
        secondary, accent, if (darkTheme) Color.Black else Color.White, accent.copy(alpha = 0.15f), foreground,
        Color.Transparent, secondary, if (darkTheme) Color(0xffef9a9a) else Color(0xffb71c1c),
        if (darkTheme) Color.Black else Color.White, foreground.copy(alpha = 0.12f))
    val textStyles = AppTextStyles(lollipopTextStyle(20, FontWeight.Medium), lollipopTextStyle(16),
        lollipopTextStyle(14), lollipopTextStyle(14), lollipopTextStyle(14, FontWeight.Medium))
    CompositionLocalProvider(
        LocalLollipopColors provides colors, LocalLollipopDrawableContext provides drawableContext,
        LocalIndication provides remember(ripple) { LollipopRipple(ripple) },
        LocalAppUiTheme provides AppUiThemeValues(palette, textStyles, AppThemeStyle.MATERIAL_LOLLIPOP,
            AppUiMetrics(48.dp, 64.dp, 80.dp, 16.dp, 16.dp,
                toolbarHeight = androidx.compose.ui.res.dimensionResource(R.dimen.lollipop_action_bar_default_height_material)),
            AppPresentationPolicy(AppControlFamily.Lollipop, AppNavigationPresentation.ToolbarTabs,
                AppPickerPresentation.Material2014Dialog, false, false, true, continuousLists = true)),
        content = content,
    )
}
