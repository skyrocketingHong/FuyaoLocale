package ing.fuyaoskyrocket.applocale.ui.designsystem

/**
 * The complete appearance request: theme engine plus the two independent
 * background effects. Fields merge per-field; no snapshot of this type ever
 * overrides a newer individual preference.
 */
data class AppAppearanceState(
    val style: AppThemeStyle,
    val moreBlur: Boolean,
    val liquidGlassNavigationBar: Boolean,
    val colorMode: AppColorMode = AppColorMode.SYSTEM,
) {
    companion object {
        fun fromPreferences(): AppAppearanceState = AppAppearanceState(
            style = AppThemePreferences.style,
            moreBlur = AppThemePreferences.moreBlur,
            liquidGlassNavigationBar = AppThemePreferences.liquidGlassNavigationBar,
            colorMode = AppThemePreferences.colorMode,
        )
    }
}
