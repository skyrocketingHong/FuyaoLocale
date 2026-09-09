package ing.fuyaoskyrocket.applocale.ui.designsystem.component

import ing.fuyaoskyrocket.applocale.ui.designsystem.lollipop.*
import androidx.compose.foundation.background
import androidx.compose.foundation.indication
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.*
import ing.fuyaoskyrocket.applocale.ui.designsystem.eclair.*
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.*
import ing.fuyaoskyrocket.applocale.ui.designsystem.legacy.*

/** Layout remains shared; each provider explicitly owns row selectors and dividers. */
@Composable
internal fun Modifier.appContinuousRow(state: LegacyControlState, interaction: androidx.compose.foundation.interaction.InteractionSource, activated: Boolean = false): Modifier =
    when (AppUiTheme.policy.controls) {
        AppControlFamily.Lollipop -> {
            val base = if (activated && state.activated) this.then(Modifier.background(AppUiTheme.palette.selected)) else this
            base.indication(interaction, LollipopRipple(LocalLollipopColors.current.ripple)).lollipopListDivider()
        }
        AppControlFamily.Holo -> {
            val base = if (activated) holoBackground(rememberHoloDrawable(HoloAsset.ActivatedBackground), state) else this
            base.holoBackground(rememberHoloDrawable(HoloAsset.ItemBackground), state).holoListDivider()
        }
        AppControlFamily.Eclair -> legacyBackground(rememberClassicDrawable(R.drawable.eclair_list_selector_background), state).eclairListDivider()
        AppControlFamily.Material2, AppControlFamily.Material3, AppControlFamily.Miuix -> error("Continuous row requested by a modern provider")
    }

@Composable
internal fun Modifier.appListDivider(): Modifier = when (AppUiTheme.policy.controls) {
    AppControlFamily.Lollipop -> lollipopListDivider()
    AppControlFamily.Holo -> holoListDivider()
    AppControlFamily.Eclair -> eclairListDivider()
    AppControlFamily.Material2, AppControlFamily.Material3, AppControlFamily.Miuix -> this
}

@Composable
internal fun AppPreferenceCategory(title: String, modifier: Modifier = Modifier) {
    when (AppUiTheme.policy.controls) {
        AppControlFamily.Lollipop -> LollipopCategory(title, modifier)
        AppControlFamily.Holo -> HoloPreferenceCategory(title, modifier)
        AppControlFamily.Eclair -> EclairCategory(title, modifier)
        AppControlFamily.Material2, AppControlFamily.Material3, AppControlFamily.Miuix -> AppText(title,
            modifier.padding(horizontal = AppUiTheme.spacing.contentInset, vertical = AppUiTheme.spacing.bodyVerticalPadding).semantics { heading() },
            style = AppUiTheme.textStyles.label, color = AppUiTheme.palette.muted)
    }
}

@Composable
internal fun AppDropdownButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    when (AppUiTheme.policy.controls) {
        AppControlFamily.Lollipop -> LollipopDropdownButton(text, onClick, modifier)
        AppControlFamily.Holo -> HoloDropdownButton(text, onClick, modifier)
        AppControlFamily.Eclair -> EclairDropdownButton(text, onClick, modifier)
        AppControlFamily.Material2, AppControlFamily.Material3, AppControlFamily.Miuix -> AppButton(text, onClick, modifier)
    }
}

@Composable
internal fun appListContentInset(): androidx.compose.ui.unit.Dp = when (AppUiTheme.policy.controls) {
    AppControlFamily.Holo -> LocalHoloListPadding.current
    AppControlFamily.Lollipop, AppControlFamily.Eclair -> AppUiTheme.metrics.listPreferredItemPaddingHorizontal
    AppControlFamily.Material2, AppControlFamily.Material3, AppControlFamily.Miuix -> AppLayout.contentFrameMargin
}

@Composable
internal fun appRowPrimary(state: LegacyControlState): androidx.compose.ui.graphics.Color =
    if (AppUiTheme.policy.controls == AppControlFamily.Eclair) eclairColor(
        if (LocalEclairColors.current.dark) R.color.eclair_primary_text_dark else R.color.eclair_primary_text_light, state,
    ) else AppUiTheme.palette.foreground

@Composable
internal fun appRowSecondary(state: LegacyControlState): androidx.compose.ui.graphics.Color =
    if (AppUiTheme.policy.controls == AppControlFamily.Eclair) eclairColor(
        if (LocalEclairColors.current.dark) R.color.eclair_secondary_text_dark else R.color.eclair_secondary_text_light, state,
    ) else AppUiTheme.palette.muted
