package ing.fuyaoskyrocket.applocale.model

/**
 * Result of a batch locale-apply operation.
 */
data class BatchLocaleResult(
    val totalCount: Int,
    val failedPackages: List<String>,
) {
    val successCount: Int
        get() = totalCount - failedPackages.size

    val isAllSuccess: Boolean
        get() = failedPackages.isEmpty()
}

/** UI-facing batch progress state. */
sealed interface BatchApplyState {
    data object Idle : BatchApplyState
    data class Applying(val total: Int, val done: Int) : BatchApplyState
    data class Done(val result: BatchLocaleResult) : BatchApplyState
}
