package ing.fuyaoskyrocket.applocale.ui.designsystem.component

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemePreferences
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemeStyle

/**
 * Theme-aware compact search field. The Miuix style renders the native miuix search bar
 * (a capsule with the HyperOS search and clear affordances), and Material You keeps the
 * Material 3 always-collapsed search bar. Both stay permanently collapsed: filtering is
 * live, so the expanded suggestion surface is never used.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    if (AppThemePreferences.style == AppThemeStyle.MIUIX) {
        top.yukonga.miuix.kmp.basic.SearchBar(
            inputField = {
                top.yukonga.miuix.kmp.basic.InputField(
                    query = query,
                    onQueryChange = onQueryChange,
                    onSearch = {},
                    expanded = false,
                    onExpandedChange = {},
                    label = placeholder,
                )
            },
            expanded = false,
            onExpandedChange = {},
            modifier = modifier,
        ) {}
    } else {
        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = query,
                    onQueryChange = onQueryChange,
                    onSearch = { onQueryChange(it) },
                    expanded = false,
                    onExpandedChange = {},
                    enabled = true,
                    placeholder = { Text(placeholder) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = if (query.isNotEmpty()) {
                        {
                            androidx.compose.material3.IconButton(
                                onClick = { onQueryChange("") },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                )
                            }
                        }
                    } else {
                        null
                    },
                )
            },
            expanded = false,
            onExpandedChange = {},
            // Scaffold and TopAppBar own the system inset; applying it again here
            // recreates the empty strip that used to appear above the search field.
            windowInsets = WindowInsets(0, 0, 0, 0),
            modifier = modifier,
        ) {}
    }
}

/**
 * Theme-aware pull-to-refresh container: the miuix implementation for the Miuix
 * style and the Material 3 box for the other styles.
 */
@Composable
fun AppPullToRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    if (AppThemePreferences.style == AppThemeStyle.MIUIX) {
        top.yukonga.miuix.kmp.basic.PullToRefresh(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = modifier,
            content = content,
        )
    } else {
        androidx.compose.material3.pulltorefresh.PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = modifier,
        ) {
            content()
        }
    }
}
