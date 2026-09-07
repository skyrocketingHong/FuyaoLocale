package ing.fuyaoskyrocket.applocale.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks as FilledBookmarks
import androidx.compose.material.icons.filled.Info as FilledInfo
import androidx.compose.material.icons.filled.Language as FilledLanguage
import androidx.compose.material.icons.filled.Translate as FilledTranslate
import androidx.compose.material.icons.outlined.Bookmarks as OutlinedBookmarks
import androidx.compose.material.icons.outlined.Info as OutlinedInfo
import androidx.compose.material.icons.outlined.Language as OutlinedLanguage
import androidx.compose.material.icons.outlined.Translate as OutlinedTranslate
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemePreferences
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemeStyle
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.BackdropBlurDefaults
import ing.fuyaoskyrocket.applocale.ui.designsystem.LocalBlurProgress
import ing.fuyaoskyrocket.applocale.ui.designsystem.LocalGlassBackdrop
import ing.fuyaoskyrocket.applocale.ui.designsystem.LocalGlassNavigationBarVisibility
import ing.fuyaoskyrocket.applocale.ui.designsystem.LocalMoreBlurActive
import ing.fuyaoskyrocket.applocale.ui.designsystem.LocalSceneBackdrop
import ing.fuyaoskyrocket.applocale.ui.designsystem.appBarBackdrop
import ing.fuyaoskyrocket.applocale.ui.designsystem.blurProtectionTint
import ing.fuyaoskyrocket.applocale.ui.designsystem.glass.GlassNavigationColors
import ing.fuyaoskyrocket.applocale.ui.designsystem.glass.GlassTabAction
import ing.fuyaoskyrocket.applocale.ui.designsystem.glass.LiquidGlassNavigationBar
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.basic.NavigationBar as MiuixNavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem as MiuixNavigationBarItem
import top.yukonga.miuix.kmp.basic.NavigationItem as MiuixNavigationItem
import top.yukonga.miuix.kmp.basic.NavigationRail as MiuixNavigationRail
import top.yukonga.miuix.kmp.basic.NavigationRailItem as MiuixNavigationRailItem
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Home
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Translate

/** The four top-level destinations shared by compact and wide navigation. */
enum class AppNavigationDestination {
    Home,
    SystemLanguages,
    Configurations,
    About,
}

private data class AppNavigationItem(
    val destination: AppNavigationDestination,
    val labelRes: Int,
    val onClick: () -> Unit,
)

private fun appNavigationItems(
    onHomeClick: () -> Unit,
    onSystemLanguagesClick: () -> Unit,
    onConfigurationsClick: () -> Unit,
    onAboutClick: () -> Unit,
): List<AppNavigationItem> = listOf(
    AppNavigationItem(
        destination = AppNavigationDestination.Home,
        labelRes = R.string.navigation_home,
        onClick = onHomeClick,
    ),
    AppNavigationItem(
        destination = AppNavigationDestination.SystemLanguages,
        labelRes = R.string.navigation_system_languages,
        onClick = onSystemLanguagesClick,
    ),
    AppNavigationItem(
        destination = AppNavigationDestination.Configurations,
        labelRes = R.string.navigation_configurations,
        onClick = onConfigurationsClick,
    ),
    AppNavigationItem(
        destination = AppNavigationDestination.About,
        labelRes = R.string.about,
        onClick = onAboutClick,
    ),
)

/**
 * The destination's glyph in the active style's native set. The miuix set has no
 * filled/unfilled pairs — its bars carry the selected state themselves — while
 * Material keeps its filled/outlined pair.
 */
@Composable
private fun AppNavigationItem.icon(selected: Boolean): ImageVector =
    if (AppThemePreferences.style == AppThemeStyle.MIUIX) {
        when (destination) {
            AppNavigationDestination.Home -> MiuixIcons.Regular.Home
            AppNavigationDestination.SystemLanguages -> MiuixIcons.Regular.Translate
            AppNavigationDestination.Configurations -> MiuixIcons.Regular.Settings
            AppNavigationDestination.About -> MiuixIcons.Regular.Info
        }
    } else {
        when (destination) {
            AppNavigationDestination.Home ->
                if (selected) Icons.Filled.FilledTranslate else Icons.Outlined.OutlinedTranslate
            AppNavigationDestination.SystemLanguages ->
                if (selected) Icons.Filled.FilledLanguage else Icons.Outlined.OutlinedLanguage
            AppNavigationDestination.Configurations ->
                if (selected) Icons.Filled.FilledBookmarks else Icons.Outlined.OutlinedBookmarks
            AppNavigationDestination.About ->
                if (selected) Icons.Filled.FilledInfo else Icons.Outlined.OutlinedInfo
        }
    }

/**
 * Navigation labels reuse the full screen titles in every language. Step-based auto
 * sizing keeps long translations ("System languages", "Idiomas do sistema") on a
 * single line instead of wrapping or squeezing the item.
 */
@Composable
private fun NavigationItemLabel(labelRes: Int) {
    val labelColor = LocalContentColor.current
    BasicText(
        text = stringResource(labelRes),
        style = LocalTextStyle.current.copy(color = labelColor),
        maxLines = 1,
        autoSize = TextAutoSize.StepBased(
            minFontSize = 9.sp,
            maxFontSize = 14.sp,
            stepSize = 1.sp,
        ),
    )
}

/**
 * Theme-aware navigation for the app's top-level destinations. The Miuix style renders
 * the native miuix bottom navigation bar and Material You keeps the Material 3 bar;
 * when the Liquid Glass navigation bar preference is enabled, the bar is hoisted into
 * `LiquidGlassScaffold`'s floating overlay and refracts the live page content through
 * the backdrop instead, regardless of the active style.
 * Keeping it in one component makes the selected state and labels consistent across screens.
 *
 * [enabled] is combined with the shared glass visibility state: while the glass bar is
 * transitioning away (or otherwise gated), the bar stops accepting navigation even if
 * callers keep [enabled] true. In-slot bars render only while the glass backdrop is
 * absent, so the shared gate resolves to the caller's [enabled] there.
 */
@Composable
fun AppNavigationBar(
    currentDestination: AppNavigationDestination,
    onHomeClick: () -> Unit,
    onSystemLanguagesClick: () -> Unit,
    onConfigurationsClick: () -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onTabDoubleTap: (AppNavigationDestination) -> Unit = {},
) {
    val items = appNavigationItems(
        onHomeClick = onHomeClick,
        onSystemLanguagesClick = onSystemLanguagesClick,
        onConfigurationsClick = onConfigurationsClick,
        onAboutClick = onAboutClick,
    )
    val effectiveEnabled = enabled &&
        (LocalGlassNavigationBarVisibility.current?.canInteract ?: true)
    val glassBackdrop = LocalGlassBackdrop.current
    val moreBlurActive = LocalMoreBlurActive.current
    // One detector per bar (round-8 035): pairs real touch taps on the SAME tab
    // into one double-tap event. Keyboard, semantics and drag activations never
    // enter this counter; only classified touch taps do.
    val doubleTapDetector = rememberTabDoubleTapDetector()
    val latestOnTabDoubleTap by rememberUpdatedState(onTabDoubleTap)
    val scrollToTopActionLabel = stringResource(R.string.scroll_to_top)
    fun handleTabTap(destination: AppNavigationDestination) {
        if (doubleTapDetector.recordTap(destination)) {
            latestOnTabDoubleTap(destination)
        }
    }
    if (glassBackdrop != null) {
        // Liquid Glass preference on: the ported miuix example tab bar refracts the
        // page content captured by LiquidGlassScaffold, under either interface style.
        val glassItems = items.map { item ->
            val selected = currentDestination == item.destination
            MiuixNavigationItem(
                label = stringResource(item.labelRes),
                icon = item.icon(selected),
            )
        }
        LiquidGlassNavigationBar(
            items = glassItems,
            selectedIndex = items.indexOfFirst { it.destination == currentDestination }
                .coerceAtLeast(0),
            onItemClick = { index -> items.getOrNull(index)?.onClick?.invoke() },
            backdrop = glassBackdrop,
            modifier = modifier,
            enabled = effectiveEnabled,
            // Neutral palette roles so the glass bar matches the slot bars under
            // either theme style instead of relying on the legacy colour parameters.
            colors = GlassNavigationColors(
                container = AppUiTheme.palette.secondarySurface,
                indicator = AppUiTheme.palette.accent,
                content = AppUiTheme.palette.surfaceContent,
                activeContent = AppUiTheme.palette.accent,
            ),
            onItemTap = { index ->
                items.getOrNull(index)?.let { item -> handleTabTap(item.destination) }
            },
            currentTabAction = GlassTabAction(
                label = scrollToTopActionLabel,
                onAction = { latestOnTabDoubleTap(currentDestination) },
            ),
        )
        return
    }
    if (AppThemePreferences.style == AppThemeStyle.MIUIX) {
        MiuixNavigationBar(
            modifier = modifier.dockBarBackdrop(),
            color = if (moreBlurActive) Color.Transparent else MiuixTheme.colorScheme.surface,
        ) {
            items.forEach { item ->
                val selected = currentDestination == item.destination
                MiuixNavigationBarItem(
                    selected = selected,
                    onClick = item.onClick,
                    icon = item.icon(selected),
                    label = stringResource(item.labelRes),
                    enabled = effectiveEnabled,
                    modifier = Modifier.tabTouchTapObserver(
                        target = item.destination,
                        enabled = { effectiveEnabled },
                        onTap = { target -> handleTabTap(target as AppNavigationDestination) },
                    ).semantics {
                        if (selected) {
                            customActions = listOf(
                                CustomAccessibilityAction(scrollToTopActionLabel) {
                                    latestOnTabDoubleTap(currentDestination)
                                    true
                                },
                            )
                        }
                    },
                )
            }
        }
        return
    }
    NavigationBar(
        modifier = modifier.dockBarBackdrop(),
        // The scaffold/dock supplies the background under more blur; the bar
        // itself stays transparent so the blurred samples show through.
        containerColor = if (moreBlurActive) {
            Color.Transparent
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
    ) {
        items.forEach { item ->
            val selected = currentDestination == item.destination
            NavigationBarItem(
                selected = selected,
                onClick = item.onClick,
                enabled = effectiveEnabled,
                modifier = Modifier.tabTouchTapObserver(
                    target = item.destination,
                    enabled = { effectiveEnabled },
                    onTap = { target -> handleTabTap(target as AppNavigationDestination) },
                ).semantics {
                    if (selected) {
                        customActions = listOf(
                            CustomAccessibilityAction(scrollToTopActionLabel) {
                                latestOnTabDoubleTap(currentDestination)
                                true
                            },
                        )
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon(selected),
                        contentDescription = null,
                    )
                },
                label = { NavigationItemLabel(item.labelRes) },
            )
        }
    }
}

/**
 * The standard dock bar's more-blur background: blurred samples of the scene
 * (the dock overlays it and is never part of its own recording) plus the
 * protection tint, scaled by the host's effect progress.
 */
@Composable
private fun Modifier.dockBarBackdrop(): Modifier {
    val active = LocalMoreBlurActive.current
    val progress = LocalBlurProgress.current
    val backdrop = LocalSceneBackdrop.current
    if (!active || backdrop == null || progress <= 0.01f) return this
    return appBarBackdrop(
        backdrop = backdrop,
        radius = BackdropBlurDefaults.bottomBarRadius,
        fadeHeight = 0.dp,
        tint = blurProtectionTint(AppUiTheme.palette),
        progress = progress,
    )
}

/**
 * A navigation rail replaces the bottom bar from the medium width breakpoint.
 * The Miuix style renders the native miuix navigation rail. The rail always stays
 * theme-native; the Liquid Glass preference only covers the compact bottom tab bar.
 * This leaves the larger canvas for the list-detail and configuration panes.
 */
@Composable
fun AppNavigationRail(
    currentDestination: AppNavigationDestination,
    onHomeClick: () -> Unit,
    onSystemLanguagesClick: () -> Unit,
    onConfigurationsClick: () -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = appNavigationItems(
        onHomeClick = onHomeClick,
        onSystemLanguagesClick = onSystemLanguagesClick,
        onConfigurationsClick = onConfigurationsClick,
        onAboutClick = onAboutClick,
    )
    if (AppThemePreferences.style == AppThemeStyle.MIUIX) {
        MiuixNavigationRail(modifier = modifier) {
            Spacer(modifier = Modifier.height(AppSpacing.lg))
            items.forEach { item ->
                val selected = currentDestination == item.destination
                MiuixNavigationRailItem(
                    selected = selected,
                    onClick = item.onClick,
                    icon = item.icon(selected),
                    label = stringResource(item.labelRes),
                )
            }
            Spacer(modifier = Modifier.height(AppSpacing.lg))
        }
        return
    }
    NavigationRail(
        modifier = modifier.fillMaxHeight(),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Spacer(modifier = Modifier.height(AppSpacing.lg))
        items.forEach { item ->
            val selected = currentDestination == item.destination
            NavigationRailItem(
                selected = selected,
                onClick = item.onClick,
                icon = {
                    Icon(
                        imageVector = item.icon(selected),
                        contentDescription = null,
                    )
                },
                label = { NavigationItemLabel(item.labelRes) },
                alwaysShowLabel = true,
            )
        }
        Spacer(modifier = Modifier.height(AppSpacing.lg))
    }
}
