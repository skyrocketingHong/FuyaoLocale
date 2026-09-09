package ing.fuyaoskyrocket.applocale.ui.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Control-library-neutral colour roles. Every field pairs a container with its
 * matching foreground (for example [error] with [onError]); callers must keep
 * those pairs instead of falling back to [foreground] everywhere. The values are
 * derived per style by the material/, miuix/ and holo/ theme backends — never
 * cached as top-level statics.
 */
data class AppPalette(
    val background: Color,
    val foreground: Color,
    val surface: Color,
    val surfaceContent: Color,
    val secondarySurface: Color,
    val secondaryContent: Color,
    val muted: Color,
    val accent: Color,
    val onAccent: Color,
    val selected: Color,
    val onSelected: Color,
    /** Quiet container for non-interactive info badges and labels (020-A). */
    val quietContainer: Color,
    val quietContent: Color,
    val error: Color,
    val onError: Color,
    val divider: Color,
)

/**
 * Control-library-neutral text roles resolved from the active theme's native
 * type scale (Material typography, Miuix text styles, or the Holo Roboto
 * TextAppearance inheritance).
 */
data class AppTextStyles(
    val pageTitle: TextStyle,
    val itemTitle: TextStyle,
    val body: TextStyle,
    val metadata: TextStyle,
    val label: TextStyle,
)

/** A rendering family can have multiple versioned asset providers (for example Holo). */
enum class AppControlFamily { Material3, Material2, Miuix, Holo, Eclair, Lollipop }

/** How the top-level destinations are presented by the active style. */
enum class AppNavigationPresentation {
    /** Modern adaptive scheme: compact bottom bar / wide NavigationRail. */
    AdaptiveBottomOrRail,

    /** Holo ICS: top tabs stacked under the Action Bar, no bottom dock. */
    ActionBarTabs,

    /** Android 2.0 title strip and TabWidget. */
    ClassicTabs,

    /** Android 5.0 Toolbar and versioned top tabs. */
    ToolbarTabs,
}

/** How catalog-style pickers (language lists) are presented. */
enum class AppPickerPresentation {
    /** Modern ModalBottomSheet window. */
    NativeBottomSheet,

    /** Holo ICS floating list dialog. */
    HoloDialog,

    ClassicDialog,

    HoneycombDialog,

    Material2014Dialog,
}

/**
 * Era-dependent presentation decisions shared by chrome, overlays and pages.
 * Derived by the active theme backend — business code never hard-codes these.
 */
data class AppPresentationPolicy(
    val controls: AppControlFamily,
    val navigation: AppNavigationPresentation,
    val picker: AppPickerPresentation,
    /** Whether backdrop effects (glass dock, more blur) may render at all. */
    val supportsBackdropEffects: Boolean,
    /** Whether the about page shows the modern effect preference rows. */
    val showsModernEffectSettings: Boolean,
    /** Whether selection modes surface through a contextual action bar. */
    val usesContextualActions: Boolean,
    /** Versioned list rows occupy a continuous surface instead of separate cards. */
    val continuousLists: Boolean = false,
    /** Classic settings use CheckBoxPreference; public Switch arrived later. */
    val toggleUsesCheckbox: Boolean = false,
)

/**
 * List metrics of the active style. The modern backends fill their current
 * values; the Holo backend resolves the AOSP dimens (48/64/80dp rows, 8dp list
 * padding, 16dp dialog padding) from the qualifier-aware resources.
 */
data class AppUiMetrics(
    val listPreferredItemHeightSmall: Dp,
    val listPreferredItemHeight: Dp,
    val listPreferredItemHeightLarge: Dp,
    val listPreferredItemPaddingHorizontal: Dp,
    val dialogListPreferredItemPaddingHorizontal: Dp,
    val toolbarHeight: Dp = 48.dp,
    val tabHeight: Dp = toolbarHeight,
    val iconButtonSize: Dp = 48.dp,
) {
    companion object {
        /** Modern themes keep their existing row anatomy; rows size themselves. */
        val Modern = AppUiMetrics(
            listPreferredItemHeightSmall = 56.dp,
            listPreferredItemHeight = 64.dp,
            listPreferredItemHeightLarge = 72.dp,
            listPreferredItemPaddingHorizontal = 16.dp,
            dialogListPreferredItemPaddingHorizontal = 16.dp,
        )
    }
}

/** The values installed by the active theme backend; treat as read-only. */
class AppUiThemeValues internal constructor(
    palette: AppPalette,
    textStyles: AppTextStyles,
    /** The style whose backend actually installed these values. */
    val style: AppThemeStyle,
    metrics: AppUiMetrics,
    policy: AppPresentationPolicy,
) {
    val spec = makeThemeSpec(palette, textStyles, style, metrics, policy)
    val palette get() = spec.colors
    val textStyles get() = spec.typography
    val metrics get() = spec.spacing.metrics
    val policy get() = spec.components.policy
}

/**
 * Installed by [AppTheme] through the material/, miuix/ and holo/ backends.
 * Reading it outside an installed theme fails fast instead of silently
 * rendering with library-default colours.
 */
val LocalAppUiTheme = staticCompositionLocalOf<AppUiThemeValues> {
    error("AppUiTheme is not installed; wrap content in AppTheme first.")
}

/** Read-only access to the neutral palette and text roles of the current theme. */
object AppUiTheme {
    val spec: AppThemeSpec @Composable get() = LocalAppUiTheme.current.spec
    val shapes: AppShapeTokens @Composable get() = spec.shapes
    val spacing: AppSpacingTokens @Composable get() = spec.spacing
    val elevation: AppElevationTokens @Composable get() = spec.elevation
    val motion: AppMotionTokens @Composable get() = spec.motion
    val icons: AppIconTokens @Composable get() = spec.icons
    val components: AppComponentTokens @Composable get() = spec.components
    val palette: AppPalette
        @Composable get() = LocalAppUiTheme.current.palette

    val textStyles: AppTextStyles
        @Composable get() = LocalAppUiTheme.current.textStyles

    /** The style of the provider that is actually rendering right now. */
    val style: AppThemeStyle
        @Composable get() = LocalAppUiTheme.current.style

    val metrics: AppUiMetrics
        @Composable get() = LocalAppUiTheme.current.metrics

    val policy: AppPresentationPolicy
        @Composable get() = LocalAppUiTheme.current.policy
}
