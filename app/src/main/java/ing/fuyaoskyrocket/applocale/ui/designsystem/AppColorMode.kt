package ing.fuyaoskyrocket.applocale.ui.designsystem

/** The same persisted appearance mode applies to every interface style. */
enum class AppColorMode {
    SYSTEM,
    LIGHT,
    DARK;

    fun isDark(systemDark: Boolean): Boolean = when (this) {
        SYSTEM -> systemDark
        LIGHT -> false
        DARK -> true
    }

    companion object {
        fun fromStored(value: String?): AppColorMode =
            entries.firstOrNull { it.name == value } ?: SYSTEM
    }
}

/** ICS uses black system-bar surfaces in both Holo Light and Holo Dark. */
internal fun usesDarkSystemBarSurface(style: AppThemeStyle, darkTheme: Boolean): Boolean =
    systemBarAppearance(style, darkTheme).darkSurface
