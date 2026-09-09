package ing.fuyaoskyrocket.applocale.ui.designsystem.eclair

import androidx.annotation.ColorRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppPalette
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppTextStyles
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiMetrics
import ing.fuyaoskyrocket.applocale.ui.designsystem.*
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.LegacyControlState
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.resolve

internal val EclairFont = FontFamily(
    Font(R.font.eclair_droid_sans),
    Font(R.font.eclair_droid_sans_bold, FontWeight.Bold),
)
internal val GingerbreadFont = FontFamily(
    Font(R.font.gingerbread_droid_sans), Font(R.font.gingerbread_droid_sans_bold, FontWeight.Bold),
)
internal val LocalClassicFont = staticCompositionLocalOf { EclairFont }
internal data class EclairColors(val dark: Boolean, val primary: Color, val secondary: Color)
internal val LocalEclairColors = staticCompositionLocalOf<EclairColors> { error("Eclair theme missing") }
internal val LocalClassicEra = staticCompositionLocalOf { AppThemeStyle.ECLAIR }

/** Selector order remains owned by the versioned framework resource. */
@Composable
internal fun eclairColor(@ColorRes id: Int, state: LegacyControlState = LegacyControlState()): Color {
    val context = LocalContext.current
    val resource = classicResource(id)
    val list = remember(resource, context) { requireNotNull(ContextCompat.getColorStateList(context, resource)) }
    return Color(state.resolve(list))
}

@Composable
internal fun eclairTextStyle(size: Int, bold: Boolean = false) = TextStyle(
    fontFamily = ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.legacyFontFamily(LocalClassicFont.current), fontSize = size.sp,
    fontSynthesis = androidx.compose.ui.text.font.FontSynthesis.Weight,
    fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
    platformStyle = PlatformTextStyle(includeFontPadding = true),
)

// Theme.listPreferredItemHeight and layout/preference.xml on android-2.0_r1.
internal val EclairMetrics = AppUiMetrics(64.dp, 64.dp, 64.dp, 15.dp, 14.dp, toolbarHeight = 48.dp, tabHeight = 64.dp)
internal val EclairTextStyles: AppTextStyles
    @Composable get() = AppTextStyles(
    pageTitle = eclairTextStyle(14, true), itemTitle = eclairTextStyle(22),
    body = eclairTextStyle(18), metadata = eclairTextStyle(14), label = eclairTextStyle(14),
)

@Composable
internal fun EclairWidgetTheme(darkTheme: Boolean, content: @Composable (AppPalette) -> Unit) {
    val foreground = eclairColor(if (darkTheme) R.color.eclair_primary_text_dark else R.color.eclair_primary_text_light)
    val secondary = eclairColor(if (darkTheme) R.color.eclair_secondary_text_dark else R.color.eclair_secondary_text_light)
    val background = colorResource(classicResource(if (darkTheme) R.color.eclair_background_dark else R.color.eclair_background_light))
    val palette = AppPalette(
        background = background, foreground = foreground,
        surface = background, surfaceContent = foreground,
        secondarySurface = background, secondaryContent = foreground, muted = secondary,
        // Classic controls carry their own orange selector pixels; no accent tint is applied.
        accent = foreground, onAccent = background, selected = background, onSelected = foreground,
        quietContainer = background, quietContent = secondary,
        error = if (darkTheme) Color(0xffff8080) else Color(0xffb00000), onError = background,
        divider = if (darkTheme) Color(0xff444444) else Color(0xffcccccc),
    )
    CompositionLocalProvider(LocalEclairColors provides EclairColors(darkTheme, foreground, secondary)) {
        content(palette)
    }
}

internal val EclairPresentationPolicy = AppPresentationPolicy(
    controls = AppControlFamily.Eclair, navigation = AppNavigationPresentation.ClassicTabs,
    picker = AppPickerPresentation.ClassicDialog, supportsBackdropEffects = false,
    showsModernEffectSettings = false, usesContextualActions = false, continuousLists = true,
    toggleUsesCheckbox = true,
)

@Composable
internal fun EclairAppTheme(
    darkTheme: Boolean,
    dialog: Boolean = false,
    era: AppThemeStyle = LocalClassicEra.current,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalClassicEra provides era,
        LocalClassicFont provides if (era == AppThemeStyle.GINGERBREAD) GingerbreadFont else EclairFont,
    ) {
    EclairWidgetTheme(darkTheme) { palette ->
        CompositionLocalProvider(LocalAppUiTheme provides AppUiThemeValues(
            palette, EclairTextStyles, era,
            if (dialog) EclairMetrics.copy(listPreferredItemPaddingHorizontal = 14.dp) else EclairMetrics,
            EclairPresentationPolicy,
        ), content = content)
    }
    }
}
