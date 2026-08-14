package ing.fuyaoskyrocket.applocale.data.system

import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads app icons off the main thread and caches the converted [ImageBitmap].
 *
 * **Never** call [PackageManager.getApplicationIcon] or [Drawable.toBitmap] inside a
 * Composable — always go through this loader.
 */
@Singleton
class AppIconLoader @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val pm get() = context.packageManager

    private val cache = object : LruCache<String, ImageBitmap>(MAX_CACHE_BYTES) {
        override fun sizeOf(key: String, value: ImageBitmap): Int {
            return try {
                value.asAndroidBitmap().byteCount
            } catch (e: Exception) {
                1
            }
        }
    }

    /**
     * Returns the cached [ImageBitmap] for [packageName], or loads it on [Dispatchers.IO].
     * Returns null on any failure (caller shows placeholder).
     */
    suspend fun loadIcon(packageName: String): ImageBitmap? {
        cache.get(packageName)?.let { return it }
        return withContext(Dispatchers.IO) {
            try {
                val drawable = pm.getApplicationIcon(packageName)
                val bitmap: Bitmap = drawable.toBitmap(ICON_PX, ICON_PX, Bitmap.Config.ARGB_8888)
                val imageBitmap = bitmap.asImageBitmap()
                cache.put(packageName, imageBitmap)
                imageBitmap
            } catch (e: Exception) {
                null
            }
        }
    }

    /** Evict a specific entry (e.g. after package uninstall). */
    fun evict(packageName: String) {
        cache.remove(packageName)
    }

    companion object {
        private const val ICON_PX = 192
        private const val MAX_CACHE_BYTES = 12 * 1024 * 1024 // 12 MB
    }
}
