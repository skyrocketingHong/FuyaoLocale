package ing.fuyaoskyrocket.applocale.ui.designsystem.component

import ing.fuyaoskyrocket.applocale.ui.designsystem.lollipop.*
import ing.fuyaoskyrocket.applocale.ui.designsystem.eclair.*
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppControlFamily
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
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloSearchTextField

/**
 * Theme-aware compact search field. The Miuix style renders the native miuix
 * search bar, Material You keeps the Material 3 always-collapsed search bar,
 * and Holo renders the SearchView-style underline field from the imported
 * search textfield 9-patches. All stay permanently collapsed: filtering is
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
    when (AppUiTheme.policy.controls) {
        AppControlFamily.Material2 -> ing.fuyaoskyrocket.applocale.ui.designsystem.material2.RoundedSearchField(query, onQueryChange, placeholder, modifier)
        AppControlFamily.Miuix -> top.yukonga.miuix.kmp.basic.SearchBar(
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

        AppControlFamily.Lollipop -> LollipopSearchField(query, onQueryChange, placeholder, modifier)

        AppControlFamily.Eclair -> EclairSearchField(query, onQueryChange, placeholder, modifier)

        AppControlFamily.Holo -> HoloSearchTextField(
            value = query,
            onValueChange = onQueryChange,
            hint = placeholder,
            modifier = modifier,
        )

        AppControlFamily.Material3 -> SearchBar(
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
 * Theme-aware pull-to-refresh container: the miuix implementation for the
 * Miuix style, the Material 3 box for Material You. Holo shows NO pull-to-
 * refresh affordance (round-9 C16) — refresh lives in the Action Bar there,
 * so the container passes the content straight through.
 */
@Composable
fun AppPullToRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    when (AppUiTheme.policy.controls) {
        AppControlFamily.Material2 -> ing.fuyaoskyrocket.applocale.ui.designsystem.material2.RoundedPullToRefresh(isRefreshing, onRefresh, modifier, content)
        AppControlFamily.Miuix -> top.yukonga.miuix.kmp.basic.PullToRefresh(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = modifier,
            content = content,
        )

        AppControlFamily.Lollipop, AppControlFamily.Eclair, AppControlFamily.Holo -> androidx.compose.foundation.layout.Box(modifier = modifier) {
            content()
        }

        AppControlFamily.Material3 -> androidx.compose.material3.pulltorefresh.PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = modifier,
        ) {
            content()
        }
    }
}
