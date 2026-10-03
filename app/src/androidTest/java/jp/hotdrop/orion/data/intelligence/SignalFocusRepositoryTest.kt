package jp.hotdrop.orion.data.intelligence

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import android.content.Context
import jp.hotdrop.orion.data.SignalFocusRepository
import jp.hotdrop.orion.data.local.OrionDatabase
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue

class SignalFocusRepositoryTest {
    private lateinit var db: OrionDatabase
    private lateinit var repository: SignalFocusRepository
    private var time = 100L
    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            OrionDatabase::class.java,
        ).build()
        repository = SignalFocusRepository(db) { time }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun crudKeepsCreationTimeAndNormalizesKeywords() = runBlocking {
        val id = repository.saveSignal(null, " original ", "ＲＬＣＤ\nrlcd")
        time = 200
        repository.saveSignal(id, "updated", "RLHF")
        val signal = repository.getSignal(id)!!
        assertEquals(100L, signal.createdAt)
        assertEquals(200L, signal.updatedAt)
        assertEquals(listOf("RLHF"), signal.keywords)
        repository.deleteSignal(id)
        assertNull(repository.getSignal(id))
    }

    @Test
    fun invalidInputsDoNotWrite() = runBlocking {
        assertTrue(runCatching { repository.saveSignal(null, "  ", "key") }.isFailure)
        assertTrue(repository.archive.first().signals.isEmpty())
        repository.saveSignal(null, "valid", "")
        val draft = repository.beginAnalysis()!!
        assertTrue(runCatching { repository.completeAnalysis(draft) }.isFailure)
        assertNotNull(db.signalFocusDao().draft())
        assertNull(db.signalFocusDao().checkpoint())
    }

    @Test
    fun frozenSnapshotAndIdBoundarySurviveEditsClockRollbackAndDeletion() = runBlocking {
        val first = repository.saveSignal(null, "frozen", "RLCD")
        val draft = repository.beginAnalysis()!!
        repository.saveSignal(first, "changed", "RLHF")
        time = 10 // A new observation can have an earlier wall-clock time.
        val next = repository.saveSignal(null, "next", "RLCD")
        assertEquals(draft, repository.beginAnalysis())
        assertTrue(draft.prompt.contains("frozen"))
        assertFalse(draft.prompt.contains("changed"))
        val focus = repository.completeAnalysis(draft.copy(analysis = "result"))
        repository.deleteSignal(first)
        repository.deleteFocus(focus)
        val second = repository.beginAnalysis()!!
        assertEquals(next, second.upperSignalId)
        assertEquals(1, second.signalCount)
        assertTrue(second.prompt.contains("next"))
    }

    @Test
    fun sameTimestampSignalsAreNotSkipped() = runBlocking {
        repository.saveSignal(null, "one", "")
        val first = repository.beginAnalysis()!!
        val secondId = repository.saveSignal(null, "two", "")
        repository.completeAnalysis(first.copy(analysis = "result"))
        assertEquals(secondId, repository.beginAnalysis()!!.upperSignalId)
    }

    @Test
    fun discardDoesNotAdvanceAndEmptyDoesNotCreateDraft() = runBlocking {
        assertNull(repository.beginAnalysis())
        repository.saveSignal(null, "one", "")
        val draft = repository.beginAnalysis()!!
        repository.discardDraft(draft.token)
        assertNull(db.signalFocusDao().checkpoint())
        assertEquals(draft.prompt, repository.beginAnalysis()!!.prompt)
    }

    @Test
    fun linksUseAllHistoryAndReactToKeywordEditsAndDeletes() = runBlocking {
        val old = repository.saveSignal(null, "old", "ＲＬＣＤ\nRLHF")
        repository.completeAnalysis(repository.beginAnalysis()!!.copy(analysis = "first", keywords = "Other"))
        repository.saveSignal(null, "new", "Jev")
        val secondDraft = repository.beginAnalysis()!!.copy(
            analysis = "second",
            keywords = "rlcd\nRLHF",
        )
        val focus = repository.completeAnalysis(secondDraft)
        val linked = db.signalFocusDao()
            .observeLinks()
            .first()
            .filter { it.focusId == focus }
        assertEquals(listOf(old), linked.map { it.signalId }) // DISTINCT even with two shared keywords.
        repository.updateFocus(focus, "edited", "note", "new key")
        assertTrue(db.signalFocusDao().observeLinks().first().none { it.focusId == focus })
        repository.saveSignal(old, "old edited", "NEW KEY")
        assertTrue(db.signalFocusDao().observeLinks().first().any { it.signalId == old && it.focusId == focus })
        repository.deleteSignal(old)
        assertTrue(db.signalFocusDao().observeLinks().first().none { it.signalId == old })
    }

    @Test
    fun draftUpdatesRestoreAllFieldsAndCannotResurrectAfterCommit() = runBlocking {
        repository.saveSignal(null, "one", "")
        val draft = repository.beginAnalysis()!!.copy(
            analysis = "answer",
            note = "my note",
            keywords = "RLCD",
            stage = 1,
        )
        repository.saveDraft(draft)
        val recreated = SignalFocusRepository(db)
        assertEquals(draft, recreated.beginAnalysis())
        val id = repository.completeAnalysis(draft)
        assertTrue(runCatching { repository.saveDraft(draft.copy(note = "late")) }.isFailure)
        assertTrue(runCatching { repository.completeAnalysis(draft) }.isFailure)
        assertNull(db.signalFocusDao().draft())
        assertEquals("my note", repository.getFocus(id)!!.note)
        assertNull(repository.beginAnalysis())
    }

    @Test
    fun saveRollbackKeepsDraftAndCheckpointRetrySucceeds() = runBlocking {
        repository.saveSignal(null, "one", "")
        val draft = repository.beginAnalysis()!!.copy(analysis = "result", keywords = "RLCD")
        repository.saveDraft(draft)
        db.openHelper.writableDatabase.execSQL(
            """
            CREATE TRIGGER fail_checkpoint BEFORE INSERT ON focus_checkpoint
            BEGIN
                SELECT RAISE(ABORT, 'test failure');
            END
            """.trimIndent(),
        )
        assertTrue(runCatching { repository.completeAnalysis(draft) }.isFailure)
        assertTrue(db.signalFocusDao().observeFocuses().first().isEmpty())
        assertNotNull(db.signalFocusDao().draft())
        assertNull(db.signalFocusDao().checkpoint())
        db.openHelper.writableDatabase.execSQL("DROP TRIGGER fail_checkpoint")
        repository.completeAnalysis(draft)
        assertEquals(1, db.signalFocusDao().observeFocuses().first().size)
    }

    @Test
    fun concurrentSubmitCreatesOneFocus() = runBlocking {
        repository.saveSignal(null, "one", "")
        val draft = repository.beginAnalysis()!!.copy(analysis = "result")
        val results = coroutineScope {
            val firstSubmit = async { runCatching { repository.completeAnalysis(draft) } }
            val secondSubmit = async { runCatching { repository.completeAnalysis(draft) } }
            listOf(firstSubmit, secondSubmit).awaitAll()
        }
        assertEquals(1, results.count { it.isSuccess })
        assertEquals(1, db.signalFocusDao().observeFocuses().first().size)
    }

    @Test
    fun persistedDraftSurvivesDatabaseReopen() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "signal-focus-draft-reopen-test.db"
        context.deleteDatabase(name)
        var disk = Room.databaseBuilder(context, OrionDatabase::class.java, name)
            .build()
        try {
            val first = SignalFocusRepository(disk)
            first.saveSignal(null, "persisted signal", "RLCD")
            val draft = first.beginAnalysis()!!.copy(
                analysis = "answer",
                note = "thoughts",
                keywords = "RLCD",
                stage = 1,
            )
            first.saveDraft(draft)
            disk.close()
            disk = Room.databaseBuilder(context, OrionDatabase::class.java, name)
                .build()
            assertEquals(draft, SignalFocusRepository(disk).beginAnalysis())
        } finally {
            disk.close()
            context.deleteDatabase(name)
        }
    }
}
