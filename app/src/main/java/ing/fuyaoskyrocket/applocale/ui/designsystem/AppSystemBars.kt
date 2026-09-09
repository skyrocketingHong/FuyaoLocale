package ing.fuyaoskyrocket.applocale.ui.designsystem

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView

/** Background and icon contrast are resolved together from the applied theme. */
internal data class AppSystemBarAppearance(val background: Int, val darkSurface: Boolean)

internal fun systemBarAppearance(style: AppThemeStyle, darkTheme: Boolean): AppSystemBarAppearance =
    when (style) {
        AppThemeStyle.MATERIAL_LOLLIPOP -> AppSystemBarAppearance(0xff303f9f.toInt(), true)
        AppThemeStyle.HOLO_ICS,
        AppThemeStyle.HOLO_KITKAT,
        AppThemeStyle.HOLO_HONEYCOMB,
        AppThemeStyle.ECLAIR,
        AppThemeStyle.FROYO,
        AppThemeStyle.GINGERBREAD,
        -> AppSystemBarAppearance(Color.BLACK, true)
        AppThemeStyle.MATERIAL_YOU,
        AppThemeStyle.MATERIAL3_EXPRESSIVE,
        AppThemeStyle.MATERIAL_ROUNDED,
        AppThemeStyle.MIUIX,
        -> AppSystemBarAppearance(Color.TRANSPARENT, darkTheme)
    }

/** One owner for bar contrast, driven by the appearance actually on screen. */
@Composable
internal fun AppSystemBars(style: AppThemeStyle, darkTheme: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    LaunchedEffect(view, style, darkTheme) {
        val activity = view.context.findComponentActivity() ?: return@LaunchedEffect
        val appearance = systemBarAppearance(style, darkTheme)
        val bars = if (appearance.darkSurface) {
            SystemBarStyle.dark(appearance.background)
        } else {
            SystemBarStyle.light(appearance.background, appearance.background)
        }
        activity.enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
        activity.window.isNavigationBarContrastEnforced = false
    }
}

private tailrec fun Context.findComponentActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> if (baseContext !== this) baseContext.findComponentActivity() else null
    else -> null
}
