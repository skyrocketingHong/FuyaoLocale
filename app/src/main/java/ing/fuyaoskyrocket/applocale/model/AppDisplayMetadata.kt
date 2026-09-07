package ing.fuyaoskyrocket.applocale.model

/**
 * Read-only package facts shown on the detail screen. Null fields mean "not
 * available from this package's current information"; callers must render them
 * as unavailable instead of substituting zero values.
 */
data class AppDisplayMetadata(
    val versionName: String?,
    val versionCode: Long?,
    val minSdk: Int?,
    val targetSdk: Int?,
)
