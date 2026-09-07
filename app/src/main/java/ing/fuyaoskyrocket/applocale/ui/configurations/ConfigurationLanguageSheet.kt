package ing.fuyaoskyrocket.applocale.ui.configurations

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.model.LocaleOption
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppCircularProgressIndicator
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppModalBottomSheet
import ing.fuyaoskyrocket.applocale.ui.languagepicker.LanguagePickerContent
import ing.fuyaoskyrocket.applocale.ui.languagepicker.LocalePickerAction
import ing.fuyaoskyrocket.applocale.ui.languagepicker.LocalePickerViewModel

/**
 * The single-app language chooser of a configuration detail (round-8 038).
 *
 * It reuses the shared directory, search, sorting, badges and the predictive
 * group return (036) — never a second catalog or navigation stack, and never
 * a write of its own: choosing only reports the tag back; the configuration
 * edit command and its persistence belong to the coordinator via the caller.
 * Its picker state lives in a KEYED view model instance so it cannot leak
 * query or group state into the batch or system sheets.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConfigurationLanguageSheet(
    visible: Boolean,
    packageName: String?,
    selectedLanguageTag: String?,
    onDismiss: () -> Unit,
    onLocaleSelected: (String?) -> Unit,
    viewModel: LocalePickerViewModel = hiltViewModel(key = CONFIGURATION_PICKER_KEY),
) {
    val pickerState by viewModel.uiState.collectAsStateWithLifecycle()

    // A fresh session per sheet OPENING (not per package only): reopening the
    // same package must not restore a leftover query or drilled-in group.
    var openCount by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(visible, packageName) {
        if (visible && packageName != null) {
            openCount++
            viewModel.beginSession("$packageName#$openCount", selectedLanguageTag)
        }
    }

    AppModalBottomSheet(
        visible = visible,
        onDismiss = onDismiss,
        title = stringResource(R.string.select_language),
    ) {
        // Same return order as the batch sheet (036): IME → clear the query →
        // predictive group return inside the shared content → native close.
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
                    // "Follow system" is an explicit, reversible action here —
                    // selecting it reports null and may ADD a managed entry.
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

private const val CONFIGURATION_PICKER_KEY = "configuration_language_picker"
