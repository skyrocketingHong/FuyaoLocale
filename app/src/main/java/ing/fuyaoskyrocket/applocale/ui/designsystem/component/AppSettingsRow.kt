package ing.fuyaoskyrocket.applocale.ui.designsystem.component

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
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemePreferences
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemeStyle
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Theme-aware settings row. The Miuix style renders the native miuix
 * [top.yukonga.miuix.kmp.basic.BasicComponent] content-slot overload with
 * theme-aware text for title and summary; the other styles keep the Material 3
 * list item. Both styles honour the same line limits and explicit colours.
 */
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
    if (AppThemePreferences.style == AppThemeStyle.MIUIX) {
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
