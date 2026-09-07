package ing.fuyaoskyrocket.applocale.ui.systemlanguages

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.model.LocaleOption
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppCircularProgressIndicator
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppModalBottomSheet
import ing.fuyaoskyrocket.applocale.ui.languagepicker.LanguagePickerContent
import ing.fuyaoskyrocket.applocale.ui.languagepicker.LocalePickerAction
import ing.fuyaoskyrocket.applocale.ui.languagepicker.LocalePickerUiState
import ing.fuyaoskyrocket.applocale.ui.languagepicker.LocalePickerViewModel
import java.util.Locale

/** Adds a system language through the same directory used for per-app locales. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SystemLanguagePickerSheet(
    visible: Boolean,
    existingLocales: List<LocaleOption>,
    onDismiss: () -> Unit,
    onLocaleSelected: (LocaleOption) -> Unit,
    viewModel: LocalePickerViewModel = hiltViewModel(),
) {
    val pickerState by viewModel.uiState.collectAsStateWithLifecycle()
    val displayLocaleTag = LocalConfiguration.current.locales[0].toLanguageTag()
    val existingTags = remember(existingLocales) {
        existingLocales.mapTo(mutableSetOf()) { it.languageTag.lowercase(Locale.ROOT) }
    }
    val selectableState = remember(pickerState, existingTags) {
        pickerState.excludingLanguageTags(existingTags)
    }

    LaunchedEffect(visible, displayLocaleTag) {
        if (visible) viewModel.refreshDisplayLocale()
    }

    AppModalBottomSheet(
        visible = visible,
        onDismiss = onDismiss,
        title = stringResource(R.string.add_system_language),
    ) {
        // Round-8 036 return order, same as the batch sheet: IME → clear the
        // query → predictive group return inside LanguagePickerContent → the
        // sheet's native back-dismiss. This handler only clears a non-blank
        // query; the existing-locales projection can already collapse a removed
        // group to null so a stale gesture never commits.
        val imeVisible = WindowInsets.isImeVisible
        BackHandler(
            enabled = visible && !imeVisible && selectableState.query.isNotBlank(),
        ) {
            viewModel.onAction(LocalePickerAction.QueryChanged(""))
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
        ) {
            if (selectableState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    AppCircularProgressIndicator()
                }
            } else {
                LanguagePickerContent(
                    state = selectableState,
                    onAction = viewModel::onAction,
                    onResetToSystemDefault = {},
                    onSelectLocale = { option: LocaleOption ->
                        if (visible) onLocaleSelected(option)
                    },
                    canPin = false,
                    showSystemDefault = false,
                    backEnabled = visible && selectableState.query.isBlank(),
                    modifier = Modifier.fillMaxHeight(),
                )
            }
        }
    }
}

private fun LocalePickerUiState.excludingLanguageTags(
    excludedTags: Set<String>,
): LocalePickerUiState {
    fun isExcluded(option: LocaleOption): Boolean =
        option.languageTag.lowercase(Locale.ROOT) in excludedTags
    val groups = localeGroups.mapNotNull { group ->
        group.copy(options = group.options.filterNot(::isExcluded)).takeIf { it.options.isNotEmpty() }
    }
    val selectedGroup = selectedGroupId?.takeIf { id -> groups.any { it.id == id } }
    return copy(
        selectedGroupId = selectedGroup,
        pinnedLocales = pinnedLocales.filterNot(::isExcluded),
        systemLocales = systemLocales.filterNot(::isExcluded),
        localeGroups = groups,
    )
}
