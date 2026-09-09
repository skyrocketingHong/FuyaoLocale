package ing.fuyaoskyrocket.applocale.ui.designsystem.holo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme

/**
 * Holo-era containers are FLAT: Theme.Holo has no card or rounded-panel
 * surfaces. Every container role renders an unshaped region on the theme
 * background — grouping comes from list dividers and category headers, not
 * boxes. Callers passing modern shape/radius arguments are migrated at their
 * call sites (round-9 044); these branches never silently draw a rounded
 * modern shell.
 */
@Composable
internal fun HoloCard(
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color = AppUiTheme.palette.background,
    content: @Composable ColumnScope.() -> Unit,
) {
    androidx.compose.foundation.layout.Column(modifier = modifier.holoFlatBackground(containerColor)) {
        content()
    }
}

@Composable
internal fun HoloSurface(
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = AppUiTheme.palette.background,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier.holoFlatBackground(color)) {
        content()
    }
}

/**
 * The role mapping for Holo flat sections: Group/Info are plain background
 * (their grouping is drawn by dividers/headers), Warning keeps the holo red
 * text emphasis inside a plain region.
 */
@Composable
internal fun HoloSectionSurface(
    role: ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSectionRole,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val background = when (role) {
        ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSectionRole.Group,
        ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSectionRole.Info,
        -> AppUiTheme.palette.background

        ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSectionRole.Warning ->
            AppUiTheme.palette.background
    }
    Box(modifier = modifier.holoFlatBackground(background)) {
        content()
    }
}

/** Let versioned window gradients remain continuous beneath ordinary flat groups. */
@Composable
private fun Modifier.holoFlatBackground(color: androidx.compose.ui.graphics.Color): Modifier =
    if (LocalHoloWindowBackground.current != null && color == AppUiTheme.palette.background) {
        this
    } else {
        background(color)
    }
