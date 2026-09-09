package ing.fuyaoskyrocket.applocale.ui.designsystem.component

import ing.fuyaoskyrocket.applocale.ui.designsystem.eclair.*
import ing.fuyaoskyrocket.applocale.ui.designsystem.lollipop.LollipopSurface
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppControlFamily
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppComponentDefaults
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloCard
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloSectionSurface
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloSurface

/**
 * Theme-aware card container. The Miuix style renders the miuix squircle card and
 * Material You keeps the Material 3 card. Both delegate padding to the caller so the
 * styles lay out identically.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    containerColor: Color = AppUiTheme.palette.secondarySurface,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (AppUiTheme.policy.controls == AppControlFamily.Lollipop) {
        LollipopSurface(modifier, containerColor, AppUiTheme.palette.surfaceContent, elevation = AppUiTheme.elevation.card) {
            androidx.compose.foundation.layout.Column(content = content)
        }
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Material2) {
        androidx.compose.material.Card(modifier = modifier,
            shape = RoundedCornerShape(AppUiTheme.shapes.card.radius), backgroundColor = containerColor,
            elevation = AppUiTheme.elevation.card) { androidx.compose.foundation.layout.Column(content = content) }
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Eclair) {
        androidx.compose.foundation.layout.Column(modifier = modifier, content = content)
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Holo) {
        HoloCard(modifier = modifier, containerColor = containerColor, content = content)
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Miuix) {
        top.yukonga.miuix.kmp.basic.Card(
            modifier = modifier,
            insideMargin = PaddingValues(0.dp),
            colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
                color = containerColor,
            ),
            content = content,
        )
    } else {
        androidx.compose.material3.Card(
            modifier = modifier,
            shape = RoundedCornerShape(AppUiTheme.shapes.card.radius),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = containerColor,
            ),
            content = content,
        )
    }
}

/**
 * Theme-aware surface. The Miuix style renders the miuix surface, and Material You
 * keeps the Material 3 surface. A null [shape] leaves each backend at its own
 * native default (the miuix squircle / MaterialTheme.shapes.medium).
 */
@Composable
fun AppSurface(
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    color: Color = AppUiTheme.palette.surface,
    contentColor: Color = AppUiTheme.palette.surfaceContent,
    content: @Composable () -> Unit,
) {
    if (AppUiTheme.policy.controls == AppControlFamily.Lollipop) {
        LollipopSurface(modifier, color, contentColor, shape ?: RoundedCornerShape(AppUiTheme.shapes.card.radius), content = content)
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Material2) {
        androidx.compose.material.Surface(modifier = modifier,
            shape = shape ?: RoundedCornerShape(AppUiTheme.shapes.section.radius),
            color = color, contentColor = contentColor, content = content)
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Eclair) {
        Box(modifier = modifier, propagateMinConstraints = true) { content() }
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Holo) {
        HoloSurface(modifier = modifier, color = color, content = content)
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Miuix) {
        top.yukonga.miuix.kmp.basic.Surface(
            modifier = modifier,
            shape = shape ?: top.yukonga.miuix.kmp.basic.SurfaceDefaults.Shape,
            color = color,
            contentColor = contentColor,
            content = content,
        )
    } else {
        androidx.compose.material3.Surface(
            modifier = modifier,
            shape = shape ?: RoundedCornerShape(AppUiTheme.shapes.card.radius),
            color = color,
            contentColor = contentColor,
            content = content,
        )
    }
}

/**
 * Theme-aware rounded panel for grouping page content. Material You renders a
 * [androidx.compose.material3.Surface] with a rounded-rect shape, and the Miuix
 * style renders the native squircle card with the same radius. Each backend's
 * card provides its own content colour; children that need a specific colour
 * pass it explicitly (for example through [AppText]).
 */
@Composable
fun AppPanel(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = AppComponentDefaults.sectionCornerRadius,
    color: Color = AppUiTheme.palette.secondarySurface,
    contentColor: Color = AppUiTheme.palette.surfaceContent,
    content: @Composable () -> Unit,
) {
    if (AppUiTheme.policy.controls == AppControlFamily.Lollipop) {
        LollipopSurface(modifier, color, contentColor, RoundedCornerShape(cornerRadius), AppUiTheme.elevation.section, content)
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Material2) {
        androidx.compose.material.Surface(modifier = modifier,
            shape = RoundedCornerShape(cornerRadius), color = color, contentColor = contentColor,
            elevation = AppUiTheme.elevation.section, content = content)
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Eclair) {
        Box(modifier = modifier, propagateMinConstraints = true) { content() }
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Holo) {
        HoloSurface(modifier = modifier, color = color, content = content)
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Miuix) {
        top.yukonga.miuix.kmp.basic.Card(
            modifier = modifier,
            cornerRadius = cornerRadius,
            insideMargin = PaddingValues(0.dp),
            colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
                color = color,
                contentColor = contentColor,
            ),
        ) {
            Box(propagateMinConstraints = true) {
                content()
            }
        }
    } else {
        androidx.compose.material3.Surface(
            modifier = modifier,
            shape = RoundedCornerShape(cornerRadius),
            color = color,
            contentColor = contentColor,
        ) {
            content()
        }
    }
}

/** Finite section roles for grouped page containers (round-5 020-A). */
enum class AppSectionRole {
    /** A group of related settings/options; the workhorse settings container. */
    Group,

    /** Brand, read-only intro, or feature description — informational, never selection-coloured. */
    Info,

    /** Destructive/error-adjacent notices; reserved for real error semantics. */
    Warning,
}

/**
 * The role-scoped section container (round-5 020-A): each backend maps the role
 * to its OWN native container — Material uses its scheme containers with the
 * theme shape; miuix uses the native Card default colours and corner radius for
 * the neutral roles and its error containers for [AppSectionRole.Warning].
 * Pages stop passing palette.selected for informational containers here.
 */
@Composable
fun AppSectionSurface(
    role: AppSectionRole,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    if (AppUiTheme.policy.controls == AppControlFamily.Lollipop) {
        val warning = role == AppSectionRole.Warning
        LollipopSurface(modifier, if (warning) AppUiTheme.palette.error else AppUiTheme.palette.secondarySurface,
            if (warning) AppUiTheme.palette.onError else AppUiTheme.palette.surfaceContent,
            shape = RoundedCornerShape(AppUiTheme.shapes.section.radius), elevation = AppUiTheme.elevation.section, content = content)
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Material2) {
        val warning = role == AppSectionRole.Warning
        androidx.compose.material.Surface(modifier = modifier,
            shape = RoundedCornerShape(AppUiTheme.shapes.section.radius),
            color = if (warning) AppUiTheme.palette.error else AppUiTheme.palette.secondarySurface,
            contentColor = if (warning) AppUiTheme.palette.onError else AppUiTheme.palette.foreground,
            elevation = AppUiTheme.elevation.section, content = content)
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Eclair) {
        Box(modifier = modifier, propagateMinConstraints = true) { content() }
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Holo) {
        HoloSectionSurface(role = role, modifier = modifier, content = content)
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Miuix) {
        val colors = when (role) {
            AppSectionRole.Group,
            AppSectionRole.Info,
            -> top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors()

            AppSectionRole.Warning -> top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
                color = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.errorContainer,
                contentColor = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.onErrorContainer,
            )
        }
        top.yukonga.miuix.kmp.basic.Card(
            modifier = modifier,
            insideMargin = PaddingValues(0.dp),
            colors = colors,
        ) {
            Box(propagateMinConstraints = true) {
                content()
            }
        }
    } else {
        val scheme = androidx.compose.material3.MaterialTheme.colorScheme
        val (container, contentColor) = when (role) {
            AppSectionRole.Group -> scheme.surfaceContainerLow to scheme.onSurface
            AppSectionRole.Info -> scheme.surfaceContainer to scheme.onSurface
            AppSectionRole.Warning -> scheme.errorContainer to scheme.onErrorContainer
        }
        androidx.compose.material3.Surface(
            modifier = modifier,
            shape = RoundedCornerShape(AppComponentDefaults.sectionCornerRadius),
            color = container,
            contentColor = contentColor,
        ) {
            content()
        }
    }
}
