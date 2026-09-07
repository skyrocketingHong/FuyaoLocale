package ing.fuyaoskyrocket.applocale.ui.designsystem

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** The selectable app-wide interface styles. */
enum class AppThemeStyle {
    /** Dynamic Material 3 colour derived from the system wallpaper. */
    MATERIAL_YOU,

    /** Native miuix (HyperOS) components and colour system. */
    MIUIX,
}

/**
 * Process-wide theme selection backed by SharedPreferences.
 * [initialize] must run once from the Application before any screen reads [style].
 *
 * [moreBlur] and [liquidGlassNavigationBar] are independent effects: neither
 * covers the other, and reading them never counts as a user action.
 */
object AppThemePreferences {
    private const val PREFERENCES_NAME = "app_theme"
    private const val KEY_STYLE = "style"
    private const val KEY_GLASS_NAVIGATION_BAR = "liquid_glass_navigation_bar"
    private const val KEY_MORE_BLUR = "more_blur"

    var style: AppThemeStyle by mutableStateOf(AppThemeStyle.MATERIAL_YOU)
        private set

    /** Renders the compact-window bottom tab bar as a Liquid Glass capsule strip. */
    var liquidGlassNavigationBar: Boolean by mutableStateOf(false)
        private set

    /** Blurs content scrolled behind the app bars and the standard navigation bar. */
    var moreBlur: Boolean by mutableStateOf(false)
        private set

    fun initialize(context: Context) {
        val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val stored = preferences.getString(KEY_STYLE, null)
        style = AppThemeStyle.entries.firstOrNull { it.name == stored } ?: AppThemeStyle.MATERIAL_YOU
        liquidGlassNavigationBar = preferences.getBoolean(KEY_GLASS_NAVIGATION_BAR, false)
        moreBlur = preferences.getBoolean(KEY_MORE_BLUR, false)
    }

    fun setStyle(context: Context, newStyle: AppThemeStyle) {
        style = newStyle
        context
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_STYLE, newStyle.name)
            .apply()
    }

    fun setLiquidGlassNavigationBar(context: Context, enabled: Boolean) {
        liquidGlassNavigationBar = enabled
        context
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_GLASS_NAVIGATION_BAR, enabled)
            .apply()
    }

    fun setMoreBlur(context: Context, enabled: Boolean) {
        moreBlur = enabled
        context
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_MORE_BLUR, enabled)
            .apply()
    }
}
