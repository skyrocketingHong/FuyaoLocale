package ing.fuyaoskyrocket.applocale.ui.designsystem.component

import ing.fuyaoskyrocket.applocale.ui.designsystem.lollipop.*
import ing.fuyaoskyrocket.applocale.ui.designsystem.eclair.*
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppControlFamily
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.miuix.miuixSymbolVector

/**
 * Toolbar icon button: a 48×48dp touch slot with a 24dp icon on a transparent
 * background, shared by the modern styles so title-row actions keep one
 * geometry. Unlike [AppIconButton] it takes the icon directly — the
 * description is required so every toolbar action stays labelled for
 * accessibility.
 *
 * The Holo backend ignores the vector: named framework glyphs must use the
 * [symbol overload][AppToolbarIconButton] so the era bitmaps render; a raw
 * vector reaches Holo only as explicitly-declared business imagery.
 */
@Composable
fun AppToolbarIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    when (AppUiTheme.policy.controls) {
        AppControlFamily.Miuix -> top.yukonga.miuix.kmp.basic.IconButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            backgroundColor = Color.Transparent,
            minWidth = 48.dp,
            minHeight = 48.dp,
        ) {
            top.yukonga.miuix.kmp.basic.Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(AppUiTheme.icons.actionSize),
            )
        }

        AppControlFamily.Lollipop -> LollipopActionButton(onClick, modifier, enabled) { AppIcon(icon, contentDescription, Modifier.size(AppUiTheme.icons.actionSize)) }

        AppControlFamily.Eclair -> EclairToolbarButton(contentDescription, onClick, modifier, enabled)

        AppControlFamily.Holo ->
            // Business imagery only: framework glyphs come through the symbol
            // overload so the era bitmaps render instead of a modern vector.
            ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloActionButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
            ) {
                AppIcon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    modifier = Modifier.size(AppUiTheme.icons.actionSize),
                )
            }

        AppControlFamily.Material2, AppControlFamily.Material3 -> AppIconButton(
            onClick = onClick, modifier = modifier, enabled = enabled,
        ) { AppIcon(icon, contentDescription, Modifier.size(AppUiTheme.icons.actionSize)) }
    }
}

/**
 * The named-symbol toolbar action: 48dp slot, 24dp glyph on the modern
 * backends; the imported era bitmap inside the Widget.Holo.ActionButton
 * chrome (action-bar-height visual, ≥48dp hit area) under Holo.
 */
@Composable
fun AppToolbarIconButton(
    symbol: AppSymbol,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    when (AppUiTheme.policy.controls) {
        AppControlFamily.Miuix -> top.yukonga.miuix.kmp.basic.IconButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            backgroundColor = Color.Transparent,
            minWidth = 48.dp,
            minHeight = 48.dp,
        ) {
            top.yukonga.miuix.kmp.basic.Icon(
                imageVector = symbol.miuixSymbolVector(),
                contentDescription = contentDescription,
                modifier = Modifier.size(AppUiTheme.icons.actionSize),
            )
        }

        AppControlFamily.Lollipop -> LollipopActionButton(onClick, modifier, enabled) { LollipopSymbolIcon(symbol, contentDescription) }

        AppControlFamily.Eclair -> EclairToolbarButton(contentDescription, onClick, modifier, enabled, symbol)

        AppControlFamily.Holo -> ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloActionButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
        ) {
            ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloSymbolIcon(
                symbol = symbol,
                contentDescription = contentDescription,
            )
        }

        AppControlFamily.Material2, AppControlFamily.Material3 -> AppIconButton(
            onClick = onClick, modifier = modifier, enabled = enabled,
        ) { AppSymbolIcon(symbol, contentDescription, Modifier.size(AppUiTheme.icons.actionSize)) }
    }
}
