package ing.fuyaoskyrocket.applocale.ui.designsystem

import android.graphics.Color as AndroidColor
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

/**
 * App theme: dynamic colour on Android 12+ (which is every device given minSdk = 33),
 * falling back to the static [LightColorScheme] / [DarkColorScheme] for previews and edge cases.
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

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
            // the current Material surface continue through HyperOS' gesture-navigation area.
            activity.enableEdgeToEdge(
                statusBarStyle = transparentSystemBarStyle,
                navigationBarStyle = transparentSystemBarStyle,
            )
            activity.window.isNavigationBarContrastEnforced = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
