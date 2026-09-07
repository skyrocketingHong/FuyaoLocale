package ing.fuyaoskyrocket.applocale.ui.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemePreferences
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemeStyle
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.theme.LocalContentColor as MiuixLocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Theme-aware controls. The Miuix style delegates to the native miuix widgets and
 * Material You keeps Material 3.
 */

@Composable
fun AppIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    if (AppThemePreferences.style == AppThemeStyle.MIUIX) {
        // miuix IconButton neither paints a background nor dims its content when
        // disabled; publish the resolved foreground through the miuix content
        // colour so its own Icon tints (and any miuix child) dim with the state.
        val contentColor = if (enabled) {
            MiuixTheme.colorScheme.onSurface
        } else {
            MiuixTheme.colorScheme.disabledOnSecondaryVariant
        }
        CompositionLocalProvider(
            MiuixLocalContentColor provides contentColor,
        ) {
            top.yukonga.miuix.kmp.basic.IconButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                content = content,
            )
        }
    } else {
        androidx.compose.material3.IconButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            content = content,
        )
    }
}

@Composable
fun AppTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    if (AppThemePreferences.style == AppThemeStyle.MIUIX) {
        // miuix TextButton defaults to a filled surface, not a bare text
        // button; map it to a transparent background with the primary text
        // colour so the hierarchy matches the Material 3 TextButton.
        top.yukonga.miuix.kmp.basic.TextButton(
            text = text,
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColors(
                color = Color.Transparent,
                disabledColor = Color.Transparent,
                textColor = MiuixTheme.colorScheme.primary,
                disabledTextColor = MiuixTheme.colorScheme.disabledOnSecondaryVariant,
            ),
        )
    } else {
        androidx.compose.material3.TextButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
        ) {
            Text(text = text)
        }
    }
}

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    if (AppThemePreferences.style == AppThemeStyle.MIUIX) {
        top.yukonga.miuix.kmp.basic.Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.buttonColorsPrimary(),
        ) {
            top.yukonga.miuix.kmp.basic.Text(text = text)
        }
    } else {
        androidx.compose.material3.Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
        ) {
            Text(text = text)
        }
    }
}

/** Filled tonal button; miuix maps it to the secondary filled button colours. */
@Composable
fun AppFilledTonalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    if (AppThemePreferences.style == AppThemeStyle.MIUIX) {
        top.yukonga.miuix.kmp.basic.Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.buttonColors(),
        ) {
            top.yukonga.miuix.kmp.basic.Text(text = text)
        }
    } else {
        androidx.compose.material3.FilledTonalButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
        ) {
            Text(text = text)
        }
    }
}

/** Outlined button; miuix renders a transparent button with a squircle border. */
@Composable
fun AppOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    if (AppThemePreferences.style == AppThemeStyle.MIUIX) {
        top.yukonga.miuix.kmp.basic.Button(
            onClick = onClick,
            modifier = modifier.squircleBorder(
                width = 1.dp,
                color = MiuixTheme.colorScheme.dividerLine,
                cornerRadius = top.yukonga.miuix.kmp.basic.ButtonDefaults.CornerRadius,
            ),
            enabled = enabled,
            colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.buttonColors(
                color = Color.Transparent,
                disabledColor = Color.Transparent,
                contentColor = MiuixTheme.colorScheme.onSurface,
                disabledContentColor = MiuixTheme.colorScheme.disabledOnSecondaryVariant,
            ),
        ) {
            top.yukonga.miuix.kmp.basic.Text(text = text)
        }
    } else {
        androidx.compose.material3.OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
        ) {
            Text(text = text)
        }
    }
}

/** Filled tonal icon button; miuix paints the secondary-container squircle. */
@Composable
fun AppFilledTonalIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    if (AppThemePreferences.style == AppThemeStyle.MIUIX) {
        val backgroundColor = if (enabled) {
            MiuixTheme.colorScheme.secondaryContainer
        } else {
            MiuixTheme.colorScheme.disabledSecondaryVariant
        }
        val contentColor = if (enabled) {
            MiuixTheme.colorScheme.onSecondaryContainer
        } else {
            MiuixTheme.colorScheme.disabledOnSecondaryVariant
        }
        CompositionLocalProvider(
            MiuixLocalContentColor provides contentColor,
        ) {
            top.yukonga.miuix.kmp.basic.IconButton(
                onClick = onClick,
                // 48dp round container: one size for both the visible circle and
                // its touch target, keeping miuix's native press and disabled colours.
                modifier = modifier,
                enabled = enabled,
                minWidth = 48.dp,
                minHeight = 48.dp,
                cornerRadius = 24.dp,
                backgroundColor = backgroundColor,
                content = content,
            )
        }
    } else {
        androidx.compose.material3.FilledTonalIconButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            content = content,
        )
    }
}

/** Outlined icon button; miuix renders a transparent squircle with a border. */
@Composable
fun AppOutlinedIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    if (AppThemePreferences.style == AppThemeStyle.MIUIX) {
        val contentColor = if (enabled) {
            MiuixTheme.colorScheme.onSurface
        } else {
            MiuixTheme.colorScheme.disabledOnSecondaryVariant
        }
        CompositionLocalProvider(
            MiuixLocalContentColor provides contentColor,
        ) {
            top.yukonga.miuix.kmp.basic.IconButton(
                onClick = onClick,
                modifier = modifier.squircleBorder(
                    width = 1.dp,
                    color = MiuixTheme.colorScheme.dividerLine,
                    cornerRadius = top.yukonga.miuix.kmp.basic.IconButtonDefaults.CornerRadius,
                ),
                enabled = enabled,
                minWidth = 48.dp,
                minHeight = 48.dp,
                cornerRadius = 24.dp,
                backgroundColor = Color.Transparent,
                content = content,
            )
        }
    } else {
        androidx.compose.material3.OutlinedIconButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            content = content,
        )
    }
}

/**
 * Theme-aware icon. The miuix widgets provide no content colour to their content, so
 * the Miuix style must resolve the tint through its own Icon composable (its default
 * is the miuix [top.yukonga.miuix.kmp.theme.LocalContentColor]); Material You keeps
 * the Material 3 icon.
 */
@Composable
fun AppIcon(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified,
) {
    if (AppThemePreferences.style == AppThemeStyle.MIUIX) {
        if (tint == androidx.compose.ui.graphics.Color.Unspecified) {
            top.yukonga.miuix.kmp.basic.Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                modifier = modifier,
            )
        } else {
            top.yukonga.miuix.kmp.basic.Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                modifier = modifier,
                tint = tint,
            )
        }
    } else {
        if (tint == androidx.compose.ui.graphics.Color.Unspecified) {
            androidx.compose.material3.Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                modifier = modifier,
            )
        } else {
            androidx.compose.material3.Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                modifier = modifier,
                tint = tint,
            )
        }
    }
}

@Composable
fun AppSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    if (AppThemePreferences.style == AppThemeStyle.MIUIX) {
        // miuix Switch accepts a nullable callback and installs no toggleable for
        // null, so read-only rows keep passing null through instead of an empty
        // lambda that would swallow the click meant for the whole row.
        top.yukonga.miuix.kmp.basic.Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            enabled = enabled,
        )
    } else {
        androidx.compose.material3.Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            enabled = enabled,
        )
    }
}

@Composable
fun AppCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    if (AppThemePreferences.style == AppThemeStyle.MIUIX) {
        // miuix Checkbox only reports clicks; toggle the current state instead of
        // always reporting true. A null callback stays null (no toggleable).
        top.yukonga.miuix.kmp.basic.Checkbox(
            state = androidx.compose.ui.state.ToggleableState(checked),
            onClick = onCheckedChange?.let { callback -> { callback(!checked) } },
            modifier = modifier,
            enabled = enabled,
        )
    } else {
        androidx.compose.material3.Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            enabled = enabled,
        )
    }
}

@Composable
fun AppRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    if (AppThemePreferences.style == AppThemeStyle.MIUIX) {
        top.yukonga.miuix.kmp.basic.RadioButton(
            selected = selected,
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
        )
    } else {
        androidx.compose.material3.RadioButton(
            selected = selected,
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
        )
    }
}

@Composable
fun AppCircularProgressIndicator(
    modifier: Modifier = Modifier,
    strokeWidth: androidx.compose.ui.unit.Dp? = null,
) {
    if (AppThemePreferences.style == AppThemeStyle.MIUIX) {
        top.yukonga.miuix.kmp.basic.CircularProgressIndicator(
            modifier = modifier,
            strokeWidth = strokeWidth
                ?: top.yukonga.miuix.kmp.basic.ProgressIndicatorDefaults.DefaultCircularProgressIndicatorStrokeWidth,
        )
    } else if (strokeWidth != null) {
        androidx.compose.material3.CircularProgressIndicator(
            modifier = modifier,
            strokeWidth = strokeWidth,
        )
    } else {
        androidx.compose.material3.CircularProgressIndicator(modifier = modifier)
    }
}

@Composable
fun AppLinearProgressIndicator(
    modifier: Modifier = Modifier,
) {
    if (AppThemePreferences.style == AppThemeStyle.MIUIX) {
        top.yukonga.miuix.kmp.basic.LinearProgressIndicator(modifier = modifier)
    } else {
        androidx.compose.material3.LinearProgressIndicator(modifier = modifier)
    }
}
