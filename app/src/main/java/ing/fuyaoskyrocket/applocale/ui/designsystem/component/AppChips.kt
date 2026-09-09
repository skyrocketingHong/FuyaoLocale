package ing.fuyaoskyrocket.applocale.ui.designsystem.component

import ing.fuyaoskyrocket.applocale.ui.designsystem.lollipop.*
import ing.fuyaoskyrocket.applocale.ui.designsystem.eclair.*
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppControlFamily
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppMotion
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Theme-aware chips for the filter and sort controls. The Miuix style renders
 * chip-sized miuix buttons (its design language has no separate chip control), and
 * Material You keeps the Material 3 chips.
 */

@OptIn(androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
fun AppFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
) {
    if (AppUiTheme.policy.controls == AppControlFamily.Material2) {
        androidx.compose.material.FilterChip(selected, onClick, modifier,
            leadingIcon = leadingIcon?.let { icon -> { androidx.compose.material.Icon(icon, null, Modifier.size(18.dp)) } },
            selectedIcon = null,
        ) { androidx.compose.material.Text(label) }
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Lollipop) {
        LollipopDropdownButton(label, onClick, modifier)
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Eclair) {
        EclairButton(label, onClick, modifier.semantics { this.selected = selected })
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Holo) {
        // Holo has no chip control (round-9 C10): the label renders as a
        // borderless ICS text action with the selection state on semantics
        // only; real filter surfaces use the Holo spinner menu (042/044), whose
        // checked marks come from the era radio/checkbox assets.
        androidx.compose.foundation.layout.Box(
            modifier = modifier.semantics { this.selected = selected },
        ) {
            ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloBorderlessButton(
                text = label,
                onClick = onClick,
                small = true,
            )
        }
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Miuix) {
        MiuixChip(
            onClick = onClick,
            label = label,
            modifier = modifier,
            leadingIcon = leadingIcon,
            selected = selected,
        )
    } else {
        androidx.compose.material3.FilterChip(
            selected = selected,
            onClick = onClick,
            label = { Text(label) },
            leadingIcon = if (leadingIcon != null) {
                {
                    androidx.compose.material3.Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
            } else {
                null
            },
            modifier = modifier,
        )
    }
}

@OptIn(androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
fun AppAssistChip(
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
) {
    if (AppUiTheme.policy.controls == AppControlFamily.Material2) {
        androidx.compose.material.Chip(onClick, modifier,
            leadingIcon = leadingIcon?.let { icon -> { androidx.compose.material.Icon(icon, null, Modifier.size(18.dp)) } },
        ) { androidx.compose.material.Text(label) }
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Lollipop) {
        LollipopDropdownButton(label, onClick, modifier)
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Eclair) {
        EclairButton(label, onClick, modifier)
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Holo) {
        ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloBorderlessButton(
            text = label,
            onClick = onClick,
            small = true,
            modifier = modifier,
        )
        return
    }
    if (AppUiTheme.policy.controls == AppControlFamily.Miuix) {
        MiuixChip(
            onClick = onClick,
            label = label,
            modifier = modifier,
            leadingIcon = leadingIcon,
            selected = false,
        )
    } else {
        androidx.compose.material3.AssistChip(
            onClick = onClick,
            label = { Text(label) },
            leadingIcon = if (leadingIcon != null) {
                {
                    androidx.compose.material3.Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
            } else {
                null
            },
            modifier = modifier,
        )
    }
}

/** HyperOS-style chip: a small miuix button, primary colours while selected. */
@Composable
private fun MiuixChip(
    onClick: () -> Unit,
    label: String,
    modifier: Modifier,
    leadingIcon: ImageVector?,
    selected: Boolean,
) {
    // The raw miuix button swaps its colours on recomposition; only this branch
    // can be given explicit colours, so the selection crossfade lives here while
    // the native Material chip keeps its own built-in animation.
    val containerColor by animateColorAsState(
        targetValue = if (selected) {
            MiuixTheme.colorScheme.primary
        } else {
            MiuixTheme.colorScheme.secondaryVariant
        },
        animationSpec = tween(
            durationMillis = AppMotion.StateColorMillis,
            easing = AppMotion.StateEasing,
        ),
        label = "chipContainerColor",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MiuixTheme.colorScheme.onPrimary
        } else {
            MiuixTheme.colorScheme.onSecondaryVariant
        },
        animationSpec = tween(
            durationMillis = AppMotion.StateColorMillis,
            easing = AppMotion.StateEasing,
        ),
        label = "chipContentColor",
    )
    top.yukonga.miuix.kmp.basic.Button(
        onClick = onClick,
        // Expose the selection state semantically instead of only switching colours.
        modifier = modifier.semantics { this.selected = selected },
        cornerRadius = 16.dp,
        minWidth = 0.dp,
        minHeight = 32.dp,
        colors = if (selected) {
            top.yukonga.miuix.kmp.basic.ButtonDefaults.buttonColorsPrimary(
                color = containerColor,
                contentColor = contentColor,
            )
        } else {
            top.yukonga.miuix.kmp.basic.ButtonDefaults.buttonColors(
                color = containerColor,
                contentColor = contentColor,
            )
        },
        insideMargin = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
    ) {
        if (leadingIcon != null) {
            top.yukonga.miuix.kmp.basic.Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        top.yukonga.miuix.kmp.basic.Text(
            text = label,
            // miuix body2 is 14sp — the same chip label size the Material branch
            // renders through its own labelLarge.
            style = MiuixTheme.textStyles.body2,
            maxLines = 1,
        )
    }
}
