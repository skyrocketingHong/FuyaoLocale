package ing.fuyaoskyrocket.applocale.ui.appinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.data.system.AppIconLoader
import ing.fuyaoskyrocket.applocale.model.LocaleOption
import ing.fuyaoskyrocket.applocale.ui.components.rememberSystemLocaleTag
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppLayout
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing
import ing.fuyaoskyrocket.applocale.ui.designsystem.listBottomReserve
import ing.fuyaoskyrocket.applocale.ui.designsystem.listTopReserve
import ing.fuyaoskyrocket.applocale.ui.designsystem.readableContentWidth
import ing.fuyaoskyrocket.applocale.ui.languagepicker.LanguageGroupTransition
import ing.fuyaoskyrocket.applocale.ui.languagepicker.LanguagePickerListStates
import ing.fuyaoskyrocket.applocale.ui.languagepicker.LocalePickerAction
import ing.fuyaoskyrocket.applocale.ui.languagepicker.LocalePickerUiState
import ing.fuyaoskyrocket.applocale.ui.languagepicker.languagePickerItems

/** One scrollable identity / chips / directory flow. Search and app actions live in the toolbar. */
@Composable
fun AppDetailContent(
    appInfoState: AppInfoUiState,
    pickerState: LocalePickerUiState,
    iconLoader: AppIconLoader,
    listStates: LanguagePickerListStates,
    onPickerAction: (LocalePickerAction) -> Unit,
    onResetLocale: () -> Unit,
    onSelectLocale: (LocaleOption) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    backEnabled: Boolean = true,
) {
    val systemLocaleTag = rememberSystemLocaleTag()
    LanguageGroupTransition(
        groupId = pickerState.selectedGroupId,
        onBack = { onPickerAction(LocalePickerAction.BackToGroups) },
        backEnabled = backEnabled,
        modifier = modifier.fillMaxSize(),
    ) { groupId, interactive ->
        // Each animated layer projects its own group; never feed the outgoing
        // layer the incoming directory. Their scroll states are independently owned.
        val visibleState = pickerState.copy(selectedGroupId = groupId)
        LazyColumn(
            state = listStates.forState(visibleState),
            modifier = Modifier.fillMaxSize().consumeWindowInsets(contentPadding),
            // Round-7 028: no host-level horizontal inset — items frame themselves
            // (16dp foreground edge; the locale rows' selected backgrounds expand
            // into their own 4dp outer margin) inside one 840dp box.
            contentPadding = PaddingValues(
                top = listTopReserve(contentPadding.calculateTopPadding()),
                bottom = listBottomReserve(contentPadding.calculateBottomPadding()),
            ),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item(key = "header", contentType = "identity") {
                AppIdentityHeader(
                    packageName = appInfoState.packageName,
                    label = appInfoState.label,
                    currentLocaleTag = appInfoState.currentLocaleTag,
                    systemLocaleTag = systemLocaleTag,
                    iconLoader = iconLoader,
                    modifier = Modifier
                        .readableContentWidth()
                        .padding(horizontal = AppLayout.contentFrameMargin),
                )
            }
            languagePickerItems(
                state = visibleState,
                // The transition's interactive permit is the single gate: sort,
                // group open, pin, reset and select all route through it, so a
                // touch, key press or TalkBack action on an exiting layer is inert.
                onAction = { action -> if (interactive) onPickerAction(action) },
                onResetToSystemDefault = { if (interactive) onResetLocale() },
                onSelectLocale = { option -> if (interactive) onSelectLocale(option) },
                canPin = true,
                systemLocaleTag = systemLocaleTag,
                contentModifier = Modifier
                    .readableContentWidth()
                    .padding(horizontal = AppLayout.contentFrameMargin),
                rowModifier = Modifier
                    .readableContentWidth()
                    .padding(horizontal = AppLayout.localeRowOuterMargin),
                showSearchField = false,
            )
        }
    }
}
