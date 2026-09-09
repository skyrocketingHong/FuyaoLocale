package ing.fuyaoskyrocket.applocale.ui.designsystem.legacy

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay

internal open class LegacyMessageQueue {
    internal class Entry(val message: String, val done: CompletableDeferred<Unit>)

    internal val channel = Channel<Entry>(capacity = Channel.UNLIMITED)

    suspend fun show(message: String) {
        val entry = Entry(message, CompletableDeferred())
        channel.send(entry)
        // Returns when the display actually finished (or the host went away).
        entry.done.await()
    }

    /** Completes every outstanding waiter — used when the consumer dies. */
    internal fun flush() {
        while (true) {
            val entry = channel.tryReceive().getOrNull() ?: break
            entry.done.complete(Unit)
        }
    }
}


@Composable
internal fun LegacyMessageHost(queue: LegacyMessageQueue, durationMillis: Long, content: @Composable (String) -> Unit) {
    var current by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(queue) {
        var inFlight: LegacyMessageQueue.Entry? = null
        try {
            for (entry in queue.channel) {
                inFlight = entry
                current = entry.message
                try {
                    delay(durationMillis)
                } finally {
                    // Host died mid-display: complete this waiter too, or its
                    // caller suspends forever (queue must never trap callers).
                    entry.done.complete(Unit)
                }
                current = null
                inFlight = null
            }
        } finally {
            inFlight?.done?.complete(Unit)
            queue.flush()
        }
    }
    val message = current ?: return
    content(message)
}
