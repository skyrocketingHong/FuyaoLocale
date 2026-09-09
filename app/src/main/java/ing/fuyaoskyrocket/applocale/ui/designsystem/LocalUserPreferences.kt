package ing.fuyaoskyrocket.applocale.ui.designsystem

import androidx.compose.runtime.staticCompositionLocalOf
import ing.fuyaoskyrocket.applocale.data.preferences.UserPreferences

/** One root collection feeds all UI consumers; list rows do not each collect a Flow. */
val LocalUserPreferences = staticCompositionLocalOf { UserPreferences() }
