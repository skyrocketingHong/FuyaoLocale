package ing.fuyaoskyrocket.applocale

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppThemePreferences

@HiltAndroidApp
class FuyaoLocaleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppThemePreferences.initialize(this)
    }
}
