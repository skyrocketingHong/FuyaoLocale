package ing.fuyaoskyrocket.applocale.model

/**
 * A reusable snapshot of the non-default per-app locales chosen by the user.
 *
 * Only modified apps are captured, so applying a configuration never resets an
 * unrelated app back to the system default.
 */
data class SavedLocaleConfiguration(
    val id: String,
    val createdAt: Long,
    val entries: List<SavedLocaleEntry>,
) {
    val appCount: Int
        get() = entries.size
}

data class SavedLocaleEntry(
    val packageName: String,
    val label: String,
    val localeTag: String,
)

/** Result of importing the same portable JSON array used by local storage. */
sealed interface ConfigurationImportResult {
    data class Success(
        val importedCount: Int,
        val replacedCount: Int,
    ) : ConfigurationImportResult

    data object InvalidFormat : ConfigurationImportResult
}

/** A comparison of a saved preset with the locales currently reported by Android. */
data class SavedLocaleConfigurationComparison(
    val configuration: SavedLocaleConfiguration,
    val differences: List<SavedLocaleDifference>,
)

enum class SavedLocaleDifferenceKind {
    NeedsApply,
    MissingApp,
    CurrentOnly,
}

data class SavedLocaleDifference(
    val kind: SavedLocaleDifferenceKind,
    val packageName: String,
    val label: String,
    val currentLocaleTag: String? = null,
    val savedLocaleTag: String? = null,
)
