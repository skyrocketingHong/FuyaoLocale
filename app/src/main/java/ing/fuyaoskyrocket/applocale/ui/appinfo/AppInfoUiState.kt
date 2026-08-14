package ing.fuyaoskyrocket.applocale.ui.appinfo

/**
 * Immutable state for the app detail screen.
 * Locale picker state is held separately in [ing.fuyaoskyrocket.applocale.ui.languagepicker.LocalePickerUiState].
 */
data class AppInfoUiState(
    val packageName: String = "",
    val label: String = "",
    val currentLocaleTag: String? = null,
    val isLoading: Boolean = true,
) {
    val isModified: Boolean
        get() = !currentLocaleTag.isNullOrBlank()
}
