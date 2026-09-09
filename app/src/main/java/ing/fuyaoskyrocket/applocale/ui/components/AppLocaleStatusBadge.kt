package ing.fuyaoskyrocket.applocale.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ing.fuyaoskyrocket.applocale.R

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
    ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppStatusLabel(
        text, isModified, quiet = emphasis == LocaleStatusEmphasis.Quiet, modifier = modifier,
    )
}
