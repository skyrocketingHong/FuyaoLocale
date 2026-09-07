package ing.fuyaoskyrocket.applocale.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.data.system.AppIconLoader
import ing.fuyaoskyrocket.applocale.model.AppModel
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppComponentDefaults
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppLayout
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppCheckbox
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppText

/**
 * Continuous installed-app row: the normal state shares the page background (no
 * per-row card shell); only selection paints a local rounded container. The
 * inner row installs the only click surface (before its padding, so the full row
 * stays clickable) and carries the long-press selection affordance in every
 * theme. A non-blank [query] highlights the matching label and package-name
 * ranges without altering their text.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppListItem(
    app: AppModel,
    systemLocaleTag: String,
    iconLoader: AppIconLoader,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    query: String = "",
) {
    val palette = AppUiTheme.palette
    val contentColor = if (isSelected) palette.onSelected else palette.surfaceContent
    val supportingColor = if (isSelected) palette.onSelected else palette.muted
    // Selected rows sit on the selected container, so the highlight only bolds
    // the match instead of layering a third background colour underneath it.
    val highlightStyle = if (isSelected) {
        SpanStyle(
            color = palette.onSelected,
            fontWeight = FontWeight.Bold,
        )
    } else {
        SpanStyle(
            background = palette.selected,
            color = palette.onSelected,
        )
    }
    val highlightedLabel = remember(app.label, query, highlightStyle) {
        highlightedText(app.label, query, highlightStyle)
    }
    val highlightedPackageName = remember(app.packageName, query, highlightStyle) {
        highlightedText(app.packageName, query, highlightStyle)
    }
    // One line normally; two only where the space genuinely runs out (large
    // system font scales), matching the detail pane's stacking rule.
    val nameMaxLines = if (LocalDensity.current.fontScale > 1.3f) 2 else 1

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = AppLayout.homeListRowMinHeight)
            .background(
                color = if (isSelected) palette.selected else Color.Transparent,
                shape = RoundedCornerShape(AppComponentDefaults.rowCornerRadius),
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .then(
                if (isSelectionMode) {
                    Modifier.semantics {
                        role = Role.Checkbox
                        toggleableState = ToggleableState(isSelected)
                    }
                } else {
                    Modifier
                },
            )
            .padding(
                horizontal = AppLayout.homeListRowHorizontalPadding,
                vertical = AppLayout.homeListRowVerticalPadding,
            ),
        verticalAlignment = Alignment.Top,
    ) {
        // Fixed 48dp lead column: the app icon with at most one quiet
        // system/user marker underneath — never stacked tags.
        Column(
            modifier = Modifier.width(AppLayout.homeListIconColumnWidth),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppLayout.appListTextGap),
        ) {
            AppIcon(
                packageName = app.packageName,
                iconLoader = iconLoader,
                modifier = Modifier.size(AppLayout.homeListIconSize),
            )
            AppText(
                text = stringResource(
                    if (app.isSystemApp) R.string.system_app else R.string.user_app,
                ),
                style = AppUiTheme.textStyles.label,
                color = if (isSelected) palette.onSelected else palette.muted,
                maxLines = 1,
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = AppLayout.homeListIconTextGap),
            verticalArrangement = Arrangement.spacedBy(AppLayout.appListTextGap),
        ) {
            AppText(
                text = highlightedLabel,
                style = AppUiTheme.textStyles.itemTitle,
                color = contentColor,
                maxLines = nameMaxLines,
                overflow = TextOverflow.Ellipsis,
            )
            AppText(
                text = highlightedPackageName,
                style = AppUiTheme.textStyles.body,
                color = supportingColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            AppLocaleStatusBadge(
                localeTag = app.localeTag,
                systemLocaleTag = systemLocaleTag,
                emphasis = LocaleStatusEmphasis.Quiet,
            )
        }
        // The selection slot exists ONLY while selection mode is active: the
        // normal state hands the whole remaining width to the text columns.
        // The reflow when selection controls appear is the correct layout.
        if (isSelectionMode) {
            Box(
                modifier = Modifier.size(AppLayout.appListSelectionSlotWidth),
                contentAlignment = Alignment.Center,
            ) {
                AppCheckbox(
                    checked = isSelected,
                    onCheckedChange = null,
                )
            }
        }
    }
}
