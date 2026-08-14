package ing.fuyaoskyrocket.applocale.ui.languagepicker

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import ing.fuyaoskyrocket.applocale.ui.components.predictiveBackTransform
import ing.fuyaoskyrocket.applocale.ui.components.rememberPredictiveBackMotion
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing

/**
 * ModalBottomSheet for batch language setting.
 * Uses [BatchLanguagePickerViewModel] — the Composable never touches LocaleRepository.
 * Pin/unpin are disabled in batch mode.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchLanguageSheet(
    onDismiss: () -> Unit,
    onLocaleSelected: (String?) -> Unit,
    viewModel: BatchLanguagePickerViewModel = hiltViewModel(),
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val pickerState by viewModel.uiState.collectAsStateWithLifecycle()
    val displayLocaleTag = LocalConfiguration.current.locales[0].toLanguageTag()

    LaunchedEffect(displayLocaleTag) {
        viewModel.refreshDisplayLocale()
    }

    val predictiveBackMotion = rememberPredictiveBackMotion(
        enabled = pickerState.isInGroup || pickerState.query.isNotBlank(),
        onBack = {
            when {
                pickerState.isInGroup -> viewModel.onAction(LocalePickerAction.BackToGroups)
                pickerState.query.isNotBlank() -> {
                    viewModel.onAction(LocalePickerAction.QueryChanged(""))
                }
            }
        },
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .predictiveBackTransform(predictiveBackMotion),
        ) {
            Text(
                text = stringResource(R.string.select_language),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(
                    start = AppSpacing.xl,
                    end = AppSpacing.xl,
                    top = AppSpacing.sm,
                    bottom = AppSpacing.lg,
                ),
            )

            if (pickerState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LanguagePickerContent(
                    state = pickerState,
                    onAction = viewModel::onAction,
                    onResetToSystemDefault = { onLocaleSelected(null) },
                    onSelectLocale = { option: LocaleOption ->
                        onLocaleSelected(option.languageTag)
                    },
                    canPin = false,
                    modifier = Modifier.fillMaxHeight(),
                )
            }
        }
    }
}
