package ing.fuyaoskyrocket.applocale.ui.components

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppComponentDefaults
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppText

/** How strongly the locale status is presented in its surrounding list. */
enum class LocaleStatusEmphasis {
    /** The capsule badge used by detail screens. */
    Standard,

    /**
     * Low-emphasis inline text for dense lists: follow-system is plain metadata,
     * modified is a single line with a small decorative dot — no second card-like
     * capsule per row.
     */
    Quiet,
}

/**
 * Read-only status badge describing the locale currently applied to an app.
 *
 * A null or blank [localeTag] means the app follows the system, resolved against
 * [systemLocaleTag]; a non-blank tag marks the app as modified and shows the raw
 * tag. This is a synonym of `AppModel.isModified`, not a new state, and it never
 * expresses configuration targets, installation state or write failures. The badge
 * is informational only — no clickable, Button or Chip semantics — and the flag or
 * letter badge stays the responsibility of [LocaleBadge].
 */
@Composable
fun AppLocaleStatusBadge(
    localeTag: String?,
    systemLocaleTag: String,
    modifier: Modifier = Modifier,
    emphasis: LocaleStatusEmphasis = LocaleStatusEmphasis.Standard,
) {
    val isModified = !localeTag.isNullOrBlank()
    val text = if (isModified) {
        "${stringResource(R.string.modified)} · $localeTag"
    } else if (systemLocaleTag.isBlank()) {
        stringResource(R.string.system_default)
    } else {
        stringResource(R.string.system_default_with_locale, systemLocaleTag)
    }

    if (emphasis == LocaleStatusEmphasis.Quiet) {
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
