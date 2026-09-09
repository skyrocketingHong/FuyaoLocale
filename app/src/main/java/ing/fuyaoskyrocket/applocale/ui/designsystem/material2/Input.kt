package ing.fuyaoskyrocket.applocale.ui.designsystem.material2

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.LocalAppContentPadding

/** A compact M2 search surface; custom inner padding avoids clipping the native 56dp text field. */
@Composable
internal fun RoundedSearchField(query: String, onQueryChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    Surface(modifier, shape = AppUiTheme.shapes.input.roundedShape(), color = AppUiTheme.palette.quietContainer) {
        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Search, null, Modifier.padding(horizontal = 12.dp).size(20.dp), tint = AppUiTheme.palette.muted)
            BasicTextField(
                value = query, onValueChange = onQueryChange, singleLine = true,
                modifier = Modifier.weight(1f).padding(vertical = 8.dp).semantics { contentDescription = placeholder },
                textStyle = MaterialTheme.typography.body1.copy(color = MaterialTheme.colors.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colors.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboard?.hide(); focus.clearFocus() }),
                decorationBox = { inner ->
                    Box {
                        if (query.isEmpty()) Text(placeholder, color = AppUiTheme.palette.muted, style = MaterialTheme.typography.body1)
                        inner()
                    }
                },
            )
            if (query.isNotEmpty()) IconButton(onClick = { onQueryChange("") }) {
                Icon(Icons.Default.Close, stringResource(R.string.clear), Modifier.size(20.dp))
            } else Spacer(Modifier.width(12.dp))
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun RoundedPullToRefresh(refreshing: Boolean, onRefresh: () -> Unit, modifier: Modifier, content: @Composable () -> Unit) {
    val state = rememberPullRefreshState(refreshing, onRefresh)
    val top = LocalAppContentPadding.current.calculateTopPadding()
    Box(modifier.pullRefresh(state)) {
        content()
        PullRefreshIndicator(refreshing, state, Modifier.align(Alignment.TopCenter).padding(top = top))
    }
}
