package ing.fuyaoskyrocket.applocale.data.preferences

import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserPreferencesStoreTest {
    @Test fun freshInstallUsesDefaultsAndDoesNotWriteThem() {
        val disk = MemoryPreferences()
        assertEquals(UserPreferences(), UserPreferencesStore(disk).state.value)
        assertTrue(disk.all.isEmpty())
    }

    @Test fun independentEditsSurviveAStoreRestart() {
        val disk = MemoryPreferences()
        val store = UserPreferencesStore(disk)
        store.set(BooleanPreference.ShowSystemApps, false)
        store.set(BooleanPreference.ShowPackageNames, false)
        store.set(BooleanPreference.ShowRegionFlags, false)
        store.set(BooleanPreference.SwipeBetweenPages, false)
        store.set(BooleanPreference.ShowPackageNames, true)
        val restored = UserPreferencesStore(disk).state.value
        assertEquals(store.state.value, restored)
        assertFalse(restored.showSystemApps)
        assertFalse(restored.showRegionFlags)
        assertFalse(restored.swipeBetweenPages)
        assertTrue(restored.showPackageNames)
        assertTrue(restored.systemFontForRetro)
    }

    @Test fun lastPageIsSavedOnlyWhileThePreferenceIsEnabled() {
        val disk = MemoryPreferences()
        val store = UserPreferencesStore(disk)
        store.rememberPage("about")
        assertEquals("home", store.state.value.lastPage)
        store.set(BooleanPreference.RememberLastPage, true)
        store.rememberPage("settings")
        store.set(BooleanPreference.RememberLastPage, false)
        store.rememberPage("about")
        val restored = UserPreferencesStore(disk).state.value
        assertFalse(restored.rememberLastPage)
        assertEquals("settings", restored.lastPage)
    }
}

/** Host-only fake; no Android runtime or UI test dependencies. */
private class MemoryPreferences : SharedPreferences {
    private val data = mutableMapOf<String, Any?>()
    override fun getAll(): Map<String, *> = data.toMap()
    override fun contains(key: String) = data.containsKey(key)
    override fun getBoolean(key: String, defValue: Boolean) = data[key] as? Boolean ?: defValue
    override fun getString(key: String, defValue: String?) = data[key] as? String ?: defValue
    override fun getInt(key: String, defValue: Int) = data[key] as? Int ?: defValue
    override fun getLong(key: String, defValue: Long) = data[key] as? Long ?: defValue
    override fun getFloat(key: String, defValue: Float) = data[key] as? Float ?: defValue
    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String, defValues: MutableSet<String>?) =
        (data[key] as? Set<String>)?.toMutableSet() ?: defValues
    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) = Unit
    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) = Unit
    override fun edit() = object : SharedPreferences.Editor {
        private val pending = mutableMapOf<String, Any?>()
        private var clear = false
        override fun putBoolean(key: String, value: Boolean) = apply { pending[key] = value }
        override fun putString(key: String, value: String?) = apply { pending[key] = value }
        override fun putInt(key: String, value: Int) = apply { pending[key] = value }
        override fun putLong(key: String, value: Long) = apply { pending[key] = value }
        override fun putFloat(key: String, value: Float) = apply { pending[key] = value }
        override fun putStringSet(key: String, values: MutableSet<String>?) = apply { pending[key] = values?.toSet() }
        override fun remove(key: String) = apply { pending[key] = null }
        override fun clear() = apply { clear = true }
        override fun commit(): Boolean {
            if (clear) data.clear()
            pending.forEach { (key, value) -> if (value == null) data.remove(key) else data[key] = value }
            return true
        }
        override fun apply() { commit() }
    }
}
