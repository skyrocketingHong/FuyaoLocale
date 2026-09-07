package ing.fuyaoskyrocket.applocale.data.repository

import ing.fuyaoskyrocket.applocale.model.ConfigurationEditState
import ing.fuyaoskyrocket.applocale.model.PendingConfigurationEdit
import ing.fuyaoskyrocket.applocale.model.ConfigurationEditRejection
import ing.fuyaoskyrocket.applocale.model.SavedLocaleConfiguration
import ing.fuyaoskyrocket.applocale.model.SavedLocaleEntry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Behavior of the single live-edit command against a recording fake
 * environment (round-8 039): real call counts for apply/save/pending, the
 * failure split (apply failed / outcome unknown / applied-pending-save), the
 * retry-save-only path, idempotent acknowledgements, and pending recovery.
 */
class ConfigurationEditCoordinatorTest {

    private class FakeEnvironment : ConfigurationEditEnvironment {
        val sources = mutableMapOf<String, SavedLocaleConfiguration>()
        var installed: Boolean = true
        var currentTag: ConfigurationEditEnvironment.TagRead =
            ConfigurationEditEnvironment.TagRead.Known(null)
        var applyOutcome: ConfigurationEditEnvironment.ApplyOutcome =
            ConfigurationEditEnvironment.ApplyOutcome.Applied
        var saveOutcome: ConfigurationEditEnvironment.SaveOutcome =
            ConfigurationEditEnvironment.SaveOutcome.Saved
        var pendingRecord: PendingConfigurationEdit? = null
        var pendingWriteOk: Boolean = true

        /** An unclear Binder call that actually landed before the timeout. */
        var unclearLanded: Boolean = false

        var applyCount = 0
        var saveCount = 0
        var pendingWriteCount = 0
        var clearPendingCount = 0
        var notifiedPackages = mutableListOf<String>()
        val savedSnapshots = mutableListOf<Pair<SavedLocaleConfiguration, String?>>()

        override suspend fun findSourceConfiguration(id: String) = sources[id]

        override suspend fun isPackageInstalled(packageName: String) = installed

        override suspend fun readCurrentTag(packageName: String) = currentTag

        override suspend fun writePendingEdit(record: PendingConfigurationEdit): Boolean {
            pendingWriteCount++
            pendingRecord = record
            return pendingWriteOk
        }

        override suspend fun applyLocale(
            packageName: String,
            localeTag: String?,
        ): ConfigurationEditEnvironment.ApplyOutcome {
            applyCount++
            // A successful (or landed-but-unclear) apply changes the state the
            // next read observes; a failed call never does.
            if (applyOutcome == ConfigurationEditEnvironment.ApplyOutcome.Applied || unclearLanded) {
                currentTag = ConfigurationEditEnvironment.TagRead.Known(localeTag)
            }
            return applyOutcome
        }

        override suspend fun saveDerivedSnapshot(
            snapshot: SavedLocaleConfiguration,
            clearPendingOperationId: String,
        ): ConfigurationEditEnvironment.SaveOutcome {
            saveCount++
            savedSnapshots += snapshot to clearPendingOperationId
            return saveOutcome
        }

        override suspend fun clearPendingEdit() {
            clearPendingCount++
            pendingRecord = null
        }

        override fun readPendingEdit(): PendingConfigurationEdit? = pendingRecord

        override fun notifyLocaleChanged(packageName: String) {
            notifiedPackages += packageName
        }

        override suspend fun resolveLabel(packageName: String, fallback: String?): String =
            fallback ?: packageName
    }

    private fun sourceConfig(): SavedLocaleConfiguration = SavedLocaleConfiguration(
        id = "source-1",
        createdAt = 10L,
        entries = listOf(
            SavedLocaleEntry("other.app", "Other", "ja"),
        ),
    )

    private fun await(
        coordinator: ConfigurationEditCoordinator,
        predicate: (ConfigurationEditState) -> Boolean,
    ): ConfigurationEditState = runBlocking {
        withTimeout(5_000) { coordinator.state.first { predicate(it) } }
    }

    private fun awaitTerminal(coordinator: ConfigurationEditCoordinator): ConfigurationEditState =
        await(coordinator) {
            it !is ConfigurationEditState.Idle && it !is ConfigurationEditState.Running
        }

    /** Polls a side effect that the coordinator performs asynchronously on IO. */
    private fun awaitTrue(condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + 5_000
        while (!condition()) {
            check(System.currentTimeMillis() < deadline) { "condition not met in time" }
            Thread.sleep(10)
        }
    }

    @Test
    fun singleSuccessAppliesOnceThenSavesWithPendingClearedInOneCall() {
        val env = FakeEnvironment().apply {
            sources["source-1"] = sourceConfig()
            currentTag = ConfigurationEditEnvironment.TagRead.Known("ja")
        }
        val coordinator = ConfigurationEditCoordinator(env)

        coordinator.submit("source-1", "target.app", "en")
        val terminal = awaitTerminal(coordinator)

        assertTrue(terminal.toString(), terminal is ConfigurationEditState.Completed)
        terminal as ConfigurationEditState.Completed
        assertEquals(true, terminal.appliedLocaleChange)
        assertEquals(terminal.command.operationId, terminal.newConfigurationId)
        assertEquals(1, env.applyCount)
        assertEquals(1, env.saveCount)
        assertEquals(1, env.pendingWriteCount)
        assertEquals(listOf("target.app"), env.notifiedPackages)
        // The save must carry the SAME operation id so the record clears with it.
        assertEquals(
            terminal.command.operationId,
            env.savedSnapshots.single().second,
        )
        // Only the target package was touched; the other entry survived.
        val saved = env.savedSnapshots.single().first
        assertEquals(
            listOf("other.app" to "ja", "target.app" to "en"),
            saved.entries.map { it.packageName to it.localeTag },
        )
    }

    @Test
    fun noChangeDoesNotTouchBinderOrStore() {
        val env = FakeEnvironment().apply {
            sources["source-1"] = SavedLocaleConfiguration(
                id = "source-1",
                createdAt = 10L,
                entries = listOf(SavedLocaleEntry("target.app", "Target", "en")),
            )
            currentTag = ConfigurationEditEnvironment.TagRead.Known("en")
        }
        val coordinator = ConfigurationEditCoordinator(env)

        coordinator.submit("source-1", "target.app", "en")
        val terminal = awaitTerminal(coordinator) as ConfigurationEditState.Completed

        assertNull("no change creates no configuration", terminal.newConfigurationId)
        assertEquals(0, env.applyCount)
        assertEquals(0, env.saveCount)
        assertEquals(0, env.pendingWriteCount)
    }

    @Test
    fun managementOnlyChangeSkipsBinderAndStillSaves() {
        val env = FakeEnvironment().apply {
            sources["source-1"] = sourceConfig()
            currentTag = ConfigurationEditEnvironment.TagRead.Known(null)
        }
        val coordinator = ConfigurationEditCoordinator(env)

        coordinator.submit("source-1", "target.app", null)
        val terminal = awaitTerminal(coordinator) as ConfigurationEditState.Completed

        assertEquals(false, terminal.appliedLocaleChange)
        assertEquals(terminal.command.operationId, terminal.newConfigurationId)
        assertEquals(0, env.applyCount)
        assertEquals(1, env.saveCount)
        assertEquals(0, env.pendingWriteCount)
    }

    @Test
    fun applyFailureClearsTheRecordAndReleasesTheGate() {
        val env = FakeEnvironment().apply {
            sources["source-1"] = sourceConfig()
            currentTag = ConfigurationEditEnvironment.TagRead.Known("ja")
            applyOutcome = ConfigurationEditEnvironment.ApplyOutcome.Failed
        }
        val coordinator = ConfigurationEditCoordinator(env)

        coordinator.submit("source-1", "target.app", "en")
        val terminal = awaitTerminal(coordinator)

        assertTrue(terminal is ConfigurationEditState.ApplyFailed)
        assertEquals(0, env.saveCount)
        assertEquals(1, env.clearPendingCount)
        coordinator.acknowledge()
        assertEquals(ConfigurationEditState.Idle, coordinator.state.value)
        // The gate is free again: a new command is admitted.
        env.applyOutcome = ConfigurationEditEnvironment.ApplyOutcome.Applied
        coordinator.submit("source-1", "target.app", "en")
        assertTrue(awaitTerminal(coordinator) is ConfigurationEditState.Completed)
        assertEquals(2, env.applyCount)
    }

    @Test
    fun unclearOutcomeThatRereadMatchesCompletesTheSaveWithoutAClaim() {
        val env = FakeEnvironment().apply {
            sources["source-1"] = sourceConfig()
            currentTag = ConfigurationEditEnvironment.TagRead.Known("ja")
            applyOutcome = ConfigurationEditEnvironment.ApplyOutcome.Unclear
            unclearLanded = true
        }
        val coordinator = ConfigurationEditCoordinator(env)

        coordinator.submit("source-1", "target.app", "en")
        val terminal = awaitTerminal(coordinator) as ConfigurationEditState.Completed

        assertEquals(1, env.applyCount)
        assertEquals(1, env.saveCount)
        assertEquals(terminal.command.operationId, terminal.newConfigurationId)
    }

    @Test
    fun unclearOutcomeThatRereadDisagreesKeepsPendingAndBlocksNewCommands() {
        val env = FakeEnvironment().apply {
            sources["source-1"] = sourceConfig()
            currentTag = ConfigurationEditEnvironment.TagRead.Known("ja")
            applyOutcome = ConfigurationEditEnvironment.ApplyOutcome.Unclear
        }
        val coordinator = ConfigurationEditCoordinator(env)

        coordinator.submit("source-1", "target.app", "en")
        // The reread still says ja (the fake keeps currentTag), not en.
        assertTrue(awaitTerminal(coordinator) is ConfigurationEditState.OutcomeUnknown)
        assertEquals(0, env.saveCount)
        assertEquals(0, env.clearPendingCount)
        assertTrue("the durable record survives", env.pendingRecord != null)

        // The held gate drops a new submission AND keeps the pending outcome
        // visible — a busy rejection must never erase the banner state.
        coordinator.submit("source-1", "other.app", "en")
        assertTrue(
            coordinator.state.value is ConfigurationEditState.OutcomeUnknown,
        )

        // An explicit discard releases everything and clears the record; the
        // language is never rolled back (no extra apply).
        val appliesBeforeDiscard = env.applyCount
        coordinator.discardPendingEdit()
        awaitTrue { env.clearPendingCount == 1 }
        assertEquals(appliesBeforeDiscard, env.applyCount)
    }

    @Test
    fun saveFailureKeepsPendingAndRetrySavesOnlyWithoutReapplying() {
        val env = FakeEnvironment().apply {
            sources["source-1"] = sourceConfig()
            currentTag = ConfigurationEditEnvironment.TagRead.Known("ja")
            saveOutcome = ConfigurationEditEnvironment.SaveOutcome.Failed
        }
        val coordinator = ConfigurationEditCoordinator(env)

        coordinator.submit("source-1", "target.app", "en")
        val pending = awaitTerminal(coordinator)
        assertTrue(pending is ConfigurationEditState.AppliedPendingSave)
        assertEquals(1, env.applyCount)
        assertEquals(1, env.saveCount)
        assertEquals("record kept for the retry", 1, env.pendingWriteCount)

        // Retrying the save must not call apply again and must reuse the id.
        env.saveOutcome = ConfigurationEditEnvironment.SaveOutcome.Saved
        coordinator.retrySave()
        val done = await(coordinator) { it is ConfigurationEditState.Completed }
            as ConfigurationEditState.Completed
        assertEquals(1, env.applyCount)
        assertEquals(2, env.saveCount)
        assertEquals(done.command.operationId, env.savedSnapshots.last().first.id)
        assertEquals(done.command.operationId, env.savedSnapshots.last().second)
    }

    @Test
    fun doubleSubmitIsRejectedAtomicallyWhileAPendingSaveHoldsTheGate() {
        val env = FakeEnvironment().apply {
            sources["source-1"] = sourceConfig()
            currentTag = ConfigurationEditEnvironment.TagRead.Known("ja")
            saveOutcome = ConfigurationEditEnvironment.SaveOutcome.Failed
        }
        val coordinator = ConfigurationEditCoordinator(env)
        coordinator.submit("source-1", "target.app", "en")
        awaitTerminal(coordinator)

        coordinator.submit("source-1", "target.app", "en")
        // The pending-save outcome stays visible; no second apply happens.
        assertTrue(coordinator.state.value is ConfigurationEditState.AppliedPendingSave)
        assertEquals(1, env.applyCount)
    }

    @Test
    fun unreadableCurrentStateRejectsBeforeAnyWrite() {
        val env = FakeEnvironment().apply {
            sources["source-1"] = sourceConfig()
            currentTag = ConfigurationEditEnvironment.TagRead.Unknown
        }
        val coordinator = ConfigurationEditCoordinator(env)

        coordinator.submit("source-1", "target.app", "en")
        val terminal = awaitTerminal(coordinator) as ConfigurationEditState.Rejected
        assertEquals(ConfigurationEditRejection.CurrentStateUnknown, terminal.reason)
        assertEquals(0, env.applyCount)
        assertEquals(0, env.pendingWriteCount)
        assertEquals(0, env.saveCount)
    }

    @Test
    fun pendingRecoveryCompletesTheSaveWhenTheLanguageStillMatches() {
        val snapshot = SavedLocaleConfiguration(
            id = "op-recover",
            createdAt = 10L,
            entries = listOf(SavedLocaleEntry("target.app", "Target", "en")),
        )
        val env = FakeEnvironment().apply {
            sources["source-1"] = sourceConfig()
            pendingRecord = PendingConfigurationEdit(
                operationId = "op-recover",
                sourceConfigurationId = "source-1",
                packageName = "target.app",
                previousLocaleTag = "ja",
                targetLocaleTag = "en",
                newConfiguration = snapshot,
            )
            currentTag = ConfigurationEditEnvironment.TagRead.Known("en")
        }
        val coordinator = ConfigurationEditCoordinator(env)

        val terminal = awaitTerminal(coordinator) as ConfigurationEditState.Completed
        assertEquals("op-recover", terminal.newConfigurationId)
        assertEquals("recovery never re-applies", 0, env.applyCount)
        assertEquals(1, env.saveCount)
        assertEquals("op-recover", env.savedSnapshots.single().second)
    }

    @Test
    fun pendingRecoveryWithMismatchedLanguageSurfacesOutcomeUnknown() {
        val snapshot = SavedLocaleConfiguration(
            id = "op-mismatch",
            createdAt = 10L,
            entries = listOf(SavedLocaleEntry("target.app", "Target", "en")),
        )
        val env = FakeEnvironment().apply {
            sources["source-1"] = sourceConfig()
            pendingRecord = PendingConfigurationEdit(
                operationId = "op-mismatch",
                sourceConfigurationId = "source-1",
                packageName = "target.app",
                previousLocaleTag = "ja",
                targetLocaleTag = "en",
                newConfiguration = snapshot,
            )
            currentTag = ConfigurationEditEnvironment.TagRead.Known("ja")
        }
        val coordinator = ConfigurationEditCoordinator(env)

        assertTrue(awaitTerminal(coordinator) is ConfigurationEditState.OutcomeUnknown)
        assertEquals(0, env.applyCount)
        assertEquals(0, env.saveCount)
        assertTrue("record intact", env.pendingRecord != null)
    }

    @Test
    fun acknowledgingATerminalStateIsIdempotent() {
        val env = FakeEnvironment().apply {
            sources["source-1"] = sourceConfig()
            currentTag = ConfigurationEditEnvironment.TagRead.Known("ja")
        }
        val coordinator = ConfigurationEditCoordinator(env)
        coordinator.submit("source-1", "target.app", "en")
        awaitTerminal(coordinator)
        coordinator.acknowledge()
        coordinator.acknowledge()
        assertEquals(ConfigurationEditState.Idle, coordinator.state.value)
    }
}
