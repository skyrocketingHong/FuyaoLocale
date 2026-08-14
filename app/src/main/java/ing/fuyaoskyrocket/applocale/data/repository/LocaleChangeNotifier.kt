package ing.fuyaoskyrocket.applocale.data.repository

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lightweight application-scoped signal for locale changes made outside the
 * home list (detail pages and saved configurations).
 */
@Singleton
class LocaleChangeNotifier @Inject constructor() {
    private val _changes = MutableSharedFlow<Set<String>>(extraBufferCapacity = 1)
    val changes = _changes.asSharedFlow()

    fun publish(packages: Collection<String>) {
        if (packages.isNotEmpty()) {
            _changes.tryEmit(packages.toSet())
        }
    }
}
