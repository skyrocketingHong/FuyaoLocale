package ing.fuyaoskyrocket.applocale.service

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import ing.fuyaoskyrocket.applocale.IUserService

/**
 * Service connection that delegates connect / disconnect events to callbacks.
 * Replaces the old static [UserServiceProvider] pattern.
 */
class Connection : ServiceConnection {

    var onConnected: ((IUserService) -> Unit)? = null
    var onDisconnected: (() -> Unit)? = null

    @Volatile
    var service: IUserService? = null
        private set

    override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
        val s = IUserService.Stub.asInterface(binder)
        // Prevent overwriting an already-live service reference (mirrors original guard).
        if (service == null) {
            service = s
        }
        onConnected?.invoke(service ?: s)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        service = null
        onDisconnected?.invoke()
    }
}
