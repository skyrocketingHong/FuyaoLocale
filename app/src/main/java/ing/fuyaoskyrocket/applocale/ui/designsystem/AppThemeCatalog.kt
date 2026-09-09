package ing.fuyaoskyrocket.applocale.ui.designsystem

import androidx.annotation.StringRes
import ing.fuyaoskyrocket.applocale.R

/** Display metadata is independent of the persisted enum name. */
data class AppThemeOption(
    val style: AppThemeStyle,
    @param:StringRes val titleRes: Int,
    @param:StringRes val summaryRes: Int,
    /** Android version order, e.g. 20, 30, 40, 44, 50; null for modern styles. */
    val androidVersionOrder: Int? = null,
    @param:StringRes val variantTitleRes: Int = titleRes,
)

data class AppThemeFamilyOption(
    val family: ThemeFamily,
    @param:StringRes val titleRes: Int,
    @param:StringRes val summaryRes: Int,
    val defaultVariant: ThemeVariant,
    val group: ThemeGroup,
)

object AppThemeCatalog {
    private val entries = listOf(
        AppThemeOption(AppThemeStyle.MATERIAL_YOU, R.string.theme_material_you, R.string.theme_material_you_summary),
        AppThemeOption(AppThemeStyle.MATERIAL3_EXPRESSIVE, R.string.theme_material3_expressive, R.string.theme_material3_expressive_summary),
        AppThemeOption(AppThemeStyle.MIUIX, R.string.theme_miuix, R.string.theme_miuix_summary),
        AppThemeOption(AppThemeStyle.ECLAIR, R.string.theme_eclair, R.string.theme_eclair_summary, 20),
        AppThemeOption(AppThemeStyle.FROYO, R.string.theme_froyo, R.string.theme_froyo_summary, 22),
        AppThemeOption(AppThemeStyle.GINGERBREAD, R.string.theme_gingerbread, R.string.theme_gingerbread_summary, 23),
        AppThemeOption(AppThemeStyle.HOLO_HONEYCOMB, R.string.theme_holo_honeycomb, R.string.theme_holo_honeycomb_summary, 30),
        AppThemeOption(AppThemeStyle.HOLO_ICS, R.string.theme_holo_ics, R.string.theme_holo_ics_summary, 40, R.string.theme_variant_ics),
        AppThemeOption(AppThemeStyle.HOLO_KITKAT, R.string.theme_holo_kitkat, R.string.theme_holo_kitkat_summary, 44, R.string.theme_variant_kitkat),
        AppThemeOption(AppThemeStyle.MATERIAL_LOLLIPOP, R.string.theme_lollipop, R.string.theme_lollipop_summary, 50),
        AppThemeOption(AppThemeStyle.MATERIAL_ROUNDED, R.string.theme_material_rounded, R.string.theme_material_rounded_summary),
    )

    val modern: List<AppThemeOption> = entries.filter { it.androidVersionOrder == null }
    val retro: List<AppThemeOption> = entries
        .filter { it.androidVersionOrder != null }
        .sortedBy { it.androidVersionOrder }
    val options: List<AppThemeOption> = entries.sortedBy { it.androidVersionOrder ?: it.style.family.androidVersionOrder ?: Int.MAX_VALUE }

    fun option(style: AppThemeStyle): AppThemeOption = options.first { it.style == style }

    val families = listOf(
        AppThemeFamilyOption(ThemeFamily.CLASSIC_ANDROID, R.string.theme_family_classic, R.string.theme_family_classic_summary, ThemeVariant.GINGERBREAD, ThemeGroup.EARLY_ANDROID),
        AppThemeFamilyOption(ThemeFamily.HONEYCOMB, R.string.theme_family_honeycomb, R.string.theme_holo_honeycomb_summary, ThemeVariant.HOLO_HONEYCOMB, ThemeGroup.EARLY_ANDROID),
        AppThemeFamilyOption(ThemeFamily.HOLO, R.string.theme_family_holo, R.string.theme_family_holo_summary, ThemeVariant.HOLO_ICS, ThemeGroup.EARLY_ANDROID),
        AppThemeFamilyOption(ThemeFamily.MATERIAL, R.string.theme_family_material, R.string.theme_lollipop_summary, ThemeVariant.MATERIAL_LOLLIPOP, ThemeGroup.MATERIAL),
        AppThemeFamilyOption(ThemeFamily.MATERIAL_ROUNDED, R.string.theme_material_rounded, R.string.theme_material_rounded_summary, ThemeVariant.MATERIAL_ROUNDED, ThemeGroup.MATERIAL),
        AppThemeFamilyOption(ThemeFamily.MATERIAL_YOU, R.string.theme_material_you, R.string.theme_material_you_summary, ThemeVariant.MATERIAL_YOU, ThemeGroup.MATERIAL),
        AppThemeFamilyOption(ThemeFamily.EXPRESSIVE, R.string.theme_family_expressive, R.string.theme_material3_expressive_summary, ThemeVariant.MATERIAL3_EXPRESSIVE, ThemeGroup.MATERIAL),
        AppThemeFamilyOption(ThemeFamily.MIUIX, R.string.theme_miuix, R.string.theme_miuix_summary, ThemeVariant.MIUIX, ThemeGroup.EXTENDED),
    ).sortedBy { it.family.androidVersionOrder ?: Int.MAX_VALUE }

    fun family(family: ThemeFamily): AppThemeFamilyOption = families.first { it.family == family }

    fun variants(family: ThemeFamily): List<AppThemeOption> = options.filter { it.style.family == family }
}
