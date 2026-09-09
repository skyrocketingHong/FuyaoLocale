package ing.fuyaoskyrocket.applocale.ui.designsystem.holo

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSymbol

/**
 * Resolves the named framework glyphs to the imported AOSP bitmaps. The
 * mapping is exhaustive per symbol-with-an-era-asset; symbols without an
 * original framework control (pin, business-only actions) must not route
 * here — their pages surface them as ICS text actions instead.
 */
internal fun AppSymbol.holoDrawableRes(): HoloAsset? = when (this) {
    AppSymbol.Search -> HoloAsset.Search
    AppSymbol.Menu -> HoloAsset.Overflow
    AppSymbol.Back -> HoloAsset.Back
    AppSymbol.Close -> HoloAsset.Close
    AppSymbol.Refresh -> HoloAsset.Refresh
    AppSymbol.Delete -> HoloAsset.Delete
    AppSymbol.Add -> HoloAsset.Add
    AppSymbol.Settings,
    AppSymbol.Pin,
    AppSymbol.PinActive,
    AppSymbol.Check,
    AppSymbol.SelectAll,
    AppSymbol.Sort,
    AppSymbol.Forward,
    AppSymbol.Info,
    -> null
}

/**
 * The named-symbol icon entry of the holo backend: renders the fixed-version
 * framework bitmap inside a 48dp slot (glyph at its intrinsic density-scaled
 * size, capped at 32dp like action bar icons). No tint — the holographic
 * glyphs are pre-coloured era assets.
 */
@Composable
internal fun HoloSymbolIcon(
    symbol: AppSymbol,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val asset = symbol.holoDrawableRes() ?: return
    HoloAssetIcon(asset, contentDescription, modifier)
}

internal val LocalHoloActionState = androidx.compose.runtime.staticCompositionLocalOf { HoloControlState() }

@Composable
internal fun HoloAssetIcon(asset: HoloAsset, description: String?, modifier: Modifier = Modifier) {
    Box(modifier.size(32.dp).then(if (description != null) Modifier.semantics {
        contentDescription = description
    } else Modifier).holoDrawable(rememberHoloDrawable(asset), LocalHoloActionState.current,
        boundsMode = ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.LegacyDrawableBounds.Fit,
        alignment = Alignment.Center))
}

/**
 * The holo ActionButton (Widget.Holo.ActionButton): 12dp horizontal padding,
 * visual height = action bar height (48dp phone / 40 landscape / 56 sw600dp,
 * from the qualifier-aware resource), item_background pressed states. The
 * landscape 40dp stays VISUAL: the wrapper is measured taller by the caller's
 * touch-slot contract (≥48dp hit area) without painting outside the bar.
 */
@Composable
internal fun HoloActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    glyph: @Composable () -> Unit,
) {
    val metrics = LocalHoloMetrics.current
    val drawable = rememberHoloDrawable(HoloAsset.ItemBackground)
    val controlState = holoControlState(interactionSource, enabled = enabled)
    Row(
        modifier = modifier
            .height(metrics.actionBarHeight)
            .holoBackground(drawable, controlState)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = metrics.actionButtonPaddingHorizontal),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.runtime.CompositionLocalProvider(LocalHoloActionState provides controlState) { glyph() }
    }
}
