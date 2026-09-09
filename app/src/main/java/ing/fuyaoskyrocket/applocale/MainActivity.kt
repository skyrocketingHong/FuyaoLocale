package ing.fuyaoskyrocket.applocale

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ipc.RootService
import dagger.hilt.android.AndroidEntryPoint
import rikka.shizuku.Shizuku
import ing.fuyaoskyrocket.applocale.model.OperationMode
import ing.fuyaoskyrocket.applocale.service.PrivilegedServiceClient
import ing.fuyaoskyrocket.applocale.service.UserService
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppAppearanceHost
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppAppearanceTransitionViewModel
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSurface
import ing.fuyaoskyrocket.applocale.ui.screen.Navigation
import javax.inject.Inject

object ShizukuArgs {
    val userServiceArgs =
        Shizuku.UserServiceArgs(
            ComponentName(BuildConfig.APPLICATION_ID, UserService::class.java.name),
        )
            .daemon(false)
            .processNameSuffix("service")
            .debuggable(BuildConfig.DEBUG)
            .version(BuildConfig.VERSION_CODE)
}

@AndroidEntryPoint
class MainActivity : ComponentActivity(), Shizuku.OnRequestPermissionResultListener {

    @Inject
    lateinit var serviceClient: PrivilegedServiceClient

    private val appearanceViewModel: AppAppearanceTransitionViewModel by viewModels()

    init {
        Shell.enableVerboseLogging = BuildConfig.DEBUG
        Shell.setDefaultBuilder(Shell.Builder.create().setTimeout(10))
    }

    private companion object {
        const val SHIZUKU_PERMISSION_REQUEST_CODE = 1
        const val SHIZUKU_PACKAGE_NAME = "moe.shizuku.privileged.api"
        const val SHIZUKU_DOWNLOAD_URL = "https://shizuku.rikka.app/download/"
        const val TAG = "FuyaoLocaleShizuku"
    }

    private var hasGrantedShizukuPermission by mutableStateOf(false)

    private fun bindShizuku() {
        Shizuku.bindUserService(ShizukuArgs.userServiceArgs, serviceClient.connection)
    }

    private fun refreshShizukuPermissionState() {
        hasGrantedShizukuPermission = try {
            Shizuku.pingBinder() &&
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (_: Throwable) {
            false
        }
    }

    private val requestPermissionResultListener = this::onRequestPermissionResult
    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        runOnUiThread {
            refreshShizukuPermissionState()
            if (
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED &&
                !serviceClient.isConnected
            ) {
                bindShizuku()
            }
        }
    }

    override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
        if (
            requestCode == SHIZUKU_PERMISSION_REQUEST_CODE &&
            grantResult == PackageManager.PERMISSION_GRANTED
        ) {
            hasGrantedShizukuPermission = true
            bindShizuku()
        } else if (requestCode == SHIZUKU_PERMISSION_REQUEST_CODE) {
            hasGrantedShizukuPermission = false
        }
    }

    /**
     * Activity-owned Shizuku permission entry point.
     *
     * Requesting Shizuku permission launches platform UI, so Compose screens delegate this
     * action here instead of asking a ViewModel to wait for a service that is not authorized yet.
     */
    private fun requestShizukuPermission() {
        try {
            if (!Shizuku.pingBinder()) {
                hasGrantedShizukuPermission = false
                openShizuku()
                return
            }

            when {
                Shizuku.isPreV11() -> {
                    openShizuku()
                }

                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED -> {
                    hasGrantedShizukuPermission = true
                    if (!serviceClient.isConnected) bindShizuku()
                }

                Shizuku.shouldShowRequestPermissionRationale() -> {
                    hasGrantedShizukuPermission = false
                    // Shizuku will not show another prompt after a persistent denial.
                    openShizuku()
                }

                else -> {
                    hasGrantedShizukuPermission = false
                    Shizuku.requestPermission(SHIZUKU_PERMISSION_REQUEST_CODE)
                }
            }
        } catch (error: Throwable) {
            hasGrantedShizukuPermission = false
            Log.e(TAG, "Unable to request Shizuku permission", error)
            openShizuku()
        }
    }

    private fun openShizuku() {
        val launchIntent = packageManager.getLaunchIntentForPackage(SHIZUKU_PACKAGE_NAME)
        if (launchIntent != null) {
            startActivity(launchIntent)
        } else {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SHIZUKU_DOWNLOAD_URL)))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Shizuku.addRequestPermissionResultListener(requestPermissionResultListener)
        refreshShizukuPermissionState()
        setContent {
            // Single movable runtime tree: its composition identity survives the
            // theme-provider switch inside the appearance host.
            val runtimeContent = remember {
                movableContentOf {
                    AppSurface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RectangleShape,
                        color = AppUiTheme.palette.background,
                        contentColor = AppUiTheme.palette.foreground,
                    ) {
                        Navigation(
                            hasGrantedShizukuPermission = hasGrantedShizukuPermission,
                            onRequestShizukuPermission = { requestShizukuPermission() },
                            onOpenShizuku = ::openShizuku,
                        )
                    }
                }
            }
            AppAppearanceHost(viewModel = appearanceViewModel) {
                runtimeContent()
            }
        }

        // Binder arrival may reconnect an already-authorized service, but permission UI is
        // opened only from the explicit user action passed to Navigation above.
        Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)

        RootReceivedListener.setListener(object : IRootListener {
            override fun onRootReceived() {
                val intent = Intent(
                    application,
                    ing.fuyaoskyrocket.applocale.service.RootUserService::class.java,
                )
                RootService.bind(intent, serviceClient.connection)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        refreshShizukuPermissionState()
        if (
            Shizuku.pingBinder() &&
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED &&
            !serviceClient.isConnected
        ) {
            bindShizuku()
        }
    }

    override fun onDestroy() {
        Shizuku.removeBinderReceivedListener(binderReceivedListener)
        Shizuku.removeRequestPermissionResultListener(requestPermissionResultListener)
        RootReceivedListener.destroy()
        // The client is application-scoped. Keep its Binder connection across rotations and
        // app-language recreation so the new Activity does not flash a permission/loading state.
        if (!isChangingConfigurations && isFinishing && serviceClient.isConnected) {
            when (serviceClient.operationMode) {
                OperationMode.ROOT -> RootService.unbind(serviceClient.connection)
                OperationMode.SHIZUKU -> Shizuku.unbindUserService(
                    ShizukuArgs.userServiceArgs,
                    serviceClient.connection,
                    true
                )
                else -> Log.d(BuildConfig.APPLICATION_ID, "UserService not bound.")
            }
        }
        super.onDestroy()
    }
}
