package ing.fuyaoskyrocket.applocale.ui.designsystem

/** User-facing design languages; several variants may share one rendering backend. */
enum class ThemeFamily {
    CLASSIC_ANDROID, HONEYCOMB, HOLO, MATERIAL, MATERIAL_ROUNDED, MATERIAL_YOU, EXPRESSIVE, MIUIX,
}

enum class ThemeGroup { EARLY_ANDROID, MATERIAL, EXTENDED }

/** AppThemeStyle remains the JVM/storage identity used by existing installations. */
typealias ThemeVariant = AppThemeStyle
typealias ThemeBackend = AppControlFamily

val ThemeVariant.family: ThemeFamily
    get() = when (this) {
        ThemeVariant.ECLAIR, ThemeVariant.FROYO, ThemeVariant.GINGERBREAD -> ThemeFamily.CLASSIC_ANDROID
        ThemeVariant.HOLO_HONEYCOMB -> ThemeFamily.HONEYCOMB
        ThemeVariant.HOLO_ICS, ThemeVariant.HOLO_KITKAT -> ThemeFamily.HOLO
        ThemeVariant.MATERIAL_LOLLIPOP -> ThemeFamily.MATERIAL
        ThemeVariant.MATERIAL_ROUNDED -> ThemeFamily.MATERIAL_ROUNDED
        ThemeVariant.MATERIAL_YOU -> ThemeFamily.MATERIAL_YOU
        ThemeVariant.MATERIAL3_EXPRESSIVE -> ThemeFamily.EXPRESSIVE
        ThemeVariant.MIUIX -> ThemeFamily.MIUIX
    }

internal fun variantFromStored(raw: String?): ThemeVariant =
    ThemeVariant.entries.firstOrNull { it.name == raw } ?: ThemeVariant.MATERIAL_YOU

internal fun preferredFamilyVariant(
    family: ThemeFamily,
    remembered: ThemeVariant?,
    current: ThemeVariant,
): ThemeVariant = when {
    current.family == family -> current
    remembered?.family == family -> remembered
    else -> AppThemeCatalog.family(family).defaultVariant
}

/** Save both sides so a pre-family installation can return to its original variant. */
internal fun rememberFamilyVariants(
    remembered: Map<ThemeFamily, ThemeVariant>,
    current: ThemeVariant,
    next: ThemeVariant,
): Map<ThemeFamily, ThemeVariant> = remembered + (current.family to current) + (next.family to next)

internal val ThemeFamily.androidVersionOrder: Int?
    get() = when (this) {
        ThemeFamily.CLASSIC_ANDROID -> 20
        ThemeFamily.HONEYCOMB -> 30
        ThemeFamily.HOLO -> 40
        ThemeFamily.MATERIAL -> 50
        ThemeFamily.MATERIAL_ROUNDED -> 90
        ThemeFamily.MATERIAL_YOU -> 120
        ThemeFamily.EXPRESSIVE -> 160
        ThemeFamily.MIUIX -> null
    }
