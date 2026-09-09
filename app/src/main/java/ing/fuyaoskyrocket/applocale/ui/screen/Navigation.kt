package ing.fuyaoskyrocket.applocale.ui.screen

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import ing.fuyaoskyrocket.applocale.data.preferences.AppUserPreferences
import ing.fuyaoskyrocket.applocale.ui.designsystem.LocalUserPreferences
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.window.core.layout.WindowSizeClass
import ing.fuyaoskyrocket.applocale.ui.appinfo.AppInfoScreen
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppNavigationBar
import ing.fuyaoskyrocket.applocale.ui.components.AppNavigationDestination
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppNavigationRail
import ing.fuyaoskyrocket.applocale.ui.configurations.ConfigurationsScreen
import ing.fuyaoskyrocket.applocale.ui.configurations.ConfigurationDetailScreen
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppChromeHost
import ing.fuyaoskyrocket.applocale.ui.main.LargeHomeScreen
import ing.fuyaoskyrocket.applocale.ui.main.MainScreen
import ing.fuyaoskyrocket.applocale.ui.screen.about.AboutScreen
import ing.fuyaoskyrocket.applocale.ui.screen.settings.SettingsScreen
import ing.fuyaoskyrocket.applocale.ui.systemlanguages.SystemLanguagesScreen

object Destinations {
    const val HOME = "home"
    const val SYSTEM_LANGUAGES = "system_languages"
    const val CONFIGURATIONS = "configurations"
    const val CONFIGURATION_DETAIL = "configuration_detail"
    const val APP_INFO = "app_info"
    const val SETTINGS = "settings"
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
    val userPreferences = LocalUserPreferences.current
    val initialRoute = remember {
        restoredTopLevelRoute(userPreferences.rememberLastPage, userPreferences.lastPage)
    }
    val pagerState = rememberPagerState(initialPage = topLevelRoutes.indexOf(initialRoute).coerceAtLeast(0)) {
        topLevelRoutes.size
    }
    val navigationScope = rememberCoroutineScope()
    // Keep the selected master/detail app outside the width-dependent host branches.
    // Narrowing a tablet window continues showing that same detail and ViewModel.
    var selectedHomeApp by rememberSaveable { mutableStateOf<String?>(null) }
    val windowInfo = currentWindowAdaptiveInfo()
    val isCompact = !windowInfo.windowSizeClass.isWidthAtLeastBreakpoint(
        WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND,
    )
    val useWideLayout = windowInfo.windowSizeClass.isAtLeastBreakpoint(
        widthDpBreakpoint = WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND,
        heightDpBreakpoint = WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND,
    )
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentNavRoute = backStackEntry?.destination?.route
    val currentRoute = if (currentNavRoute == Destinations.HOME) topLevelRoutes[pagerState.settledPage] else currentNavRoute
    val currentDestination = when (currentRoute) {
        Destinations.SYSTEM_LANGUAGES -> AppNavigationDestination.SystemLanguages
        Destinations.CONFIGURATIONS,
        "${Destinations.CONFIGURATION_DETAIL}/{configuration_id}"
        -> AppNavigationDestination.Configurations
        Destinations.SETTINGS -> AppNavigationDestination.Settings
        Destinations.ABOUT -> AppNavigationDestination.About
        else -> AppNavigationDestination.Home
    }
    val displayedDestination = if (currentNavRoute == Destinations.HOME) {
        topLevelDestinations()[pagerState.currentPage].first
    } else currentDestination
    // The floating glass tab bar only belongs to the five top-level destinations;
    // detail routes (app info, configuration detail) keep their bottom edge free.
    val isTopLevelDestination = (currentRoute == Destinations.HOME &&
        (useWideLayout || selectedHomeApp == null)) ||
        currentRoute == Destinations.SYSTEM_LANGUAGES ||
        currentRoute == Destinations.CONFIGURATIONS ||
        currentRoute == Destinations.SETTINGS ||
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
        val page = topLevelRoutes.indexOf(route)
        if (page < 0) return
        if (navController.currentDestination?.route != Destinations.HOME) {
            navController.navigate(Destinations.HOME) {
                popUpTo(Destinations.HOME) { inclusive = false }
                launchSingleTop = true
            }
        }
        navigationScope.launch { pagerState.animateScrollToPage(page) }
    }

    fun navigateToDestination(destination: AppNavigationDestination) {
        navigateToTopLevel(
            when (destination) {
                AppNavigationDestination.Home -> Destinations.HOME
                AppNavigationDestination.SystemLanguages -> Destinations.SYSTEM_LANGUAGES
                AppNavigationDestination.Configurations -> Destinations.CONFIGURATIONS
                AppNavigationDestination.Settings -> Destinations.SETTINGS
                AppNavigationDestination.About -> Destinations.ABOUT
            },
        )
    }

    LaunchedEffect(currentRoute, userPreferences.rememberLastPage) {
        if (currentNavRoute == Destinations.HOME && currentRoute in topLevelRoutes) {
            AppUserPreferences.rememberPage(checkNotNull(currentRoute))
        }
    }
    // Page-level back handlers register below this fallback and keep first refusal.
    BackHandler(currentNavRoute == Destinations.HOME && pagerState.settledPage != 0) {
        navigateToTopLevel(Destinations.HOME)
    }

    // The neutral top-level facts every presenter (the Holo top tabs) shares;
    // the NavController above stays the single navigation owner.
    val topLevelNavigation = TopLevelNavigationState(
        currentDestination = displayedDestination,
        destinations = topLevelDestinations(),
        navigate = ::navigateToDestination,
        onTabDoubleTap = tabScrollCoordinator::request,
    )

    CompositionLocalProvider(LocalTopLevelNavigation provides topLevelNavigation) {
        if (isCompact) {
            // One fixed chrome host for the whole compact window: the NavHost exists
            // once inside it; the bottom dock (glass or standard) is an overlay the
            // host owns, regardless of any effect or glass preference. Under Holo
            // the host's dock branch is inert and the pages carry the top tabs.
            AppChromeHost(
                isTopLevelDestination = isTopLevelDestination,
                navigationBar = {
                        AppNavigationBar(
                            currentDestination = displayedDestination,
                            onHomeClick = { navigateToTopLevel(Destinations.HOME) },
                            onSystemLanguagesClick = { navigateToTopLevel(Destinations.SYSTEM_LANGUAGES) },
                            onConfigurationsClick = { navigateToTopLevel(Destinations.CONFIGURATIONS) },
                            onSettingsClick = { navigateToTopLevel(Destinations.SETTINGS) },
                            onAboutClick = { navigateToTopLevel(Destinations.ABOUT) },
                            onTabDoubleTap = tabScrollCoordinator::request,
                        )
                },
            ) {
                FuyaoNavHost(
                    navController = navController,
                    selectedHomeApp = selectedHomeApp,
                    onSelectHomeApp = { selectedHomeApp = it },
                    isCompact = true,
                    useWideLayout = false,
                    hasGrantedShizukuPermission = hasGrantedShizukuPermission,
                    onRequestShizukuPermission = onRequestShizukuPermission,
                    onOpenShizuku = onOpenShizuku,
                    navigateToTopLevel = ::navigateToTopLevel,
                    tabScrollCoordinator = tabScrollCoordinator,
                    pagerState = pagerState,
                    showTopLevelNavigation = isTopLevelDestination,
                    swipeEnabled = userPreferences.swipeBetweenPages && isTopLevelDestination,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        } else {
            Row(modifier = Modifier.fillMaxSize()) {
                // Holo keeps a single top tab strip even on wide windows; the
                // modern NavigationRail does not compose there (round-9 042).
                    AppNavigationRail(
                        currentDestination = displayedDestination,
                        onHomeClick = { navigateToTopLevel(Destinations.HOME) },
                        onSystemLanguagesClick = { navigateToTopLevel(Destinations.SYSTEM_LANGUAGES) },
                        onConfigurationsClick = { navigateToTopLevel(Destinations.CONFIGURATIONS) },
                        onSettingsClick = { navigateToTopLevel(Destinations.SETTINGS) },
                        onAboutClick = { navigateToTopLevel(Destinations.ABOUT) },
                        onTabDoubleTap = tabScrollCoordinator::request,
                    )
                FuyaoNavHost(
                    navController = navController,
                    selectedHomeApp = selectedHomeApp,
                    onSelectHomeApp = { selectedHomeApp = it },
                    isCompact = false,
                    useWideLayout = useWideLayout,
                    hasGrantedShizukuPermission = hasGrantedShizukuPermission,
                    onRequestShizukuPermission = onRequestShizukuPermission,
                    onOpenShizuku = onOpenShizuku,
                    navigateToTopLevel = ::navigateToTopLevel,
                    tabScrollCoordinator = tabScrollCoordinator,
                    pagerState = pagerState,
                    showTopLevelNavigation = isTopLevelDestination,
                    swipeEnabled = userPreferences.swipeBetweenPages && isTopLevelDestination,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun FuyaoNavHost(
    navController: androidx.navigation.NavHostController,
    isCompact: Boolean,
    useWideLayout: Boolean,
    selectedHomeApp: String?,
    onSelectHomeApp: (String?) -> Unit,
    hasGrantedShizukuPermission: Boolean,
    onRequestShizukuPermission: () -> Unit,
    onOpenShizuku: () -> Unit,
    navigateToTopLevel: (String) -> Unit,
    tabScrollCoordinator: TabScrollCoordinator,
    pagerState: PagerState,
    showTopLevelNavigation: Boolean,
    swipeEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Destinations.HOME,
        modifier = modifier.clipToBounds(),
    ) {
        composable(route = Destinations.HOME) {
            TopLevelPagerHost(
                state = pagerState,
                swipeEnabled = swipeEnabled,
                showNavigation = showTopLevelNavigation,
                useWideLayout = useWideLayout,
            ) { page ->
                when (topLevelRoutes[page]) {
                    Destinations.HOME -> {
                        val appListState = rememberLazyListState()
                        if ((isCompact || !useWideLayout) && selectedHomeApp != null) {
                            AppInfoScreen(appId = selectedHomeApp, navigateBack = { onSelectHomeApp(null) }, interceptNavigationBack = true)
                        } else if (isCompact || !useWideLayout) {
                            MainScreen(
                                listState = appListState,
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
                                selectedApp = selectedHomeApp,
                                onSelectApp = { onSelectHomeApp(it) },
                                onCloseApp = { onSelectHomeApp(null) },
                                mainListState = appListState,
                                tabScrollCoordinator = tabScrollCoordinator,
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
                    Destinations.SYSTEM_LANGUAGES -> {
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
                    Destinations.CONFIGURATIONS -> {
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
                    Destinations.SETTINGS -> {
                        SettingsScreen(tabScrollCoordinator = tabScrollCoordinator)
                    }
                    Destinations.ABOUT -> {
                        AboutScreen(tabScrollCoordinator = tabScrollCoordinator)
                    }
                }
            }
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

        // Accept a saved pre-pager destination after an app upgrade.
        topLevelRoutes.drop(1).forEach { route ->
            composable(route) {
                LaunchedEffect(route) { navigateToTopLevel(route) }
            }
        }
    }
}
