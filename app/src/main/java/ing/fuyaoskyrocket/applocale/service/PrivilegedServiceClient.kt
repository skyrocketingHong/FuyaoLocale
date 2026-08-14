package ing.fuyaoskyrocket.applocale.service

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import ing.fuyaoskyrocket.applocale.IUserService
import ing.fuyaoskyrocket.applocale.model.OperationMode
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Replaces the old [UserServiceProvider] singleton.
 *
 * Exposes the privileged [IUserService] as a [StateFlow] so ViewModels and repositories
 * can await it without blocking or [Thread.sleep].  Connection binding itself is still
 * done by [ing.fuyaoskyrocket.applocale.MainActivity] and [ing.fuyaoskyrocket.applocale.LocaleQuickSettingsTile]
 * via [connection].
 */
@Singleton
class PrivilegedServiceClient @Inject constructor() {

    companion object {
        private const val TAG = "PrivilegedServiceClient"
        private const val DEFAULT_TIMEOUT_MS = 20_000L
    }

    val connection = Connection().apply {
        onConnected = { s -> onServiceConnected(s) }
        onDisconnected = { onServiceDisconnected() }
    }

    private val _service = MutableStateFlow<IUserService?>(null)
    val serviceFlow: StateFlow<IUserService?> = _service.asStateFlow()

    private val _operationMode = MutableStateFlow(OperationMode.NONE)
    val operationModeFlow: StateFlow<OperationMode> = _operationMode.asStateFlow()

    val isConnected: Boolean
        get() = _service.value != null

    /** Synchronous accessor — may return null if not yet connected. */
    val service: IUserService?
        get() = _service.value

    val operationMode: OperationMode
        get() = _operationMode.value

    private fun onServiceConnected(s: IUserService) {
        _service.value = s
        val uid = try {
            s.uid
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read service uid", e)
            -1
        }
        _operationMode.value = when {
            uid == 0 -> OperationMode.ROOT
            uid in 1000..2000 -> OperationMode.SHIZUKU
            else -> OperationMode.NONE
        }
        Log.d(TAG, "IUserService connected, uid=$uid, mode=${_operationMode.value}")
    }

    private fun onServiceDisconnected() {
        _service.value = null
        _operationMode.value = OperationMode.NONE
        Log.d(TAG, "IUserService disconnected")
    }

    /**
     * Suspend until the service is connected or [timeoutMs] elapses.
     * Returns null on timeout.
     */
    suspend fun awaitService(timeoutMs: Long = DEFAULT_TIMEOUT_MS): IUserService? {
        _service.value?.let { return it }
        return withTimeoutOrNull(timeoutMs) {
            serviceFlow.first { it != null }
        }
    }
}
