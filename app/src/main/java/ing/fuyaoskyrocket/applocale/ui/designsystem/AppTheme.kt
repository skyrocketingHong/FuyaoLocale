package ing.fuyaoskyrocket.applocale.ui.designsystem

import android.graphics.Color as AndroidColor
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
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
    darkTheme: Boolean = isSystemInDarkTheme(),
    style: AppThemeStyle = AppThemePreferences.style,
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as ComponentActivity
            val transparentSystemBarStyle = if (darkTheme) {
                SystemBarStyle.dark(AndroidColor.TRANSPARENT)
            } else {
                SystemBarStyle.light(
                    scrim = AndroidColor.TRANSPARENT,
                    darkScrim = AndroidColor.TRANSPARENT,
                )
            }
            // Re-apply the icon appearance when the theme changes. The transparent style lets
            // the active backend's surface continue through HyperOS' gesture-navigation area.
            activity.enableEdgeToEdge(
                statusBarStyle = transparentSystemBarStyle,
                navigationBarStyle = transparentSystemBarStyle,
            )
            activity.window.isNavigationBarContrastEnforced = false
        }
    }

    if (style == AppThemeStyle.MIUIX) {
        MiuixAppTheme(darkTheme = darkTheme, content = content)
    } else {
        MaterialAppTheme(darkTheme = darkTheme, content = content)
    }
}
