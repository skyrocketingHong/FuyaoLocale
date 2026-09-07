package ing.fuyaoskyrocket.applocale.ui.appinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ing.fuyaoskyrocket.applocale.data.system.AppIconLoader
import ing.fuyaoskyrocket.applocale.ui.components.AppIcon
import ing.fuyaoskyrocket.applocale.ui.components.AppLocaleStatusBadge
import ing.fuyaoskyrocket.applocale.ui.components.LocaleStatusEmphasis
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppLayout
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppText

/**
 * Compact app identity (round-5 019-A): icon, label, package name and the
 * current-language marker in one tight block — 8dp internal vertical padding,
 * 4dp gaps between the text lines. The top spacing comes from the page's
 * shared content padding, never from here. Continuous with the page
 * background; no outer rounded card.
 */
@Composable
fun AppIdentityHeader(
    packageName: String,
    label: String,
    currentLocaleTag: String?,
    systemLocaleTag: String,
    iconLoader: AppIconLoader,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(
            packageName = packageName,
            iconLoader = iconLoader,
            modifier = Modifier.size(AppLayout.appHeaderIconSize),
        )
        Column(
            modifier = Modifier
                .padding(start = AppLayout.homeListIconTextGap)
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            AppText(
                text = label,
                style = AppUiTheme.textStyles.itemTitle,
                maxLines = 2,
            )
            AppText(
                text = packageName,
                style = AppUiTheme.textStyles.metadata,
                color = AppUiTheme.palette.muted,
                maxLines = 2,
            )
            AppLocaleStatusBadge(
                localeTag = currentLocaleTag,
                systemLocaleTag = systemLocaleTag,
                emphasis = LocaleStatusEmphasis.Quiet,
            )
        }
    }
}
