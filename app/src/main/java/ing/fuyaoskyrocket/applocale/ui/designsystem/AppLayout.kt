package ing.fuyaoskyrocket.applocale.ui.designsystem

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shared Material 3 layout dimensions.
 *
 * Page margins stay in [AppSpacing]; these values describe component anatomy
 * and adaptive constraints that must remain consistent between screens.
 */
object AppLayout {
    val appListItemMinHeight = 72.dp
    val standardListItemMinHeight = 56.dp
    val appListIconSize = 44.dp
    val appListIconTextGap = 12.dp
    val appListSelectionSlotWidth = 48.dp

    /** App-list row anatomy: vertical padding, text-block gap (both non-token). */
    val appListRowVerticalPadding = 10.dp
    val appListTextGap = 2.dp
    val localeBadgeSize = 40.dp
    val appBarIconSize = 28.dp
    val appHeaderIconSize = 64.dp
    val appHeroIconSize = 64.dp
    val listContentInset = 64.dp
    val actionButtonMinWidth = 160.dp

    /** Continuous home-row anatomy per round-4 013-A: 48dp icon column, 16dp gutters. */
    val homeListIconColumnWidth = 48.dp
    val homeListIconSize = 48.dp
    val homeListIconTextGap = 16.dp
    val homeListRowHorizontalPadding = 16.dp
    val homeListRowVerticalPadding = 12.dp
    val homeListRowMinHeight = 80.dp

    /** The single horizontal frame every primary container aligns to (round-5 018-B). */
    val contentFrameMargin = 16.dp

    /**
     * The locale row's selected-background reach beyond the shared 16dp foreground
     * frame (round-7 028): rows lay out in an F−8 box so a selected row's container
     * keeps 12dp of clear space around the badge and the trailing glyph, while every
     * foreground element still starts at 16. Never an extra inset on top of 16.
     */
    val localeRowOuterMargin = 4.dp
    val localeChoiceRowMinHeight = 72.dp

    val readableContentMaxWidth = 840.dp
    val wideContentMaxWidth = 1040.dp
    // A 320dp master pane leaves a usable detail pane after the 80dp rail and 24dp gutters
    // at the first expanded breakpoint. Compact-height landscape phones stay single-pane.
    val listPaneWidth = 320.dp
    val warningMaxWidth = 560.dp
}

/** Constrains prose and form content while still filling narrow windows. */
fun Modifier.readableContentWidth(): Modifier =
    widthIn(max = AppLayout.readableContentMaxWidth).fillMaxWidth()

/**
 * For list items AFTER the host has consumed the shared horizontal page margin
 * (round-6 024): matches the home list's 840dp frame minus its two 16dp row
 * gutters, so detail pages never run 32dp wider than the home list.
 */
fun Modifier.readableListContentWidth(): Modifier =
    widthIn(
        max = AppLayout.readableContentMaxWidth - AppLayout.contentFrameMargin * 2,
    ).fillMaxWidth()

/** Constrains multi-column screen content on very wide windows. */
fun Modifier.wideContentWidth(): Modifier =
    widthIn(max = AppLayout.wideContentMaxWidth).fillMaxWidth()

/**
 * A scrollable list's TOTAL bottom contentPadding (round-5 018-A contract): the
 * maximum of the native bottom slot plus its trailing gap, the host dock's
 * measured footprint (already includes its own inset), and the independent
 * system-navigation minimum — never a sum of them.
 */
@Composable
fun listBottomReserve(bottomInset: Dp, endGap: Dp = AppSpacing.lg): Dp {
    val dockMetrics = LocalBottomDockMetrics.current
    val navigationBottom = WindowInsets.navigationBars
        .only(WindowInsetsSides.Bottom)
        .asPaddingValues()
        .calculateBottomPadding()
    return maxOf(
        bottomInset + endGap,
        if (dockMetrics.isPresent) dockMetrics.listEndPadding else 0.dp,
        navigationBottom + endGap,
    )
}

/** A scrollable list's top contentPadding: the real measured bar height plus its gap. */
@Composable
fun listTopReserve(topInset: Dp, topGap: Dp = AppUiTheme.spacing.bodyVerticalPadding): Dp = topInset + topGap
