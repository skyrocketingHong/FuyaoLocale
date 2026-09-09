package ing.fuyaoskyrocket.applocale.ui.designsystem

import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.eclair.gingerbreadResource
import org.junit.Assert.*
import org.junit.Test

class ThemeFamilyTest {
    @Test fun nineExistingPreferenceValuesKeepTheirVariantAndFamily() {
        val oldValues = mapOf(
            "ECLAIR" to ThemeFamily.CLASSIC_ANDROID, "FROYO" to ThemeFamily.CLASSIC_ANDROID,
            "HOLO_HONEYCOMB" to ThemeFamily.HONEYCOMB, "HOLO_ICS" to ThemeFamily.HOLO,
            "HOLO_KITKAT" to ThemeFamily.HOLO, "MATERIAL_LOLLIPOP" to ThemeFamily.MATERIAL,
            "MATERIAL_YOU" to ThemeFamily.MATERIAL_YOU, "MATERIAL3_EXPRESSIVE" to ThemeFamily.EXPRESSIVE,
            "MIUIX" to ThemeFamily.MIUIX,
        )
        oldValues.forEach { (stored, family) ->
            assertEquals(stored, variantFromStored(stored).name)
            assertEquals(family, variantFromStored(stored).family)
        }
        assertEquals(ThemeVariant.MATERIAL_YOU, variantFromStored(null))
        assertEquals(ThemeVariant.MATERIAL_YOU, variantFromStored("future-variant"))
    }

    @Test fun familyPickerHasEightChoicesAndOnlyTwoVariantPickers() {
        assertEquals(8, AppThemeCatalog.families.size)
        assertEquals(ThemeFamily.entries.toSet(), AppThemeCatalog.families.map { it.family }.toSet())
        assertEquals(11, AppThemeCatalog.families.sumOf { AppThemeCatalog.variants(it.family).size })
        assertEquals(setOf(ThemeFamily.CLASSIC_ANDROID, ThemeFamily.HOLO),
            ThemeFamily.entries.filter { AppThemeCatalog.variants(it).size > 1 }.toSet())
        AppThemeCatalog.families.forEach {
            assertTrue(it.defaultVariant in AppThemeCatalog.variants(it.family).map { option -> option.style })
            assertTrue(it.titleRes != 0 && it.summaryRes != 0)
        }
        assertEquals(ThemeVariant.GINGERBREAD, AppThemeCatalog.family(ThemeFamily.CLASSIC_ANDROID).defaultVariant)
    }

    @Test fun chronologicalOrderDoesNotMoveTheRememberedOrDefaultChoice() {
        assertEquals(listOf(ThemeVariant.ECLAIR, ThemeVariant.FROYO, ThemeVariant.GINGERBREAD),
            AppThemeCatalog.variants(ThemeFamily.CLASSIC_ANDROID).map { it.style })
        assertEquals(listOf(ThemeVariant.HOLO_ICS, ThemeVariant.HOLO_KITKAT),
            AppThemeCatalog.variants(ThemeFamily.HOLO).map { it.style })
        val versions = AppThemeCatalog.families.mapNotNull { it.family.androidVersionOrder }
        assertEquals(versions.sorted(), versions)
        assertEquals(ThemeVariant.GINGERBREAD,
            preferredFamilyVariant(ThemeFamily.CLASSIC_ANDROID, null, ThemeVariant.MATERIAL_YOU))
        assertEquals(ThemeVariant.FROYO,
            preferredFamilyVariant(ThemeFamily.CLASSIC_ANDROID, ThemeVariant.FROYO, ThemeVariant.MATERIAL_YOU))
    }

    @Test fun leavingAnOldInstallationAndRestartingStillRestoresFroyo() {
        val recorded = rememberFamilyVariants(emptyMap(), ThemeVariant.FROYO, ThemeVariant.HOLO_KITKAT)
        // These are the two entries persisted in the same preference edit.
        val restored = recorded.mapValues { variantFromStored(it.value.name) }
        assertEquals(ThemeVariant.FROYO, preferredFamilyVariant(ThemeFamily.CLASSIC_ANDROID,
            restored[ThemeFamily.CLASSIC_ANDROID], ThemeVariant.HOLO_KITKAT))
        assertEquals(ThemeVariant.HOLO_KITKAT, preferredFamilyVariant(ThemeFamily.HOLO,
            restored[ThemeFamily.HOLO], ThemeVariant.FROYO))
    }

    @Test fun sameFamilyChangesReplaceTheRememberedVariantAndRejectForeignMemories() {
        val recorded = rememberFamilyVariants(emptyMap(), ThemeVariant.FROYO, ThemeVariant.GINGERBREAD)
        assertEquals(ThemeVariant.GINGERBREAD, recorded[ThemeFamily.CLASSIC_ANDROID])
        assertEquals(ThemeVariant.FROYO, preferredFamilyVariant(ThemeFamily.CLASSIC_ANDROID,
            ThemeVariant.GINGERBREAD, ThemeVariant.FROYO))
        assertEquals(ThemeVariant.GINGERBREAD, preferredFamilyVariant(ThemeFamily.CLASSIC_ANDROID,
            ThemeVariant.HOLO_KITKAT, ThemeVariant.MATERIAL_YOU))
    }

    @Test fun newThemeFailureConvergesToActualAppearanceAndKeepsIndependentEffects() {
        listOf(ThemeVariant.GINGERBREAD, ThemeVariant.MATERIAL_ROUNDED).forEach { target ->
            val model = AppAppearanceTransitionViewModel()
            model.requestTheme(target)
            model.requestMoreBlur(true)
            model.requestGlass(true)
            val actual = AppAppearanceState(ThemeVariant.FROYO, false, false, AppColorMode.LIGHT)
            model.onCommandFailed(model.uiState.value.generation, actual)
            val result = model.uiState.value
            assertEquals(AppearancePhase.Idle, result.phase)
            assertEquals(ThemeVariant.FROYO, result.applied.style)
            assertEquals(AppColorMode.LIGHT, result.applied.colorMode)
            assertTrue(result.applied.moreBlur && result.applied.liquidGlassNavigationBar)
        }
    }

    @Test fun gingerbreadUsesItsOwnWidgetsAndFontsWithoutRemappingAppImages() {
        listOf(
            R.drawable.eclair_btn_check to R.drawable.gingerbread_btn_check,
            R.drawable.eclair_edit_text to R.drawable.gingerbread_edit_text,
            R.drawable.eclair_tab_indicator to R.drawable.gingerbread_tab_indicator,
            R.drawable.eclair_popup_top_dark to R.drawable.gingerbread_popup_top_dark,
            R.drawable.eclair_progress_medium to R.drawable.gingerbread_progress_medium,
            R.font.eclair_droid_sans to R.font.gingerbread_droid_sans,
        ).forEach { (old, current) ->
            assertNotEquals(0, current)
            assertNotEquals(old, current)
            assertEquals(current, gingerbreadResource(old))
        }
        assertEquals(R.drawable.froyo_btn_dropdown, gingerbreadResource(R.drawable.froyo_btn_dropdown))
    }
}
