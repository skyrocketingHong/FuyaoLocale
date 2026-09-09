package ing.fuyaoskyrocket.applocale.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.StateFlow

/**
 * One NavController/entry owns the pager. Prefix model keys by page so identical
 * picker types on neighboring pages retain independent queries and selections.
 * Detail routes keep their existing entry-owned keys and SavedStateHandle support.
 */
@Composable
internal inline fun <reified VM : ViewModel> pageHiltViewModel(key: String? = null): VM {
    val page = LocalPagerPageScaffold.current?.page
    val pageKey = pageViewModelKey(page, key, VM::class.java.name)
    return hiltViewModel(key = pageKey)
}

internal fun pageViewModelKey(page: Int?, key: String?, modelName: String): String? =
    if (page == null) key else "main-page:$page:$modelName:${key ?: "default"}"

/** Preview data follows the host lifecycle; hidden pages still cannot handle back or UI events. */
@Composable
internal fun <T> StateFlow<T>.collectPageUiState(): State<T> = collectAsStateWithLifecycle(
    lifecycleOwner = LocalPagerStateLifecycleOwner.current ?: LocalLifecycleOwner.current,
)
