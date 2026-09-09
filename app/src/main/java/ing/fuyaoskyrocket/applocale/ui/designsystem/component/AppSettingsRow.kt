package ing.fuyaoskyrocket.applocale.ui.designsystem.component

import ing.fuyaoskyrocket.applocale.ui.designsystem.lollipop.*
import ing.fuyaoskyrocket.applocale.ui.designsystem.eclair.*
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppControlFamily
import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppComponentDefaults
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloSettingsRow
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Theme-aware settings row. The Miuix style renders the native miuix
 * [top.yukonga.miuix.kmp.basic.BasicComponent] content-slot overload with
 * theme-aware text for title and summary; the other styles keep the Material 3
 * list item. Both styles honour the same line limits and explicit colours.
 */
@OptIn(androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
fun AppSettingsRow(
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    titleMaxLines: Int = 1,
    summaryMaxLines: Int = 2,
    titleColor: Color = Color.Unspecified,
    summaryColor: Color = Color.Unspecified,
    onClick: (() -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    if (AppUiTheme.policy.controls == AppControlFamily.Material2) {
        androidx.compose.material.ListItem(
            modifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier,
            icon = leading, trailing = trailing, singleLineSecondaryText = summaryMaxLines == 1,
            text = { androidx.compose.material.Text(title, style = AppUiTheme.textStyles.itemTitle,
                color = if (titleColor == Color.Unspecified) AppUiTheme.palette.foreground else titleColor,
                maxLines = titleMaxLines, overflow = TextOverflow.Ellipsis) },
            secondaryText = summary?.let { text -> { androidx.compose.material.Text(text,
                style = AppUiTheme.textStyles.body,
                color = if (summaryColor == Color.Unspecified) AppUiTheme.palette.muted else summaryColor,
                maxLines = summaryMaxLines, overflow = TextOverflow.Ellipsis) } },
        )
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Lollipop) {
        LollipopSettingsRow(title, modifier, summary, titleMaxLines, summaryMaxLines, titleColor, summaryColor, onClick, leading, trailing)
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Eclair) {
        EclairSettingsRow(title, modifier, summary, titleMaxLines, summaryMaxLines, titleColor, summaryColor, onClick, leading, trailing)
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Holo) {
        HoloSettingsRow(
            title = title,
            modifier = modifier,
            summary = summary,
            titleMaxLines = titleMaxLines,
            summaryMaxLines = summaryMaxLines,
            titleColor = titleColor,
            summaryColor = summaryColor,
            onClick = onClick,
            leading = leading,
            trailing = trailing,
        )
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Miuix) {
        // The text overload of BasicComponent cannot limit line counts, so the
        // content-slot overload hosts miuix Text with the shared text roles.
        val resolvedTitleColor = if (titleColor == Color.Unspecified) {
            MiuixTheme.colorScheme.onSurface
        } else {
            titleColor
        }
        val resolvedSummaryColor = if (summaryColor == Color.Unspecified) {
            MiuixTheme.colorScheme.onSurfaceVariantSummary
        } else {
            summaryColor
        }
        top.yukonga.miuix.kmp.basic.BasicComponent(
            modifier = modifier,
            insideMargin = androidx.compose.foundation.layout.PaddingValues(
                horizontal = AppUiTheme.spacing.contentInset, vertical = androidx.compose.ui.unit.Dp(16f)),
            startAction = leading,
            endActions = if (trailing != null) {
                { trailing() }
            } else {
                null
            },
            onClick = onClick,
        ) {
            top.yukonga.miuix.kmp.basic.Text(
                text = title,
                style = AppComponentDefaults.titleStyle(),
                color = resolvedTitleColor,
                maxLines = titleMaxLines,
                overflow = TextOverflow.Ellipsis,
            )
            if (summary != null) {
                top.yukonga.miuix.kmp.basic.Text(
                    text = summary,
                    style = AppComponentDefaults.bodyStyle(),
                    color = resolvedSummaryColor,
                    maxLines = summaryMaxLines,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    } else {
        ListItem(
            headlineContent = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (titleColor == Color.Unspecified) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        titleColor
                    },
                    maxLines = titleMaxLines,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            supportingContent = summary?.let {
                {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (summaryColor == Color.Unspecified) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            summaryColor
                        },
                        maxLines = summaryMaxLines,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            },
            leadingContent = leading,
            trailingContent = trailing,
            modifier = modifier.let { base ->
                if (onClick != null) base.clickable(onClick = onClick) else base
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}
