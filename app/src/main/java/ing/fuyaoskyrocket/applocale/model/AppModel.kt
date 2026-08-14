package ing.fuyaoskyrocket.applocale.model

/**
 * Immutable representation of an installed app for the UI layer.
 * Never holds Drawable or Bitmap — icons are loaded asynchronously by [ing.fuyaoskyrocket.applocale.data.system.AppIconLoader].
 */
data class AppModel(
    val packageName: String,
    val label: String,
    val isSystemApp: Boolean,
    val localeTag: String?,
) {
    val isModified: Boolean
        get() = !localeTag.isNullOrBlank()
}

enum class OperationMode {
    NONE, SHIZUKU, ROOT
}
