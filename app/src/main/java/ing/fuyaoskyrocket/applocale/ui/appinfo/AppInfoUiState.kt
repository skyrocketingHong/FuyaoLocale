package ing.fuyaoskyrocket.applocale.ui.appinfo

/**
 * Immutable state for the app detail screen.
 * Locale picker state is held separately in [ing.fuyaoskyrocket.applocale.ui.languagepicker.LocalePickerUiState].
 *
 * The detail page is a single direct language flow (round-5 019-A): the old
 * app-info tab, candidate-count and scope-tab presentation state is gone.
 */
data class AppInfoUiState(
    val packageName: String = "",
    val label: String = "",
    val currentLocaleTag: String? = null,
    val isSystemApp: Boolean = false,
    val isLoading: Boolean = true,
) {
    val isModified: Boolean
        get() = !currentLocaleTag.isNullOrBlank()
}
