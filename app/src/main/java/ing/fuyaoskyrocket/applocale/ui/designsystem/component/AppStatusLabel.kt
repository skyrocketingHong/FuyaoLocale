package ing.fuyaoskyrocket.applocale.ui.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppComponentDefaults
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppText

/** Informational emphasis independent of the source of the status. */
@Composable
fun AppStatusLabel(text: String, isModified: Boolean, quiet: Boolean = false, modifier: Modifier = Modifier) {
    if (AppUiTheme.policy.continuousLists
    ) {
        // Era status text (round-9 P04): plain metadata line, modified keeps
        // its distinction through the holo blue accent — no capsule, no dot.
        AppText(
            text = text,
            style = AppComponentDefaults.metadataStyle(),
            color = if (isModified) {
                ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme.palette.accent
            } else {
                AppUiTheme.palette.muted
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = modifier,
        )
        return
    }

    if (quiet) {
        if (isModified) {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // Decoration only; the adjacent text carries the meaning.
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(AppUiTheme.palette.accent),
                )
                // Single line, but the semantics keep the full untruncated tag text.
                AppText(
                    text = text,
                    style = AppComponentDefaults.labelStyle(),
                    color = AppUiTheme.palette.accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        } else {
            AppText(
                text = text,
                style = AppComponentDefaults.metadataStyle(),
                color = AppUiTheme.palette.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = modifier,
            )
        }
        return
    }

    val palette = AppUiTheme.palette
    // Both states are informational (020): the modified capsule rides the quiet
    // container, not the selection colour — selection colour belongs to real
    // user selection only.
    val containerColor = if (isModified) {
        palette.quietContainer
    } else {
        palette.secondarySurface
    }
    val contentColor = if (isModified) {
        palette.quietContent
    } else {
        palette.muted
    }

    Box(
        modifier = modifier
            .heightIn(min = 24.dp)
            .background(containerColor, CircleShape)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        AppText(
            text = text,
            style = AppComponentDefaults.labelStyle(),
            color = contentColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
