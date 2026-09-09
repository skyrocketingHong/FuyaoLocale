package ing.fuyaoskyrocket.applocale.ui.screen


internal val topLevelRoutes = listOf(
    Destinations.HOME, Destinations.SYSTEM_LANGUAGES, Destinations.CONFIGURATIONS,
    Destinations.SETTINGS, Destinations.ABOUT,
)

internal fun restoredTopLevelRoute(rememberLastPage: Boolean, savedRoute: String): String =
    savedRoute.takeIf { rememberLastPage && it in topLevelRoutes } ?: Destinations.HOME
