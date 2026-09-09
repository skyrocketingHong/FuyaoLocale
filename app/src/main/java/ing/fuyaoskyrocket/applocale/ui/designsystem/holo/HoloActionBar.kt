package ing.fuyaoskyrocket.applocale.ui.designsystem.holo

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.components.AppNavigationDestination
import ing.fuyaoskyrocket.applocale.ui.components.rememberTabDoubleTapDetector
import ing.fuyaoskyrocket.applocale.ui.components.tabTouchTapObserver
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.revealSelectedTab

/**
 * The Widget.Holo.ActionBar.Solid chrome: the ab_solid_dark_holo 9-patch
 * background at the qualifier-aware height, title 18sp bright, leading
 * navigation slot and trailing action row. The caller composes the slots; the
 * bar never draws a second title.
 */
@Composable
internal fun HoloActionBar(
    modifier: Modifier = Modifier,
    navigation: (@Composable () -> Unit)? = null,
    actions: (@Composable androidx.compose.foundation.layout.RowScope.() -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
) {
    val metrics = LocalHoloMetrics.current
    val background = rememberHoloDrawable(HoloAsset.ActionBar)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(metrics.actionBarHeight)
            .holoBackground(background),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.width(8.dp))
        navigation?.invoke()
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
        ) {
            title?.invoke()
        }
        if (actions != null) {
            Row(content = actions)
        }
    }
}

/** The standard action bar title per TextAppearance.Holo.Widget.ActionBar.Title. */
@Composable
internal fun HoloActionBarTitle(text: String) {
    val textColors = LocalHoloTextColors.current
    HoloText(
        text = text,
        style = androidx.compose.ui.text.TextStyle(
            fontFamily = LocalHoloFontFamily.current,
            fontSize = LocalHoloMetrics.current.actionBarTitleTextSize,
        ),
        color = textColors.primary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/**
 * The stacked tab strip under a top-level action bar: the ab_stacked_solid
 * 9-patch background, Widget.Holo.ActionBar.TabView indicators (16dp
 * horizontal padding, tab_indicator_ab_holo states), 12sp bold labels, thin
 * dividers between tabs; the strip scrolls horizontally when long labels
 * overflow. Double-tapping a tab scrolls its page back to the top — a modern
 * convenience bound to the same tab area (round-9 042), with the custom
 * accessibility action kept on the active tab.
 */
@Composable
internal fun HoloTopTabs(
    currentDestination: AppNavigationDestination,
    onNavigate: (AppNavigationDestination) -> Unit,
    onTabDoubleTap: (AppNavigationDestination) -> Unit,
    destinations: List<Pair<AppNavigationDestination, Int>>,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalHoloMetrics.current
    val textColors = LocalHoloTextColors.current
    val stacked = rememberHoloDrawable(HoloAsset.ActionBarStacked)
    val doubleTapDetector = rememberTabDoubleTapDetector()
    val scrollToTopActionLabel = stringResource(R.string.scroll_to_top)

    BoxWithConstraints(modifier.fillMaxWidth()) {
    val tabWidth = ((maxWidth - (destinations.size - 1).coerceAtLeast(0).dp) /
        destinations.size.coerceAtLeast(1)).coerceAtLeast(88.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .holoBackground(stacked)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        destinations.forEachIndexed { index, (destination, labelRes) ->
            val selected = destination == currentDestination
            val indicator = rememberHoloDrawable(HoloAsset.TabIndicator)
            val interaction = remember { MutableInteractionSource() }
            val controlState = holoControlState(interaction, selected = selected)
            Box(
                modifier = Modifier
                    .widthIn(min = tabWidth)
                    .height(metrics.actionBarHeight)
                    .holoBackground(indicator, controlState)
                    .selectable(
                        selected = selected,
                        interactionSource = interaction,
                        indication = null,
                        role = androidx.compose.ui.semantics.Role.Tab,
                        onClick = { onNavigate(destination) },
                    )
                    .tabTouchTapObserver(
                        target = destination,
                        enabled = { true },
                        onTap = { target ->
                            if (doubleTapDetector.recordTap(target)) {
                                onTabDoubleTap(target as AppNavigationDestination)
                            }
                        },
                    )
                    .semantics {
                        if (selected) {
                            customActions = listOf(
                                CustomAccessibilityAction(scrollToTopActionLabel) {
                                    onTabDoubleTap(currentDestination)
                                    true
                                },
                            )
                        }
                    }
                    .revealSelectedTab(selected)
                    .padding(horizontal = metrics.actionBarTabPaddingHorizontal),
                contentAlignment = Alignment.Center,
            ) {
                HoloText(
                    text = stringResource(labelRes),
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = LocalHoloFontFamily.current,
                        fontSize = metrics.actionBarTabTextSize,
                    ),
                    color = textColors.primary,
                    fontWeight = if (metrics.actionBarTabBold) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (index != destinations.lastIndex) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(1.dp)
                        .height(24.dp)
                        .holoDrawable(rememberHoloDrawable(HoloAsset.VerticalDivider)),
                )
            }
        }
    }
    }
}

@Composable
private fun Modifier.heightInFix(height: androidx.compose.ui.unit.Dp): Modifier =
    this.then(Modifier.height(height))
