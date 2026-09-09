package ing.fuyaoskyrocket.applocale.ui.designsystem

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppColorModeTest {
    @Test
    fun explicitModesOverrideTheSystem() {
        listOf(false, true).forEach { systemDark ->
            assertFalse(AppColorMode.LIGHT.isDark(systemDark))
            assertTrue(AppColorMode.DARK.isDark(systemDark))
        }
    }

    @Test
    fun systemModeFollowsBothSystemStates() {
        assertFalse(AppColorMode.SYSTEM.isDark(false))
        assertTrue(AppColorMode.SYSTEM.isDark(true))
    }

    @Test
    fun storedModesRoundTripAndOldPreferencesDefaultToSystem() {
        AppColorMode.entries.forEach { assertEquals(it, AppColorMode.fromStored(it.name)) }
        assertEquals(AppColorMode.SYSTEM, AppColorMode.fromStored(null))
        assertEquals(AppColorMode.SYSTEM, AppColorMode.fromStored("unknown"))
    }

    @Test
    fun holoBarsRemainReadableInLightAndDark() {
        listOf(AppThemeStyle.HOLO_ICS, AppThemeStyle.HOLO_KITKAT, AppThemeStyle.ECLAIR, AppThemeStyle.FROYO, AppThemeStyle.HOLO_HONEYCOMB).forEach {
            assertTrue(usesDarkSystemBarSurface(it, false))
            assertTrue(usesDarkSystemBarSurface(it, true))
        }
        listOf(AppThemeStyle.MATERIAL_YOU, AppThemeStyle.MATERIAL3_EXPRESSIVE, AppThemeStyle.MIUIX).forEach {
            assertFalse(usesDarkSystemBarSurface(it, false))
            assertTrue(usesDarkSystemBarSurface(it, true))
        }
    }

    @Test
    fun styleAndModeRequestsMergeAndApplyTogether() {
        val model = AppAppearanceTransitionViewModel()
        model.requestTheme(AppThemeStyle.HOLO_ICS)
        model.requestColorMode(AppColorMode.DARK)
        val state = model.uiState.value
        assertEquals(AppThemeStyle.HOLO_ICS, state.requested.style)
        assertEquals(AppColorMode.DARK, state.requested.colorMode)
        model.onFadeOutFinished(state.generation)
        model.markCommandStarted(state.generation)
        model.onCommandApplied(state.generation, null)
        assertEquals(state.requested, model.uiState.value.applied)
        assertEquals(AppearancePhase.AwaitingFrame, model.uiState.value.phase)
    }

    @Test
    fun switchingStylesKeepsTheRequestedMode() {
        AppThemeStyle.entries.forEach { style ->
            val model = AppAppearanceTransitionViewModel()
            model.requestColorMode(AppColorMode.LIGHT)
            model.requestMoreBlur(true)
            model.requestGlass(true)
            model.requestTheme(style)
            val requested = model.uiState.value.requested
            assertEquals(AppColorMode.LIGHT, requested.colorMode)
            assertTrue(requested.moreBlur)
            assertTrue(requested.liquidGlassNavigationBar)
        }
    }

    @Test
    fun failureRestoresActualModeWithoutClobberingIndependentEffects() {
        val model = AppAppearanceTransitionViewModel()
        model.requestMoreBlur(true)
        model.requestGlass(true)
        model.requestColorMode(AppColorMode.DARK)
        val actual = AppAppearanceState(AppThemeStyle.MATERIAL_YOU, false, false, AppColorMode.LIGHT)
        model.onCommandFailed(model.uiState.value.generation, actual)
        val recovered = model.uiState.value
        assertEquals(AppColorMode.LIGHT, recovered.applied.colorMode)
        assertEquals(AppColorMode.LIGHT, recovered.requested.colorMode)
        assertTrue(recovered.requested.moreBlur)
        assertTrue(recovered.applied.liquidGlassNavigationBar)
    }
}
