package ing.fuyaoskyrocket.applocale.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Presentation preferences only; no locale writes, permission changes, or draft data. */
data class UserPreferences(
    val systemFontForRetro: Boolean = true,
    val showSystemApps: Boolean = true,
    val showPackageNames: Boolean = true,
    val showAppTypes: Boolean = true,
    val showRegionFlags: Boolean = true,
    val swipeBetweenPages: Boolean = true,
    val doubleTapToTop: Boolean = true,
    val rememberLastPage: Boolean = false,
    val lastPage: String = "home",
) {
    fun value(setting: BooleanPreference): Boolean = when (setting) {
        BooleanPreference.SystemFontForRetro -> systemFontForRetro
        BooleanPreference.ShowSystemApps -> showSystemApps
        BooleanPreference.ShowPackageNames -> showPackageNames
        BooleanPreference.ShowAppTypes -> showAppTypes
        BooleanPreference.ShowRegionFlags -> showRegionFlags
        BooleanPreference.SwipeBetweenPages -> swipeBetweenPages
        BooleanPreference.DoubleTapToTop -> doubleTapToTop
        BooleanPreference.RememberLastPage -> rememberLastPage
    }

    fun with(setting: BooleanPreference, enabled: Boolean): UserPreferences = when (setting) {
        BooleanPreference.SystemFontForRetro -> copy(systemFontForRetro = enabled)
        BooleanPreference.ShowSystemApps -> copy(showSystemApps = enabled)
        BooleanPreference.ShowPackageNames -> copy(showPackageNames = enabled)
        BooleanPreference.ShowAppTypes -> copy(showAppTypes = enabled)
        BooleanPreference.ShowRegionFlags -> copy(showRegionFlags = enabled)
        BooleanPreference.SwipeBetweenPages -> copy(swipeBetweenPages = enabled)
        BooleanPreference.DoubleTapToTop -> copy(doubleTapToTop = enabled)
        BooleanPreference.RememberLastPage -> copy(rememberLastPage = enabled)
    }
}

enum class BooleanPreference(val key: String) {
    SystemFontForRetro("system_font_for_retro"), ShowSystemApps("show_system_apps"),
    ShowPackageNames("show_package_names"), ShowAppTypes("show_app_types"),
    ShowRegionFlags("show_region_flags"), SwipeBetweenPages("swipe_between_pages"),
    DoubleTapToTop("double_tap_to_top"), RememberLastPage("remember_last_page"),
}

class UserPreferencesStore(private val preferences: SharedPreferences) {
    private val mutableState = MutableStateFlow(read())
    val state: StateFlow<UserPreferences> = mutableState.asStateFlow()

    private fun read(): UserPreferences {
        var result = UserPreferences()
        BooleanPreference.entries.forEach { setting ->
            result = result.with(setting, preferences.getBoolean(setting.key, result.value(setting)))
        }
        return result.copy(lastPage = preferences.getString("last_page", "home") ?: "home")
    }

    fun set(setting: BooleanPreference, enabled: Boolean) {
        if (state.value.value(setting) == enabled) return
        preferences.edit().putBoolean(setting.key, enabled).apply()
        mutableState.update { it.with(setting, enabled) }
    }

    fun rememberPage(route: String) {
        if (!state.value.rememberLastPage || state.value.lastPage == route) return
        preferences.edit().putString("last_page", route).apply()
        mutableState.update { it.copy(lastPage = route) }
    }
}

/** Initialized once by Application, matching the existing appearance preferences. */
object AppUserPreferences {
    private var store: UserPreferencesStore? = null
    private val defaults = MutableStateFlow(UserPreferences()).asStateFlow()
    val state: StateFlow<UserPreferences> get() = store?.state ?: defaults

    fun initialize(context: Context) {
        store = UserPreferencesStore(context.applicationContext.getSharedPreferences("app_user_settings", Context.MODE_PRIVATE))
    }

    fun set(setting: BooleanPreference, enabled: Boolean) = checkNotNull(store).set(setting, enabled)
    fun rememberPage(route: String) = checkNotNull(store).rememberPage(route)
}
