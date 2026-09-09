package ing.fuyaoskyrocket.applocale.ui.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ing.fuyaoskyrocket.applocale.data.preferences.AppUserPreferences
import androidx.compose.runtime.staticCompositionLocalOf
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloAppTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.kitkat.KitKatAppTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.material.MaterialAppTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.miuix.MiuixAppTheme

/**
 * Non-null while the Liquid Glass navigation bar preference is enabled and supported:
 * [LiquidGlassScaffold] captures the page content into this backdrop layer and the
 * floating overlay tab bar refracts it. Top-level screens must skip their own in-slot
 * bottom navigation bar while this is non-null. Components rendered in their own
 * window (dialogs, sheets, dropdown menus) must clear it so they fall back to their
 * normal theme rendering.
 */
val LocalGlassBackdrop = staticCompositionLocalOf<top.yukonga.miuix.kmp.blur.LayerBackdrop?> { null }

/**
 * App theme with selectable interface styles. Each style installs only its native
 * backend — miuix/ renders the MIUIX tree through MiuixTheme alone, material/
 * installs the Material 3 theme — and every backend also provides the neutral
 * [LocalAppUiTheme] roles. The MIUIX runtime path never installs a MaterialTheme.
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = AppThemePreferences.colorMode.isDark(isSystemInDarkTheme()),
    style: AppThemeStyle = AppThemePreferences.style,
    content: @Composable () -> Unit,
) {
    AppSystemBars(style = style, darkTheme = darkTheme)

    AppThemeProvider(darkTheme, style, content)
}

/** Provider-only entry used inside the appearance transaction's stable content slot. */
@Composable
internal fun AppThemeProvider(
    darkTheme: Boolean,
    style: AppThemeStyle,
    content: @Composable () -> Unit,
) {
    val preferences by AppUserPreferences.state.collectAsStateWithLifecycle()
    CompositionLocalProvider(LocalUserPreferences provides preferences) {
    when (style) {
        AppThemeStyle.MATERIAL_LOLLIPOP -> ing.fuyaoskyrocket.applocale.ui.designsystem.lollipop.LollipopAppTheme(darkTheme, content)
        AppThemeStyle.HOLO_HONEYCOMB -> ing.fuyaoskyrocket.applocale.ui.designsystem.honeycomb.HoneycombAppTheme(darkTheme, content)
        AppThemeStyle.ECLAIR -> ing.fuyaoskyrocket.applocale.ui.designsystem.eclair.EclairAppTheme(darkTheme, content = content)
        AppThemeStyle.FROYO -> ing.fuyaoskyrocket.applocale.ui.designsystem.eclair.EclairAppTheme(darkTheme, era = style, content = content)
        AppThemeStyle.GINGERBREAD -> ing.fuyaoskyrocket.applocale.ui.designsystem.eclair.EclairAppTheme(darkTheme, era = style, content = content)
        AppThemeStyle.MATERIAL_ROUNDED -> ing.fuyaoskyrocket.applocale.ui.designsystem.material2.RoundedMaterialTheme(darkTheme, content)
        AppThemeStyle.MIUIX -> MiuixAppTheme(darkTheme = darkTheme, content = content)
        AppThemeStyle.HOLO_ICS -> HoloAppTheme(darkTheme = darkTheme, content = content)
        AppThemeStyle.HOLO_KITKAT -> KitKatAppTheme(darkTheme = darkTheme, content = content)
        AppThemeStyle.MATERIAL_YOU, AppThemeStyle.MATERIAL3_EXPRESSIVE ->
            MaterialAppTheme(darkTheme = darkTheme, expressive = style == AppThemeStyle.MATERIAL3_EXPRESSIVE, content = content)
    }
    }
}
