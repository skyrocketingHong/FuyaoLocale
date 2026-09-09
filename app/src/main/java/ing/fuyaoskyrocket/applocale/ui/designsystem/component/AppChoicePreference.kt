package ing.fuyaoskyrocket.applocale.ui.designsystem.component

import ing.fuyaoskyrocket.applocale.ui.designsystem.component.appContinuousRow
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.appListDivider
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppPreferenceCategory
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppDropdownButton
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.legacyControlState
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppControlFamily
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppLayout
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme

data class AppPreferenceOption<T : Any>(
    val value: T,
    val title: String,
    val summary: String? = null,
    val section: String? = null,
)

/** A setting with a current value and a theme-native single-choice window. */
@Composable
fun <T : Any> AppChoicePreference(
    title: String,
    selected: T,
    options: List<AppPreferenceOption<T>>,
    onSelected: (T, FocusRequester) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focus = remember { FocusRequester() }
    var visible by remember { mutableStateOf(false) }
    var pending by remember { mutableStateOf<AppPreferenceOption<T>?>(null) }
    val continuousRows = AppUiTheme.policy.continuousLists
    AppSettingsRow(
        title = title,
        summary = options.first { it.value == selected }.title,
        summaryMaxLines = Int.MAX_VALUE,
        modifier = modifier.focusRequester(focus),
        onClick = {
            pending = null
            visible = true
        },
        trailing = if (continuousRows) null else {
            {
                AppIcon(
                    imageVector = AppSymbolVector(AppSymbol.Forward),
                    contentDescription = null,
                    tint = AppUiTheme.palette.muted,
                )
            }
        },
    )
    AppModalBottomSheet(
        visible = visible,
        title = title,
        onDismiss = { visible = false },
        onDismissFinished = {
            // ASVS 2.3.1: close the outgoing window before committing the choice.
            val choice = pending
            pending = null
            if (choice != null && choice.value != selected) {
                onSelected(choice.value, focus)
            }
        },
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.7f),
            contentPadding = PaddingValues(vertical = AppSpacing.sm),
        ) {
            options.forEachIndexed { index, option ->
                if (option.section != null && (index == 0 || options[index - 1].section != option.section)) {
                    item(key = "section-${option.section}", contentType = "section") {
                        if (continuousRows) {
                            AppPreferenceCategory(option.section)
                        } else {
                            AppText(
                                text = option.section,
                                style = AppUiTheme.textStyles.label,
                                color = AppUiTheme.palette.muted,
                                modifier = Modifier
                                    .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm)
                                    .semantics { heading() },
                            )
                        }
                    }
                }
                item(key = option.value, contentType = "choice") {
                    AppLocaleChoiceRow(
                        title = option.title,
                        subtitle = option.summary,
                        selected = option.value == selected,
                        onSelect = {
                            if (visible) {
                                pending = option
                                visible = false
                            }
                        },
                        titleMaxLines = Int.MAX_VALUE,
                        subtitleMaxLines = Int.MAX_VALUE,
                        modifier = Modifier.padding(
                            horizontal = if (continuousRows) 0.dp else AppLayout.localeRowOuterMargin,
                        ),
                    )
                }
            }
        }
    }
}
