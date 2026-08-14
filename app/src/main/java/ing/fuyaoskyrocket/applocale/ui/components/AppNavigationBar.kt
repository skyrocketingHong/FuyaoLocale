package ing.fuyaoskyrocket.applocale.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks as FilledBookmarks
import androidx.compose.material.icons.filled.Home as FilledHome
import androidx.compose.material.icons.filled.Info as FilledInfo
import androidx.compose.material.icons.outlined.Bookmarks as OutlinedBookmarks
import androidx.compose.material.icons.outlined.Home as OutlinedHome
import androidx.compose.material.icons.outlined.Info as OutlinedInfo
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing

/** The three top-level destinations shared by compact and wide navigation. */
enum class AppNavigationDestination {
    Home,
    Configurations,
    About,
}

private data class AppNavigationItem(
    val destination: AppNavigationDestination,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val onClick: () -> Unit,
)

private fun appNavigationItems(
    onHomeClick: () -> Unit,
    onConfigurationsClick: () -> Unit,
    onAboutClick: () -> Unit,
): List<AppNavigationItem> = listOf(
    AppNavigationItem(
        destination = AppNavigationDestination.Home,
        labelRes = R.string.home,
        selectedIcon = Icons.Filled.FilledHome,
        unselectedIcon = Icons.Outlined.OutlinedHome,
        onClick = onHomeClick,
    ),
    AppNavigationItem(
        destination = AppNavigationDestination.Configurations,
        labelRes = R.string.navigation_configurations,
        selectedIcon = Icons.Filled.FilledBookmarks,
        unselectedIcon = Icons.Outlined.OutlinedBookmarks,
        onClick = onConfigurationsClick,
    ),
    AppNavigationItem(
        destination = AppNavigationDestination.About,
        labelRes = R.string.about,
        selectedIcon = Icons.Filled.FilledInfo,
        unselectedIcon = Icons.Outlined.OutlinedInfo,
        onClick = onAboutClick,
    ),
)

/**
 * Native Material 3 navigation for the app's top-level destinations.
 * Keeping it in one component makes the selected state and labels consistent across screens.
 */
@Composable
fun AppNavigationBar(
    currentDestination: AppNavigationDestination,
    onHomeClick: () -> Unit,
    onConfigurationsClick: () -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = appNavigationItems(
        onHomeClick = onHomeClick,
        onConfigurationsClick = onConfigurationsClick,
        onAboutClick = onAboutClick,
    )
    NavigationBar(
        modifier = modifier,
        // The scaffold extends this tonal surface behind the transparent gesture area,
        // keeping edge-to-edge immersive without visually detaching navigation items.
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
    ) {
        items.forEach { item ->
            val selected = currentDestination == item.destination
            NavigationBarItem(
                selected = selected,
                onClick = item.onClick,
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = null,
                    )
                },
                label = { Text(stringResource(item.labelRes)) },
            )
        }
    }
}

/**
 * A navigation rail replaces the bottom bar from the medium width breakpoint.
 * This leaves the larger canvas for the list-detail and configuration panes.
 */
@Composable
fun AppNavigationRail(
    currentDestination: AppNavigationDestination,
    onHomeClick: () -> Unit,
    onConfigurationsClick: () -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = appNavigationItems(
        onHomeClick = onHomeClick,
        onConfigurationsClick = onConfigurationsClick,
        onAboutClick = onAboutClick,
    )
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
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = null,
                    )
                },
                label = { Text(stringResource(item.labelRes)) },
                alwaysShowLabel = true,
            )
        }
        Spacer(modifier = Modifier.height(AppSpacing.lg))
    }
}
