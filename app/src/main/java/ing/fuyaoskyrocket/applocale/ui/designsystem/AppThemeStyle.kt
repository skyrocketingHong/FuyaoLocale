package ing.fuyaoskyrocket.applocale.ui.designsystem

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Stable variant IDs. Names remain compatible with existing app_theme/style values. */
enum class AppThemeStyle {
    /** Dynamic Material 3 colour derived from the system wallpaper. */
    MATERIAL_YOU,

    /** Material 3 Expressive: expressive motion, emphasized type and morphing controls. */
    MATERIAL3_EXPRESSIVE,

    /** Native miuix (HyperOS) components and colour system. */
    MIUIX,

    /**
     * Strict-era Holo ICS reproduction backed by the fixed AOSP
     * android-4.0.4_r2.1 assets (see docs/holo-ics/asset-manifest.json).
     * Original Light/Dark assets; glass/blur effects are disabled while active but the saved
     * modern preferences are never rewritten.
     */
    HOLO_ICS,

    /** Original Android 4.4 Holo assets, typography and window backgrounds. */
    HOLO_KITKAT,

    /** Original Android 2.0 classic widgets, Droid Sans and TabWidget. */
    ECLAIR,

    /** Froyo retains the classic widgets and updates the original progress strip. */
    FROYO,

    /** Android 2.3 baseline for the Classic Android family. */
    GINGERBREAD,

    /** Rounded Material 2, with a native Material 2 backend. */
    MATERIAL_ROUNDED,

    /** Original API 11 SDK Holo assets and tablet metrics. */
    HOLO_HONEYCOMB,

    /** Original Android 5.0 Material resources and fixed ripple equations. */
    MATERIAL_LOLLIPOP,
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
    private const val KEY_COLOR_MODE = "color_mode"
    private const val KEY_GLASS_NAVIGATION_BAR = "liquid_glass_navigation_bar"
    private const val KEY_MORE_BLUR = "more_blur"
    private const val LAST_VARIANT_PREFIX = "last_variant."
    private var lastVariants: Map<ThemeFamily, ThemeVariant> = emptyMap()

    var style: AppThemeStyle by mutableStateOf(AppThemeStyle.MATERIAL_YOU)
        private set

    var colorMode: AppColorMode by mutableStateOf(AppColorMode.SYSTEM)
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
        style = variantFromStored(stored)
        lastVariants = ThemeFamily.entries.mapNotNull { family ->
            val raw = preferences.getString(LAST_VARIANT_PREFIX + family.name, null)
            AppThemeStyle.entries.firstOrNull { it.name == raw && it.family == family }?.let { family to it }
        }.toMap()
        colorMode = AppColorMode.fromStored(preferences.getString(KEY_COLOR_MODE, null))
        liquidGlassNavigationBar = preferences.getBoolean(KEY_GLASS_NAVIGATION_BAR, false)
        moreBlur = preferences.getBoolean(KEY_MORE_BLUR, false)
    }

    fun setStyle(context: Context, newStyle: AppThemeStyle) {
        lastVariants = rememberFamilyVariants(lastVariants, style, newStyle)
        style = newStyle
        context
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_STYLE, newStyle.name)
            .also { editor -> lastVariants.forEach { (family, variant) ->
                editor.putString(LAST_VARIANT_PREFIX + family.name, variant.name)
            } }
            .apply()
    }

    fun preferredVariant(family: ThemeFamily): ThemeVariant =
        preferredFamilyVariant(family, lastVariants[family], style)

    fun setColorMode(context: Context, mode: AppColorMode) {
        colorMode = mode
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_COLOR_MODE, mode.name).apply()
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
