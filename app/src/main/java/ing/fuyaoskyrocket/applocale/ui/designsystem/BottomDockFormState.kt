package ing.fuyaoskyrocket.applocale.ui.designsystem

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** A preference change cannot replace the renderer until its exit has finished. */
internal class BottomDockFormState(initialGlass: Boolean) {
    var displayedGlass by mutableStateOf(initialGlass)
        private set
    var requestedGlass by mutableStateOf(initialGlass)
        private set
    val changing: Boolean get() = displayedGlass != requestedGlass
    fun request(glass: Boolean) { requestedGlass = glass }
    fun finishExit() { displayedGlass = requestedGlass }
}
