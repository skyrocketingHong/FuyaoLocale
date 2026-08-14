package ing.fuyaoskyrocket.applocale.ui.components

import androidx.compose.foundation.basicMarquee
import androidx.compose.ui.Modifier

/**
 * Uses Compose Foundation's native marquee only when the content exceeds its width.
 * Content that already fits remains static.
 */
fun Modifier.marqueeOnOverflow(): Modifier = basicMarquee(
    iterations = Int.MAX_VALUE,
)
