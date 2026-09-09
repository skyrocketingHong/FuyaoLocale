package ing.fuyaoskyrocket.applocale.ui.designsystem.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.EmojiSupportMatch
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ing.fuyaoskyrocket.applocale.ui.designsystem.*

@Composable
fun AppBadge(text: String, isFlag: Boolean, modifier: Modifier = Modifier) {
    val typography = AppUiTheme.textStyles
    val style = when {
        isFlag -> typography.pageTitle.copy(platformStyle = PlatformTextStyle(emojiSupportMatch = EmojiSupportMatch.None))
        text.length <= 2 -> typography.label.copy(fontSize = 14.sp)
        text.length == 3 -> typography.label
        else -> typography.label.copy(fontSize = 11.sp)
    }
    val content: @Composable () -> Unit = {
        Box(contentAlignment = Alignment.Center) {
            AppText(text, style = style, fontWeight = if (isFlag) FontWeight.Normal else FontWeight.Bold, maxLines = 1)
        }
    }
    if (AppUiTheme.policy.continuousLists) Box(modifier.size(AppLayout.localeBadgeSize), contentAlignment = Alignment.Center) { content() }
    else AppSurface(modifier.size(AppLayout.localeBadgeSize), shape = RoundedCornerShape(8.dp),
        color = AppUiTheme.palette.quietContainer, contentColor = AppUiTheme.palette.quietContent, content = content)
}

@Composable
fun AppSectionHeading(text: String, modifier: Modifier = Modifier) {
    val continuous = AppUiTheme.policy.continuousLists
    AppText(text, modifier.padding(vertical = 8.dp).semantics { heading() },
        style = if (continuous) AppUiTheme.textStyles.metadata else AppUiTheme.textStyles.itemTitle,
        fontWeight = if (continuous) FontWeight.Bold else null,
        color = if (continuous) AppUiTheme.palette.muted else AppUiTheme.palette.accent)
}

/** Sections remain continuous on the page background; children consume their own insets. */
@Composable
fun AppInfoSection(title: String?, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier) {
        if (title != null) AppPreferenceCategory(title)
        content()
    }
}

@Composable
fun AppInfoSectionDivider(modifier: Modifier = Modifier) {
    if (!AppUiTheme.policy.continuousLists) AppDivider(modifier.padding(horizontal = AppUiTheme.spacing.contentInset))
}

@Composable
fun appInfoTopInset(top: Dp): Dp = listTopReserve(top)

@Composable
fun AppBooleanPreference(title: String, summary: String, checked: Boolean, enabled: Boolean, onChange: (Boolean) -> Unit) {
    AppSettingsRow(title = title, summary = summary, summaryMaxLines = Int.MAX_VALUE,
        modifier = Modifier.fillMaxWidth().toggleable(checked, enabled = enabled,
            role = if (AppUiTheme.policy.toggleUsesCheckbox) Role.Checkbox else Role.Switch, onValueChange = onChange),
        trailing = { AppSwitch(checked, onCheckedChange = onChange, enabled = enabled,
            modifier = Modifier.focusProperties { canFocus = false }.clearAndSetSemantics { }) })
}
