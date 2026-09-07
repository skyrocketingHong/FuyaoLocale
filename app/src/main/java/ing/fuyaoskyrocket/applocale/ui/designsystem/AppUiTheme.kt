package ing.fuyaoskyrocket.applocale.ui.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

/**
 * Control-library-neutral colour roles. Every field pairs a container with its
 * matching foreground (for example [error] with [onError]); callers must keep
 * those pairs instead of falling back to [foreground] everywhere. The values are
 * derived per style by the material/ and miuix/ theme backends — never cached
 * as top-level statics.
 */
data class AppPalette(
    val background: Color,
    val foreground: Color,
    val surface: Color,
    val surfaceContent: Color,
    val secondarySurface: Color,
    val secondaryContent: Color,
    val muted: Color,
    val accent: Color,
    val onAccent: Color,
    val selected: Color,
    val onSelected: Color,
    /** Quiet container for non-interactive info badges and labels (020-A). */
    val quietContainer: Color,
    val quietContent: Color,
    val error: Color,
    val onError: Color,
    val divider: Color,
)

/**
 * Control-library-neutral text roles resolved from the active theme's native
 * type scale (Material typography or Miuix text styles).
 */
data class AppTextStyles(
    val pageTitle: TextStyle,
    val itemTitle: TextStyle,
    val body: TextStyle,
    val metadata: TextStyle,
    val label: TextStyle,
)

/** The values installed by the active theme backend; treat as read-only. */
class AppUiThemeValues internal constructor(
    val palette: AppPalette,
    val textStyles: AppTextStyles,
)

/**
 * Installed by [AppTheme] through the material/ and miuix/ backends. Reading it
 * outside a installed theme fails fast instead of silently rendering with
 * library-default colours.
 */
val LocalAppUiTheme = staticCompositionLocalOf<AppUiThemeValues> {
    error("AppUiTheme is not installed; wrap content in AppTheme first.")
}

/** Read-only access to the neutral palette and text roles of the current theme. */
object AppUiTheme {
    val palette: AppPalette
        @Composable get() = LocalAppUiTheme.current.palette

    val textStyles: AppTextStyles
        @Composable get() = LocalAppUiTheme.current.textStyles
}
