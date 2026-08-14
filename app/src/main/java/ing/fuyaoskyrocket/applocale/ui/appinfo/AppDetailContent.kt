package ing.fuyaoskyrocket.applocale.ui.appinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ing.fuyaoskyrocket.applocale.data.system.AppIconLoader
import ing.fuyaoskyrocket.applocale.model.LocaleOption
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing
import ing.fuyaoskyrocket.applocale.ui.designsystem.readableContentWidth
import ing.fuyaoskyrocket.applocale.ui.languagepicker.languagePickerItems
import ing.fuyaoskyrocket.applocale.ui.languagepicker.LocalePickerAction
import ing.fuyaoskyrocket.applocale.ui.languagepicker.LocalePickerUiState
import ing.fuyaoskyrocket.applocale.ui.components.rememberSystemLocaleTag
import ing.fuyaoskyrocket.applocale.ui.languagepicker.rememberLanguagePickerListState

/**
 * Reusable app-detail content for both the phone detail route and the large-screen detail pane.
 *
 * Structure (LazyColumn):
 * 1. [AppIdentityHeader]
 * 2. [AppActionGroup]
 * 3. [LanguagePickerContent]
 */
@Composable
fun AppDetailContent(
    appInfoState: AppInfoUiState,
    pickerState: LocalePickerUiState,
    iconLoader: AppIconLoader,
    onPickerAction: (LocalePickerAction) -> Unit,
    onResetLocale: () -> Unit,
    onSelectLocale: (LocaleOption) -> Unit,
    onOpen: () -> Unit,
    onForceStop: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLanguagePickerListState(
        state = pickerState,
        firstVisibleItemIndex = 2,
        contentKey = appInfoState.packageName,
    )
    val systemLocaleTag = rememberSystemLocaleTag()

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = AppSpacing.screenCompact,
            vertical = AppSpacing.lg,
        ),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 1. App identity
        item(key = "header") {
            AppIdentityHeader(
                packageName = appInfoState.packageName,
                label = appInfoState.label,
                currentLocaleTag = appInfoState.currentLocaleTag,
                systemLocaleTag = systemLocaleTag,
                iconLoader = iconLoader,
                modifier = Modifier.readableContentWidth(),
            )
        }

        // 2. Quick actions
        item(key = "actions") {
            AppActionGroup(
                onOpen = onOpen,
                onForceStop = onForceStop,
                onSettings = onSettings,
                modifier = Modifier.readableContentWidth(),
            )
        }

        // 3. Language picker items (LazyListScope extension — no nested LazyColumn)
        languagePickerItems(
            state = pickerState,
            onAction = onPickerAction,
            onResetToSystemDefault = { onResetLocale() },
            onSelectLocale = { option -> onSelectLocale(option) },
            canPin = true,
            systemLocaleTag = systemLocaleTag,
            contentModifier = Modifier.readableContentWidth(),
        )
    }
}
