package ing.fuyaoskyrocket.applocale.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Deselect
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ing.fuyaoskyrocket.applocale.R

/**
 * Contextual top app bar shown during multi-selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionTopAppBar(
    selectedCount: Int,
    onClose: () -> Unit,
    onSelectAll: () -> Unit,
    onClear: () -> Unit,
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.exit_selection),
                )
            }
        },
        title = {
            AppTopAppBarTitle(title = stringResource(R.string.selected_count, selectedCount))
        },
        actions = {
            IconButton(onClick = onSelectAll) {
                Icon(
                    imageVector = Icons.Outlined.SelectAll,
                    contentDescription = stringResource(R.string.select_all),
                )
            }
            IconButton(onClick = onClear) {
                Icon(
                    imageVector = Icons.Outlined.Deselect,
                    contentDescription = stringResource(R.string.clear_selection),
                )
            }
        },
    )
}
