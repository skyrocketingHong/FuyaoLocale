package ing.fuyaoskyrocket.applocale.ui.designsystem

import androidx.compose.ui.unit.dp

/**
 * Spacing tokens for the app.  Components should prefer these values;
 * local-only dimensions inside individual composables may still use literals.
 */
object AppSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp

    /** Horizontal page margin for compact windows. */
    val screenCompact = lg

    /** Horizontal page margin for medium and expanded windows. */
    val screenExpanded = xl

    /** Space between independent panes in list-detail layouts. */
    val paneGap = xl
}
