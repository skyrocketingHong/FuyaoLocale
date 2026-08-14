package ing.fuyaoskyrocket.applocale.ui.components

import android.app.LocaleManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

/** Returns the primary system locale, ignoring Fuyao Locale's own language override. */
@Composable
fun rememberSystemLocaleTag(): String {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    return remember(context, configuration) {
        val locales = context.getSystemService(LocaleManager::class.java).systemLocales
        if (locales.isEmpty) "" else locales[0].toLanguageTag()
    }
}
