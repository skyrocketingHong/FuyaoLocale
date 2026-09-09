package ing.fuyaoskyrocket.applocale.ui.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class CornerGeometry { Square, Rounded, Capsule, Squircle, Framework }
enum class TouchFeedback { StateSelector, Ripple, ShapeMorph }
enum class IconSource { Classic, Holographic, MaterialClassic, Material, Miuix }
enum class ToolbarActionPresentation { Menu, Inline }
enum class FilterPresentation { Dropdown, Chips }
enum class SelectionPresentation { ContextBar, BottomBar }

@Immutable
data class AppShapeToken(val radius: Dp, val geometry: CornerGeometry = CornerGeometry.Rounded) {
    fun roundedShape() = if (geometry == CornerGeometry.Capsule) androidx.compose.foundation.shape.RoundedCornerShape(50)
        else androidx.compose.foundation.shape.RoundedCornerShape(radius)
}

@Immutable
data class AppShapeTokens(
    val row: AppShapeToken,
    val section: AppShapeToken,
    val card: AppShapeToken,
    val button: AppShapeToken,
    val input: AppShapeToken,
    val dialog: AppShapeToken,
)

@Immutable
data class AppSpacingTokens(
    val metrics: AppUiMetrics,
    val contentInset: Dp,
    val sectionGap: Dp,
    val heroPadding: Dp,
    val bodyVerticalPadding: Dp,
    val filterGap: Dp,
    val rowOuterInset: Dp,
    val listGap: Dp,
    val paneOuterInset: Dp,
    val paneGap: Dp,
) {
    val toolbarHeight: Dp get() = metrics.toolbarHeight
    val tabHeight: Dp get() = metrics.tabHeight
}

@Immutable
data class AppElevationTokens(val card: Dp, val section: Dp, val toolbar: Dp, val menu: Dp, val dialog: Dp)

@Immutable
data class AppMotionTokens(
    val feedback: TouchFeedback,
    val batchEnterMillis: Int,
    /** Native libraries or fixed-era window implementations own their actual curves. */
    val picker: AppPickerPresentation,
)

@Immutable
data class AppIconTokens(val source: IconSource, val actionSize: Dp, val tabSize: Dp, val heroSize: Dp)

@Immutable
data class AppComponentTokens(
    val policy: AppPresentationPolicy,
    val toolbarActions: ToolbarActionPresentation,
    val filters: FilterPresentation,
    val selection: SelectionPresentation,
)

/** Immutable values only. Native drawables, animation clocks and callbacks belong to each control. */
data class AppThemeSpec(
    val variant: ThemeVariant,
    val colors: AppPalette,
    val typography: AppTextStyles,
    val shapes: AppShapeTokens,
    val spacing: AppSpacingTokens,
    val elevation: AppElevationTokens,
    val motion: AppMotionTokens,
    val icons: AppIconTokens,
    val components: AppComponentTokens,
) {
    val family: ThemeFamily get() = variant.family
    val backend: ThemeBackend get() = components.policy.controls
}

internal fun themeShapes(variant: ThemeVariant): AppShapeTokens {
    val square = AppShapeToken(0.dp, CornerGeometry.Square)
    val skin = AppShapeToken(0.dp, CornerGeometry.Framework)
    val pill = AppShapeToken(24.dp, CornerGeometry.Capsule)
    return when (variant.family) {
        ThemeFamily.CLASSIC_ANDROID, ThemeFamily.HONEYCOMB, ThemeFamily.HOLO ->
            AppShapeTokens(square, square, square, skin, skin, skin)
        ThemeFamily.MATERIAL -> AppShapeToken(2.dp).let { small ->
            AppShapeTokens(square, small, small, small, square, small)
        }
        ThemeFamily.MATERIAL_ROUNDED -> AppShapeTokens(
            AppShapeToken(12.dp), AppShapeToken(16.dp), AppShapeToken(12.dp), pill,
            AppShapeToken(12.dp), AppShapeToken(16.dp),
        )
        ThemeFamily.MATERIAL_YOU -> AppShapeTokens(
            AppShapeToken(16.dp), AppShapeToken(28.dp), AppShapeToken(12.dp), pill,
            AppShapeToken(12.dp), AppShapeToken(28.dp),
        )
        ThemeFamily.EXPRESSIVE -> AppShapeTokens(
            AppShapeToken(20.dp), AppShapeToken(32.dp), AppShapeToken(16.dp), pill,
            AppShapeToken(16.dp), AppShapeToken(28.dp),
        )
        ThemeFamily.MIUIX -> AppShapeToken(28.dp, CornerGeometry.Squircle).let { section ->
            AppShapeTokens(AppShapeToken(16.dp, CornerGeometry.Squircle), section, section,
                AppShapeToken(12.dp, CornerGeometry.Squircle), AppShapeToken(16.dp, CornerGeometry.Squircle), section)
        }
    }
}

internal fun themeElevation(variant: ThemeVariant): AppElevationTokens = when (variant.family) {
    ThemeFamily.CLASSIC_ANDROID, ThemeFamily.HONEYCOMB, ThemeFamily.HOLO -> AppElevationTokens(0.dp, 0.dp, 0.dp, 0.dp, 0.dp)
    ThemeFamily.MATERIAL -> AppElevationTokens(2.dp, 2.dp, 4.dp, 8.dp, 24.dp)
    ThemeFamily.MATERIAL_ROUNDED -> AppElevationTokens(1.dp, 1.dp, 2.dp, 8.dp, 24.dp)
    ThemeFamily.MATERIAL_YOU, ThemeFamily.EXPRESSIVE -> AppElevationTokens(0.dp, 0.dp, 0.dp, 3.dp, 6.dp)
    ThemeFamily.MIUIX -> AppElevationTokens(0.dp, 0.dp, 0.dp, 0.dp, 0.dp)
}

internal fun themeMotion(variant: ThemeVariant, picker: AppPickerPresentation): AppMotionTokens = when (variant.family) {
    ThemeFamily.CLASSIC_ANDROID, ThemeFamily.HONEYCOMB, ThemeFamily.HOLO -> AppMotionTokens(TouchFeedback.StateSelector, 0, picker)
    ThemeFamily.MATERIAL, ThemeFamily.MATERIAL_ROUNDED, ThemeFamily.MATERIAL_YOU, ThemeFamily.MIUIX -> AppMotionTokens(TouchFeedback.Ripple, 180, picker)
    ThemeFamily.EXPRESSIVE -> AppMotionTokens(TouchFeedback.ShapeMorph, 180, picker)
}

internal fun makeThemeSpec(
    palette: AppPalette,
    typography: AppTextStyles,
    variant: ThemeVariant,
    metrics: AppUiMetrics,
    policy: AppPresentationPolicy,
): AppThemeSpec {
    val continuous = policy.continuousLists
    val source = when (policy.controls) {
        AppControlFamily.Eclair -> IconSource.Classic
        AppControlFamily.Holo -> IconSource.Holographic
        AppControlFamily.Lollipop -> IconSource.MaterialClassic
        AppControlFamily.Material2, AppControlFamily.Material3 -> IconSource.Material
        AppControlFamily.Miuix -> IconSource.Miuix
    }
    return AppThemeSpec(
        variant, palette, typography, themeShapes(variant),
        AppSpacingTokens(metrics, metrics.listPreferredItemPaddingHorizontal,
            if (continuous) 0.dp else 12.dp, if (continuous) 8.dp else 16.dp, 8.dp, if (continuous) 0.dp else 8.dp,
            if (continuous) 0.dp else AppLayout.localeRowOuterMargin,
            if (continuous) 0.dp else 8.dp, if (continuous) 0.dp else AppLayout.contentFrameMargin,
            if (continuous) 1.dp else AppSpacing.paneGap),
        themeElevation(variant), themeMotion(variant, policy.picker),
        AppIconTokens(source, 24.dp, if (source == IconSource.Classic) 36.dp else 24.dp, if (continuous) 48.dp else AppLayout.appHeroIconSize),
        AppComponentTokens(policy,
            if (continuous) ToolbarActionPresentation.Menu else ToolbarActionPresentation.Inline,
            if (continuous) FilterPresentation.Dropdown else FilterPresentation.Chips,
            if (policy.usesContextualActions) SelectionPresentation.ContextBar else SelectionPresentation.BottomBar),
    )
}
