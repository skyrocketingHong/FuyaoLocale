package ing.fuyaoskyrocket.applocale.ui.screen

import android.net.Uri
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.window.core.layout.WindowSizeClass
import ing.fuyaoskyrocket.applocale.ui.appinfo.AppInfoScreen
import ing.fuyaoskyrocket.applocale.ui.components.AppNavigationBar
import ing.fuyaoskyrocket.applocale.ui.components.AppNavigationDestination
import ing.fuyaoskyrocket.applocale.ui.components.AppNavigationRail
import ing.fuyaoskyrocket.applocale.ui.configurations.ConfigurationsScreen
import ing.fuyaoskyrocket.applocale.ui.configurations.ConfigurationDetailScreen
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppChromeHost
import ing.fuyaoskyrocket.applocale.ui.main.LargeHomeScreen
import ing.fuyaoskyrocket.applocale.ui.main.MainScreen
import ing.fuyaoskyrocket.applocale.ui.screen.about.AboutScreen
import ing.fuyaoskyrocket.applocale.ui.systemlanguages.SystemLanguagesScreen

object Destinations {
    const val HOME = "home"
    const val SYSTEM_LANGUAGES = "system_languages"
    const val CONFIGURATIONS = "configurations"
    const val CONFIGURATION_DETAIL = "configuration_detail"
    const val APP_INFO = "app_info"
    const val ABOUT = "about"
}

/**
 * Top-level adaptive navigation. Compact windows use the bottom navigation
 * bar inside each top-level screen; wider windows keep a persistent M3 rail
 * beside their content.
 */
@Composable
fun Navigation(
    hasGrantedShizukuPermission: Boolean,
    onRequestShizukuPermission: () -> Unit,
    onOpenShizuku: () -> Unit,
) {
    val navController = rememberNavController()
    val windowInfo = currentWindowAdaptiveInfo()
    val isCompact = !windowInfo.windowSizeClass.isWidthAtLeastBreakpoint(
        WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND,
    )
    val useWideLayout = windowInfo.windowSizeClass.isAtLeastBreakpoint(
        widthDpBreakpoint = WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND,
        heightDpBreakpoint = WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND,
    )
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentDestination = when (currentRoute) {
        Destinations.SYSTEM_LANGUAGES -> AppNavigationDestination.SystemLanguages
        Destinations.CONFIGURATIONS,
        "${Destinations.CONFIGURATION_DETAIL}/{configuration_id}"
        -> AppNavigationDestination.Configurations
        Destinations.ABOUT -> AppNavigationDestination.About
        else -> AppNavigationDestination.Home
    }
    // The floating glass tab bar only belongs to the four top-level destinations;
    // detail routes (app info, configuration detail) keep their bottom edge free.
    val isTopLevelDestination = currentRoute == Destinations.HOME ||
        currentRoute == Destinations.SYSTEM_LANGUAGES ||
        currentRoute == Destinations.CONFIGURATIONS ||
        currentRoute == Destinations.ABOUT

    // Round-8 035: the page-level scroll-to-top request hub. A double tap only
    // scrolls; it never refreshes or clears page state. A pending request lives
    // exactly as long as the matching top-level route stays visible.
    val tabScrollCoordinator = rememberTabScrollCoordinator()
    LaunchedEffect(currentRoute, isTopLevelDestination) {
        if (isTopLevelDestination) {
            tabScrollCoordinator.onTopLevelRouteChanged(currentDestination)
        } else {
            tabScrollCoordinator.onLeftTopLevelRoutes()
        }
    }

    fun navigateToTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    if (isCompact) {
        // One fixed chrome host for the whole compact window: the NavHost exists
        // once inside it; the bottom dock (glass or standard) is an overlay the
        // host owns, regardless of any effect or glass preference.
        AppChromeHost(
            isTopLevelDestination = isTopLevelDestination,
            navigationBar = {
                AppNavigationBar(
                    currentDestination = currentDestination,
                    onHomeClick = { navigateToTopLevel(Destinations.HOME) },
                    onSystemLanguagesClick = { navigateToTopLevel(Destinations.SYSTEM_LANGUAGES) },
                    onConfigurationsClick = { navigateToTopLevel(Destinations.CONFIGURATIONS) },
                    onAboutClick = { navigateToTopLevel(Destinations.ABOUT) },
                    onTabDoubleTap = tabScrollCoordinator::request,
                )
            },
        ) {
            FuyaoNavHost(
                navController = navController,
                isCompact = true,
                useWideLayout = false,
                hasGrantedShizukuPermission = hasGrantedShizukuPermission,
                onRequestShizukuPermission = onRequestShizukuPermission,
                onOpenShizuku = onOpenShizuku,
                navigateToTopLevel = ::navigateToTopLevel,
                tabScrollCoordinator = tabScrollCoordinator,
                modifier = Modifier.fillMaxSize(),
            )
        }
    } else {
        Row(modifier = Modifier.fillMaxSize()) {
            AppNavigationRail(
                currentDestination = currentDestination,
                onHomeClick = { navigateToTopLevel(Destinations.HOME) },
                onSystemLanguagesClick = { navigateToTopLevel(Destinations.SYSTEM_LANGUAGES) },
                onConfigurationsClick = { navigateToTopLevel(Destinations.CONFIGURATIONS) },
                onAboutClick = { navigateToTopLevel(Destinations.ABOUT) },
            )
            FuyaoNavHost(
                navController = navController,
                isCompact = false,
                useWideLayout = useWideLayout,
                hasGrantedShizukuPermission = hasGrantedShizukuPermission,
                onRequestShizukuPermission = onRequestShizukuPermission,
                onOpenShizuku = onOpenShizuku,
                navigateToTopLevel = ::navigateToTopLevel,
                tabScrollCoordinator = tabScrollCoordinator,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun FuyaoNavHost(
    navController: androidx.navigation.NavHostController,
    isCompact: Boolean,
    useWideLayout: Boolean,
    hasGrantedShizukuPermission: Boolean,
    onRequestShizukuPermission: () -> Unit,
    onOpenShizuku: () -> Unit,
    navigateToTopLevel: (String) -> Unit,
    tabScrollCoordinator: TabScrollCoordinator,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Destinations.HOME,
        modifier = modifier,
    ) {
        composable(route = Destinations.HOME) {
            if (isCompact || !useWideLayout) {
                MainScreen(
                    navigateToAppScreen = { navController.navigate("${Destinations.APP_INFO}/$it") },
                    navigateToSystemLanguages = {
                        navigateToTopLevel(Destinations.SYSTEM_LANGUAGES)
                    },
                    navigateToConfigurations = {
                        navigateToTopLevel(Destinations.CONFIGURATIONS)
                    },
                    navigateToAbout = { navigateToTopLevel(Destinations.ABOUT) },
                    hasGrantedShizukuPermission = hasGrantedShizukuPermission,
                    onRequestShizukuPermission = onRequestShizukuPermission,
                    onOpenShizuku = onOpenShizuku,
                    showBottomNavigation = isCompact,
                    tabScrollCoordinator = tabScrollCoordinator,
                )
            } else {
                LargeHomeScreen(
                    navigateToSystemLanguages = {
                        navigateToTopLevel(Destinations.SYSTEM_LANGUAGES)
                    },
                    navigateToConfigurations = {
                        navigateToTopLevel(Destinations.CONFIGURATIONS)
                    },
                    navigateToAbout = { navigateToTopLevel(Destinations.ABOUT) },
                    hasGrantedShizukuPermission = hasGrantedShizukuPermission,
                    onRequestShizukuPermission = onRequestShizukuPermission,
                    onOpenShizuku = onOpenShizuku,
                )
            }
        }

        composable(route = Destinations.CONFIGURATIONS) {
            ConfigurationsScreen(
                navigateToHome = { navigateToTopLevel(Destinations.HOME) },
                navigateToSystemLanguages = {
                    navigateToTopLevel(Destinations.SYSTEM_LANGUAGES)
                },
                navigateToAbout = { navigateToTopLevel(Destinations.ABOUT) },
                onOpenConfiguration = { configurationId ->
                    navController.navigate(
                        "${Destinations.CONFIGURATION_DETAIL}/${Uri.encode(configurationId)}",
                    )
                },
                showBottomNavigation = isCompact,
                useWideLayout = useWideLayout,
                tabScrollCoordinator = tabScrollCoordinator,
            )
        }

        composable(route = Destinations.SYSTEM_LANGUAGES) {
            SystemLanguagesScreen(
                navigateToHome = { navigateToTopLevel(Destinations.HOME) },
                navigateToConfigurations = {
                    navigateToTopLevel(Destinations.CONFIGURATIONS)
                },
                navigateToAbout = { navigateToTopLevel(Destinations.ABOUT) },
                hasGrantedShizukuPermission = hasGrantedShizukuPermission,
                onRequestShizukuPermission = onRequestShizukuPermission,
                onOpenShizuku = onOpenShizuku,
                showBottomNavigation = isCompact,
                tabScrollCoordinator = tabScrollCoordinator,
            )
        }

        composable(
            route = "${Destinations.CONFIGURATION_DETAIL}/{configuration_id}",
            arguments = listOf(navArgument("configuration_id") { type = NavType.StringType }),
        ) { backStackEntry ->
            val configurationId = backStackEntry.arguments
                ?.getString("configuration_id")
                ?: return@composable
            // Round-8 038: a completed live edit REPLACES this route with the
            // derived configuration instead of stacking a second detail on
            // top, and hands the edited row's package to the new page as its
            // scroll anchor (consumed exactly once, then cleared).
            var detailAnchorPackage by remember { mutableStateOf<String?>(null) }
            ConfigurationDetailScreen(
                configurationId = configurationId,
                navigateBack = { navController.navigateUp() },
                onConfigurationReplaced = { newConfigurationId, anchorPackage ->
                    detailAnchorPackage = anchorPackage
                    val currentEntryId = navController.currentBackStackEntry?.id
                    navController.navigate(
                        "${Destinations.CONFIGURATION_DETAIL}/${Uri.encode(newConfigurationId)}",
                    ) {
                        if (currentEntryId != null) {
                            popUpTo(currentEntryId) { inclusive = true }
                        }
                        launchSingleTop = true
                    }
                },
                anchorPackage = detailAnchorPackage,
                onAnchorConsumed = { detailAnchorPackage = null },
            )
        }

        composable(
            route = "${Destinations.APP_INFO}/{app_id}",
            arguments = listOf(navArgument("app_id") { type = NavType.StringType }),
        ) { backStackEntry ->
            val appId = backStackEntry.arguments?.getString("app_id") ?: return@composable
            AppInfoScreen(appId = appId, navigateBack = { navController.navigateUp() })
        }

        composable(route = Destinations.ABOUT) {
            AboutScreen(
                showBottomNavigation = isCompact,
                navigateToHome = { navigateToTopLevel(Destinations.HOME) },
                navigateToSystemLanguages = {
                    navigateToTopLevel(Destinations.SYSTEM_LANGUAGES)
                },
                navigateToConfigurations = {
                    navigateToTopLevel(Destinations.CONFIGURATIONS)
                },
                tabScrollCoordinator = tabScrollCoordinator,
            )
        }
    }
}
