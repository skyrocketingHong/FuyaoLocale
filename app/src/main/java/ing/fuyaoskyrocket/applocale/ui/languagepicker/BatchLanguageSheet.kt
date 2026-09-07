package ing.fuyaoskyrocket.applocale.ui.languagepicker

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.model.LocaleOption
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppCircularProgressIndicator
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppModalBottomSheet

/**
 * Modal bottom sheet for batch language setting.
 * Uses [LocalePickerViewModel] — the Composable never touches LocaleRepository.
 * Pin/unpin are disabled in batch mode.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BatchLanguageSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onLocaleSelected: (String?) -> Unit,
    viewModel: LocalePickerViewModel = hiltViewModel(),
) {
    val pickerState by viewModel.uiState.collectAsStateWithLifecycle()
    val displayLocaleTag = LocalConfiguration.current.locales[0].toLanguageTag()

    LaunchedEffect(visible, displayLocaleTag) {
        if (visible) viewModel.refreshDisplayLocale()
    }

    AppModalBottomSheet(
        visible = visible,
        onDismiss = onDismiss,
        title = stringResource(R.string.select_language),
    ) {
        // Round-8 036 return order: IME → clear the query → leave the group
        // (predictive, owned by LanguagePickerContent's transition) → the
        // sheet's own native back-dismiss. This plain handler therefore ONLY
        // clears a non-blank query and never exits the group; the visible gate
        // keeps it silent while the exit animation is still running.
        val imeVisible = WindowInsets.isImeVisible
        BackHandler(
            enabled = visible && !imeVisible && pickerState.query.isNotBlank(),
        ) {
            viewModel.onAction(LocalePickerAction.QueryChanged(""))
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
        ) {
            if (pickerState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    AppCircularProgressIndicator()
                }
            } else {
                LanguagePickerContent(
                    state = pickerState,
                    onAction = viewModel::onAction,
                    onResetToSystemDefault = {
                        if (visible) onLocaleSelected(null)
                    },
                    onSelectLocale = { option: LocaleOption ->
                        if (visible) onLocaleSelected(option.languageTag)
                    },
                    canPin = false,
                    backEnabled = visible && pickerState.query.isBlank(),
                    modifier = Modifier.fillMaxHeight(),
                )
            }
        }
    }
}
