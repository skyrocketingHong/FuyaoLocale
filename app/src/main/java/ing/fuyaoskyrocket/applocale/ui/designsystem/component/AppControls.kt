package ing.fuyaoskyrocket.applocale.ui.designsystem.component

import ing.fuyaoskyrocket.applocale.ui.designsystem.lollipop.*
import ing.fuyaoskyrocket.applocale.ui.designsystem.eclair.*
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppControlFamily
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemeStyle
import ing.fuyaoskyrocket.applocale.ui.designsystem.material.ExpressiveButton
import ing.fuyaoskyrocket.applocale.ui.designsystem.material.ExpressiveButtonKind
import ing.fuyaoskyrocket.applocale.ui.designsystem.material.ExpressiveIconButton
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloActionButton
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloBorderlessButton
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloButton
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloCheckbox
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloCircularProgress
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloLinearIndeterminateProgress
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloRadioButton
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloSwitch
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.LocalHoloContentColor
import ing.fuyaoskyrocket.applocale.ui.designsystem.material.materialSymbolVector
import ing.fuyaoskyrocket.applocale.ui.designsystem.miuix.miuixSymbolVector
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.theme.LocalContentColor as MiuixLocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Theme-aware controls. Dispatch follows the rendering provider's style
 * (exhaustive over [AppThemeStyle]): the Miuix style delegates to the native
 * miuix widgets, Material You keeps Material 3, and Holo ICS renders through
 * the fixed-era holo backend with its imported stateful assets.
 */

@Composable
fun AppIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    when (AppUiTheme.policy.controls) {
        AppControlFamily.Miuix -> {
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
        }

        AppControlFamily.Lollipop -> LollipopActionButton(onClick, modifier, enabled, content)

        AppControlFamily.Eclair -> EclairButtonFrame(onClick, modifier, enabled) { content() }

        AppControlFamily.Holo -> HoloActionButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            glyph = content,
        )

        AppControlFamily.Material2 -> androidx.compose.material.IconButton(onClick = onClick, modifier = modifier, enabled = enabled, content = content)

        AppControlFamily.Material3 -> if (AppUiTheme.motion.feedback == ing.fuyaoskyrocket.applocale.ui.designsystem.TouchFeedback.ShapeMorph) {
            ExpressiveIconButton(onClick, modifier, enabled, content)
        } else androidx.compose.material3.IconButton(
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
    when (AppUiTheme.policy.controls) {
        AppControlFamily.Miuix -> {
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
        }

        AppControlFamily.Lollipop -> LollipopButton(text, onClick, modifier, enabled, borderless = true)

        AppControlFamily.Eclair -> EclairButton(text, onClick, modifier, enabled)

        AppControlFamily.Holo -> HoloBorderlessButton(
            text = text,
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            // The Small borderless variant is the dialog/button-bar text size.
            small = true,
        )

        AppControlFamily.Material2 -> androidx.compose.material.TextButton(onClick = onClick, modifier = modifier, enabled = enabled) { androidx.compose.material.Text(text) }

        AppControlFamily.Material3 -> if (AppUiTheme.motion.feedback == ing.fuyaoskyrocket.applocale.ui.designsystem.TouchFeedback.ShapeMorph) {
            ExpressiveButton(text, onClick, modifier, enabled, ExpressiveButtonKind.Text)
        } else androidx.compose.material3.TextButton(
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
    when (AppUiTheme.policy.controls) {
        AppControlFamily.Miuix -> top.yukonga.miuix.kmp.basic.Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.buttonColorsPrimary(),
        ) {
            top.yukonga.miuix.kmp.basic.Text(text = text)
        }

        AppControlFamily.Lollipop -> LollipopButton(text, onClick, modifier, enabled)

        AppControlFamily.Eclair -> EclairButton(text, onClick, modifier, enabled)

        AppControlFamily.Holo -> HoloButton(
            text = text,
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
        )

        AppControlFamily.Material2 -> androidx.compose.material.Button(onClick = onClick, modifier = modifier, enabled = enabled) { androidx.compose.material.Text(text) }

        AppControlFamily.Material3 -> if (AppUiTheme.motion.feedback == ing.fuyaoskyrocket.applocale.ui.designsystem.TouchFeedback.ShapeMorph) {
            ExpressiveButton(text, onClick, modifier, enabled, ExpressiveButtonKind.Filled)
        } else androidx.compose.material3.Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
        ) {
            Text(text = text)
        }
    }
}

/** Filled tonal button; miuix maps it to the secondary filled colours, Holo to the standard button. */
@Composable
fun AppFilledTonalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    when (AppUiTheme.policy.controls) {
        AppControlFamily.Miuix -> top.yukonga.miuix.kmp.basic.Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.buttonColors(),
        ) {
            top.yukonga.miuix.kmp.basic.Text(text = text)
        }

        AppControlFamily.Lollipop -> LollipopButton(text, onClick, modifier, enabled)

        AppControlFamily.Eclair -> EclairButton(text, onClick, modifier, enabled)

        AppControlFamily.Holo -> HoloButton(
            text = text,
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
        )

        AppControlFamily.Material2 -> androidx.compose.material.Button(onClick = onClick, modifier = modifier, enabled = enabled,
            colors = androidx.compose.material.ButtonDefaults.buttonColors(
                backgroundColor = AppUiTheme.palette.quietContainer, contentColor = AppUiTheme.palette.foreground,
            )) { androidx.compose.material.Text(text) }

        AppControlFamily.Material3 -> if (AppUiTheme.motion.feedback == ing.fuyaoskyrocket.applocale.ui.designsystem.TouchFeedback.ShapeMorph) {
            ExpressiveButton(text, onClick, modifier, enabled, ExpressiveButtonKind.Tonal)
        } else androidx.compose.material3.FilledTonalButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
        ) {
            Text(text = text)
        }
    }
}

/** Outlined button; miuix renders a transparent button with a squircle border, Holo the standard button. */
@Composable
fun AppOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    when (AppUiTheme.policy.controls) {
        AppControlFamily.Miuix -> top.yukonga.miuix.kmp.basic.Button(
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

        AppControlFamily.Lollipop -> LollipopButton(text, onClick, modifier, enabled)

        AppControlFamily.Eclair -> EclairButton(text, onClick, modifier, enabled)

        AppControlFamily.Holo -> HoloButton(
            text = text,
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
        )

        AppControlFamily.Material2 -> androidx.compose.material.OutlinedButton(onClick = onClick, modifier = modifier, enabled = enabled) { androidx.compose.material.Text(text) }

        AppControlFamily.Material3 -> if (AppUiTheme.motion.feedback == ing.fuyaoskyrocket.applocale.ui.designsystem.TouchFeedback.ShapeMorph) {
            ExpressiveButton(text, onClick, modifier, enabled, ExpressiveButtonKind.Outlined)
        } else androidx.compose.material3.OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
        ) {
            Text(text = text)
        }
    }
}

/** Filled tonal icon button; miuix paints the secondary-container squircle, Holo a plain action button. */
@Composable
fun AppFilledTonalIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    when (AppUiTheme.policy.controls) {
        AppControlFamily.Miuix -> {
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
        }

        AppControlFamily.Lollipop -> LollipopActionButton(onClick, modifier, enabled, content)

        AppControlFamily.Eclair -> EclairButtonFrame(onClick, modifier, enabled) { content() }

        AppControlFamily.Holo -> HoloActionButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            glyph = content,
        )

        AppControlFamily.Material2 -> ing.fuyaoskyrocket.applocale.ui.designsystem.material2.RoundedIconButton(onClick, modifier, enabled, outlined = false, content)

        AppControlFamily.Material3 -> androidx.compose.material3.FilledTonalIconButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            content = content,
        )
    }
}

/** Outlined icon button; miuix renders a transparent squircle with a border, Holo a plain action button. */
@Composable
fun AppOutlinedIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    when (AppUiTheme.policy.controls) {
        AppControlFamily.Miuix -> {
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
        }

        AppControlFamily.Lollipop -> LollipopActionButton(onClick, modifier, enabled, content)

        AppControlFamily.Eclair -> EclairButtonFrame(onClick, modifier, enabled) { content() }

        AppControlFamily.Holo -> HoloActionButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            glyph = content,
        )

        AppControlFamily.Material2 -> ing.fuyaoskyrocket.applocale.ui.designsystem.material2.RoundedIconButton(onClick, modifier, enabled, outlined = true, content)

        AppControlFamily.Material3 -> androidx.compose.material3.OutlinedIconButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            content = content,
        )
    }
}

/**
 * Theme-aware icon. Business vector imagery renders through the active
 * backend's native icon control; framework NAMED glyphs must go through
 * [AppSymbolIcon] instead so the Holo backend can resolve its era bitmaps.
 */
@Composable
fun AppIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
) {
    when (AppUiTheme.policy.controls) {
        AppControlFamily.Miuix -> if (tint == Color.Unspecified) {
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

        AppControlFamily.Lollipop -> Image(imageVector, contentDescription, modifier, colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(tint.takeOrElse { LocalLollipopContentColor.current.takeOrElse { LocalLollipopColors.current.foreground } }))

        AppControlFamily.Eclair -> Image(imageVector, contentDescription, modifier, colorFilter = if (tint == Color.Unspecified) null else androidx.compose.ui.graphics.ColorFilter.tint(tint))

        AppControlFamily.Holo -> Image(
            imageVector = imageVector,
            contentDescription = contentDescription,
            modifier = modifier,
            colorFilter = if (tint == Color.Unspecified) {
                null
            } else {
                androidx.compose.ui.graphics.ColorFilter.tint(
                    tint.takeOrElse { LocalHoloContentColor.current },
                )
            },
        )

        AppControlFamily.Material2 -> androidx.compose.material.Icon(imageVector = imageVector, contentDescription = contentDescription, modifier = modifier,
            tint = tint.takeOrElse { androidx.compose.material.LocalContentColor.current.copy(alpha = androidx.compose.material.LocalContentAlpha.current) })

        AppControlFamily.Material3 -> if (tint == Color.Unspecified) {
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

/**
 * The named-symbol icon entry: each backend resolves the symbol to its native
 * asset — Material vectors, miuix glyphs, or the imported AOSP bitmaps under
 * Holo (which apply no tint: they are pre-coloured era assets).
 */
@Composable
fun AppSymbolIcon(
    symbol: AppSymbol,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    /** Modern-backend tint; the pre-coloured era bitmaps ignore it. */
    tint: Color = Color.Unspecified,
) {
    when (AppUiTheme.policy.controls) {
        AppControlFamily.Miuix -> if (tint == Color.Unspecified) {
            top.yukonga.miuix.kmp.basic.Icon(
                imageVector = symbol.miuixSymbolVector(),
                contentDescription = contentDescription,
                modifier = modifier.size(24.dp),
            )
        } else {
            top.yukonga.miuix.kmp.basic.Icon(
                imageVector = symbol.miuixSymbolVector(),
                contentDescription = contentDescription,
                modifier = modifier.size(24.dp),
                tint = tint,
            )
        }

        AppControlFamily.Lollipop -> LollipopSymbolIcon(symbol, contentDescription, modifier)

        AppControlFamily.Eclair -> EclairSymbolIcon(symbol, contentDescription, modifier)

        AppControlFamily.Holo -> ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloSymbolIcon(
            symbol = symbol,
            contentDescription = contentDescription,
            modifier = modifier,
        )

        AppControlFamily.Material2 -> androidx.compose.material.Icon(imageVector = symbol.materialSymbolVector(), contentDescription = contentDescription,
            modifier = modifier.size(AppUiTheme.icons.actionSize),
            tint = tint.takeOrElse { androidx.compose.material.LocalContentColor.current.copy(alpha = androidx.compose.material.LocalContentAlpha.current) })

        AppControlFamily.Material3 -> if (tint == Color.Unspecified) {
            androidx.compose.material3.Icon(
                imageVector = symbol.materialSymbolVector(),
                contentDescription = contentDescription,
                modifier = modifier.size(24.dp),
            )
        } else {
            androidx.compose.material3.Icon(
                imageVector = symbol.materialSymbolVector(),
                contentDescription = contentDescription,
                modifier = modifier.size(24.dp),
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
    if (AppUiTheme.policy.toggleUsesCheckbox) {
        AppCheckbox(checked, onCheckedChange, modifier, enabled)
        return
    }
    when (AppUiTheme.policy.controls) {
        AppControlFamily.Miuix -> {
            // miuix Switch accepts a nullable callback and installs no toggleable for
            // null, so read-only rows keep passing null through instead of an empty
            // lambda that would swallow the click meant for the whole row.
            top.yukonga.miuix.kmp.basic.Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = modifier,
                enabled = enabled,
            )
        }

        AppControlFamily.Lollipop -> LollipopSwitch(checked, onCheckedChange, modifier, enabled)

        AppControlFamily.Eclair -> EclairCheckbox(checked, onCheckedChange, modifier, enabled)

        AppControlFamily.Holo -> HoloSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            enabled = enabled,
        )

        AppControlFamily.Material2 -> androidx.compose.material.Switch(checked = checked, onCheckedChange = onCheckedChange, modifier = modifier, enabled = enabled)

        AppControlFamily.Material3 -> ing.fuyaoskyrocket.applocale.ui.designsystem.material.DraggableMaterialSwitch(
            checked, onCheckedChange, modifier, enabled,
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
    when (AppUiTheme.policy.controls) {
        AppControlFamily.Miuix -> {
            // miuix Checkbox only reports clicks; toggle the current state instead of
            // always reporting true. A null callback stays null (no toggleable).
            top.yukonga.miuix.kmp.basic.Checkbox(
                state = androidx.compose.ui.state.ToggleableState(checked),
                onClick = onCheckedChange?.let { callback -> { callback(!checked) } },
                modifier = modifier,
                enabled = enabled,
            )
        }

        AppControlFamily.Lollipop -> LollipopCheckbox(checked, onCheckedChange, modifier, enabled)

        AppControlFamily.Eclair -> EclairCheckbox(checked, onCheckedChange, modifier, enabled)

        AppControlFamily.Holo -> HoloCheckbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            enabled = enabled,
        )

        AppControlFamily.Material2 -> androidx.compose.material.Checkbox(checked = checked, onCheckedChange = onCheckedChange, modifier = modifier, enabled = enabled)

        AppControlFamily.Material3 -> androidx.compose.material3.Checkbox(
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
    when (AppUiTheme.policy.controls) {
        AppControlFamily.Miuix -> top.yukonga.miuix.kmp.basic.RadioButton(
            selected = selected,
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
        )

        AppControlFamily.Lollipop -> LollipopRadioButton(selected, onClick, modifier, enabled)

        AppControlFamily.Eclair -> EclairRadioButton(selected, onClick, modifier, enabled)

        AppControlFamily.Holo -> HoloRadioButton(
            selected = selected,
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
        )

        AppControlFamily.Material2 -> androidx.compose.material.RadioButton(selected = selected, onClick = onClick, modifier = modifier, enabled = enabled)

        AppControlFamily.Material3 -> androidx.compose.material3.RadioButton(
            selected = selected,
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
        )
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppCircularProgressIndicator(
    modifier: Modifier = Modifier,
    strokeWidth: androidx.compose.ui.unit.Dp? = null,
) {
    when (AppUiTheme.policy.controls) {
        AppControlFamily.Miuix -> top.yukonga.miuix.kmp.basic.CircularProgressIndicator(
            modifier = modifier,
            strokeWidth = strokeWidth
                ?: top.yukonga.miuix.kmp.basic.ProgressIndicatorDefaults.DefaultCircularProgressIndicatorStrokeWidth,
        )

        AppControlFamily.Lollipop -> LollipopProgress(modifier)

        AppControlFamily.Eclair -> EclairProgress(modifier)

        AppControlFamily.Holo -> HoloCircularProgress(modifier = modifier)

        AppControlFamily.Material2 -> androidx.compose.material.CircularProgressIndicator(modifier = modifier,
            strokeWidth = strokeWidth ?: androidx.compose.material.ProgressIndicatorDefaults.StrokeWidth)

        AppControlFamily.Material3 ->
            if (AppUiTheme.motion.feedback == ing.fuyaoskyrocket.applocale.ui.designsystem.TouchFeedback.ShapeMorph) {
                androidx.compose.material3.LoadingIndicator(modifier = modifier)
            } else if (strokeWidth != null) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = modifier,
                    strokeWidth = strokeWidth,
                )
            } else {
                androidx.compose.material3.CircularProgressIndicator(modifier = modifier)
            }
    }
}

@Composable
fun AppLinearProgressIndicator(
    modifier: Modifier = Modifier,
) {
    when (AppUiTheme.policy.controls) {
        AppControlFamily.Miuix ->
            top.yukonga.miuix.kmp.basic.LinearProgressIndicator(modifier = modifier)

        AppControlFamily.Lollipop -> LollipopProgress(modifier, horizontal = true)

        AppControlFamily.Eclair -> EclairProgress(modifier, horizontal = true)

        AppControlFamily.Holo -> HoloLinearIndeterminateProgress(modifier = modifier)

        AppControlFamily.Material2 -> androidx.compose.material.LinearProgressIndicator(modifier = modifier)

        AppControlFamily.Material3 ->
            androidx.compose.material3.LinearProgressIndicator(modifier = modifier)
    }
}
