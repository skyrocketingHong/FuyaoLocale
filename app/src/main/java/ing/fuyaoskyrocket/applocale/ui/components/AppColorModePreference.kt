package ing.fuyaoskyrocket.applocale.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppColorMode
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemePreferences
import ing.fuyaoskyrocket.applocale.ui.designsystem.LocalAppearanceRequester
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppChoicePreference
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppPreferenceOption

@Composable
fun AppColorModePreference(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val requester = LocalAppearanceRequester.current
    AppChoicePreference(
        title = stringResource(R.string.appearance_mode),
        selected = AppThemePreferences.colorMode,
        options = listOf(
            AppPreferenceOption(AppColorMode.SYSTEM, stringResource(R.string.appearance_mode_system)),
            AppPreferenceOption(AppColorMode.LIGHT, stringResource(R.string.appearance_mode_light)),
            AppPreferenceOption(AppColorMode.DARK, stringResource(R.string.appearance_mode_dark)),
        ),
        onSelected = { mode, focus ->
            requester?.requestColorMode(mode, focus) ?: AppThemePreferences.setColorMode(context, mode)
        },
        modifier = modifier,
    )
}
