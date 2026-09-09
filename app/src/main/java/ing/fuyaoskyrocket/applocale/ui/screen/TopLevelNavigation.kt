package ing.fuyaoskyrocket.applocale.ui.screen

import androidx.compose.runtime.staticCompositionLocalOf
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.components.AppNavigationDestination

/**
 * The neutral top-level navigation facts owned by [Navigation]: the five
 * destinations (with their label resources), the current one, the original
 * navigate-to-top-level callback and the tab double-tap scroll-to-top hook.
 * One NavController stays the single source of truth; presenters (the Holo
 * top tabs) consume this state without owning navigation themselves.
 */
class TopLevelNavigationState(
    val currentDestination: AppNavigationDestination,
    val destinations: List<Pair<AppNavigationDestination, Int>>,
    val navigate: (AppNavigationDestination) -> Unit,
    val onTabDoubleTap: (AppNavigationDestination) -> Unit,
)

val LocalTopLevelNavigation = staticCompositionLocalOf<TopLevelNavigationState?> { null }

/** The shared top-level destination list with label resources. */
fun topLevelDestinations(): List<Pair<AppNavigationDestination, Int>> = listOf(
    AppNavigationDestination.Home to R.string.navigation_home,
    AppNavigationDestination.SystemLanguages to R.string.navigation_system_languages,
    AppNavigationDestination.Configurations to R.string.navigation_configurations,
    AppNavigationDestination.Settings to R.string.settings_category,
    AppNavigationDestination.About to R.string.about,
)
