package ing.fuyaoskyrocket.applocale.ui.designsystem

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Modifier
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
    val localeBadgeSize = 40.dp
    val appBarIconSize = 28.dp
    val appHeaderIconSize = 64.dp
    val appHeroIconSize = 64.dp
    val listContentInset = 64.dp
    val actionButtonMinWidth = 160.dp

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

/** Constrains multi-column screen content on very wide windows. */
fun Modifier.wideContentWidth(): Modifier =
    widthIn(max = AppLayout.wideContentMaxWidth).fillMaxWidth()
