package ing.fuyaoskyrocket.applocale.ui.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppComponentDefaults
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppLayout
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemePreferences
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemeStyle
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme

/** The trailing selection glyph; the slot itself is the shared 48dp terminal slot. */
private val ChoiceCheckGlyphSize = 24.dp

/**
 * Single-select language choice row shared by the locale picker and the app-language
 * settings. The whole row is the radio target ([Role.RadioButton] with its selected
 * state); the trailing check is pure decoration. A dedicated [trailingAction] slot
 * (pin) keeps its own click target and semantics — the row never merges it away.
 *
 * Anatomy (round-7 028): the host places the row in the F−8 frame (4dp beyond the
 * shared 16dp foreground edge on each side) and the selected container paints to
 * the row's real edges; [contentPadding] then starts the badge/text at 16 absolute
 * with the terminal 48dp slot flush against the row's end, so the badge and the
 * 24dp check glyph each keep 12dp of clear space inside the background. The two
 * backends consume [contentPadding] exactly once each.
 */
@Composable
fun AppLocaleChoiceRow(
    title: String,
    subtitle: String?,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    trailingAction: (@Composable () -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(
        start = AppSpacing.md,
        top = AppSpacing.md,
        end = 0.dp,
        bottom = AppSpacing.md,
    ),
) {
    val palette = AppUiTheme.palette
    val containerColor = if (selected) {
        palette.selected
    } else {
        Color.Transparent
    }
    val contentColor = if (selected) {
        palette.onSelected
    } else {
        palette.surfaceContent
    }
    val subtitleColor = if (selected) {
        palette.onSelected
    } else {
        palette.muted
    }
    // Continuous row: no per-row card shell — only the selected state paints a
    // local rounded container, and the press ripple shares that shape.
    val rowShape = if (selected) {
        RoundedCornerShape(AppComponentDefaults.rowCornerRadius)
    } else {
        RectangleShape
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(rowShape)
            .background(color = containerColor, shape = rowShape)
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onSelect,
            ),
    ) {
        if (AppThemePreferences.style == AppThemeStyle.MIUIX) {
            // Native layout container; the click/semantics stay on the shared
            // selectable above so both themes expose exactly one radio target.
            top.yukonga.miuix.kmp.basic.BasicComponent(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = AppLayout.localeChoiceRowMinHeight),
                startAction = if (leading != null) {
                    // BasicComponent spaces its leading slot 8dp from the text;
                    // the extra 4dp here matches the Material branch's 12dp gap.
                    {
                        Box(modifier = Modifier.padding(end = AppSpacing.xs)) {
                            leading()
                        }
                    }
                } else {
                    null
                },
                endActions = if (selected || trailingAction != null) {
                    {
                        ChoiceTrailingSlots(
                            selected = selected,
                            trailingAction = trailingAction,
                        )
                    }
                } else {
                    null
                },
                insideMargin = contentPadding,
            ) {
                top.yukonga.miuix.kmp.basic.Text(
                    text = title,
                    style = AppComponentDefaults.titleStyle(),
                    color = contentColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    top.yukonga.miuix.kmp.basic.Text(
                        text = subtitle,
                        style = AppComponentDefaults.metadataStyle(),
                        color = subtitleColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = AppLayout.localeChoiceRowMinHeight)
                    .padding(contentPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (leading != null) {
                    Box(
                        modifier = Modifier.size(AppLayout.localeBadgeSize),
                        contentAlignment = Alignment.Center,
                    ) {
                        leading()
                    }
                    Spacer(Modifier.width(AppSpacing.md))
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    Text(
                        text = title,
                        style = AppComponentDefaults.titleStyle(),
                        color = contentColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = AppComponentDefaults.metadataStyle(),
                            color = subtitleColor,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                if (selected || trailingAction != null) {
                    Spacer(Modifier.width(AppSpacing.md))
                    ChoiceTrailingSlots(
                        selected = selected,
                        trailingAction = trailingAction,
                    )
                }
            }
        }
    }
}

/**
 * The right-side slots (round-5 019-A3, round-7 028). The terminal slot is a
 * unified 48dp flush with the row's end edge; the decorative check glyph is 24dp
 * centred inside it. States: unselected with an action → the action owns the
 * terminal slot (no empty check slot); selected without an action → check only;
 * selected with an action → its own 48dp slot directly beside the check (the
 * check stays the fixed terminal slot); neither → nothing. All slots are real
 * layout space — no offsets — so the unselected pin and the selected check share
 * the same centre x.
 */
@Composable
private fun RowScope.ChoiceTrailingSlots(
    selected: Boolean,
    trailingAction: (@Composable () -> Unit)?,
) {
    if (selected && trailingAction != null) {
        Box(
            modifier = Modifier.size(AppLayout.appListSelectionSlotWidth),
            contentAlignment = Alignment.Center,
        ) {
            trailingAction()
        }
    }
    if (selected) {
        Box(
            modifier = Modifier.size(AppLayout.appListSelectionSlotWidth),
            contentAlignment = Alignment.Center,
        ) {
            AppIcon(
                imageVector = AppSymbolVector(AppSymbol.Check),
                contentDescription = null,
                modifier = Modifier.size(ChoiceCheckGlyphSize),
            )
        }
    } else if (trailingAction != null) {
        Box(
            modifier = Modifier.size(AppLayout.appListSelectionSlotWidth),
            contentAlignment = Alignment.Center,
        ) {
            trailingAction()
        }
    }
}
