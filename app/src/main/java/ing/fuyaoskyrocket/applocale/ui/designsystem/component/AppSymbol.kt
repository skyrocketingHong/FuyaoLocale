package ing.fuyaoskyrocket.applocale.ui.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemePreferences
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemeStyle
import ing.fuyaoskyrocket.applocale.ui.designsystem.material.materialSymbolVector
import ing.fuyaoskyrocket.applocale.ui.designsystem.miuix.miuixSymbolVector

/**
 * Named app-chrome glyphs, deliberately not [ImageVector]s: business code asks
 * for a symbol by name and each backend resolves it to its own native glyph
 * (MiuixIcons under the MIUIX style, Material icons under Material You).
 * Extending the set requires updating both backends; the exhaustive `when` in
 * each backend enforces that.
 *
 * Glyphs without a miuix counterpart never get an entry here: call sites pass
 * their project vector to [AppIcon], which draws it with the active backend's
 * native icon control, and the vector is listed as a documented asset
 * exception in the 012 execution record.
 */
enum class AppSymbol {
    Search,
    Menu,
    Back,
    Close,
    Refresh,
    Settings,
    Pin,
    /** The pinned (active) pin state; the miuix set only shifts glyph weight. */
    PinActive,
    Check,
    SelectAll,
    /** Sort-order control affordance. */
    Sort,
    /** Row affordance that opens the next level (chevron on miuix). */
    Forward,
    /** Remove affordance (trash on both sets). */
    Delete,
    /** Informational affordance. */
    Info,
    /** Add/create affordance. */
    Add,
}

/**
 * Resolves the theme-native glyph for [symbol]; render the result with
 * [AppIcon]. Never cache the value outside composition.
 */
@Composable
fun AppSymbolVector(symbol: AppSymbol): ImageVector =
    if (AppThemePreferences.style == AppThemeStyle.MIUIX) {
        symbol.miuixSymbolVector()
    } else {
        symbol.materialSymbolVector()
    }
