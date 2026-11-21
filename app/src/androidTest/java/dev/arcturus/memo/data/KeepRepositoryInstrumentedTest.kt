package dev.arcturus.memo.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KeepRepositoryInstrumentedTest {
    private lateinit var database: KeepDatabase
    private lateinit var repository: KeepRepository

    @Before
    fun createDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            KeepDatabase::class.java,
        ).allowMainThreadQueries().build()
        repository = KeepRepository(database.keepDao())
    }

    @After
    fun closeDatabase() = database.close()

    @Test
    fun checklistLabelsArchiveTrashAndDeletePersistThroughRoom() = runBlocking {
        val note = repository.createNote(checklist = true)
        repository.saveNote(note.copy(title = "Shopping"))
        repository.saveItem(ChecklistItemEntity("milk", note.id, "Milk", position = 0))
        repository.checkItem("milk", checked = true)
        repository.createLabel("Groceries")
        val label = repository.labels().first().single()
        repository.addLabel(note.id, label.id)

        assertEquals("Shopping", repository.note(note.id).first()?.title)
        assertTrue(repository.checklistItems(note.id).first().single().isChecked)
        assertEquals(listOf(label), repository.noteWithRelations(note.id).first()?.labels)
        assertEquals(listOf(note.id), repository.notesForLabel(label.id).first().map { it.id })

        repository.saveNote(note.copy(title = "Shopping", body = "Updated after label attach"))
        assertEquals(listOf(label), repository.noteWithRelations(note.id).first()?.labels)
        assertEquals(listOf(note.id), repository.notesForLabel(label.id).first().map { it.id })

        repository.setArchived(note.copy(title = "Shopping"), archived = true)
        assertEquals(listOf(note.id), repository.archivedNotes().first().map { it.id })
        repository.setTrashed(note.copy(title = "Shopping", isArchived = true), trashed = true)
        assertEquals(listOf(note.id), repository.trashedNotes().first().map { it.id })

        repository.deleteNote(note.id)
        assertNull(repository.note(note.id).first())
        assertTrue(repository.checklistItems(note.id).first().isEmpty())
        assertTrue(repository.noteWithRelations(note.id).first() == null)
    }

    @Test
    fun reminderTimestampIsQueryableAndTrashSuppressesIt() = runBlocking {
        val note = repository.createNote()
        val reminderAt = System.currentTimeMillis() + 60_000
        repository.saveNote(note.copy(title = "Pick up groceries", reminderAt = reminderAt))

        assertEquals(listOf(note.id), repository.reminderNotes().first().map { it.id })
        assertEquals(reminderAt, repository.note(note.id).first()?.reminderAt)

        repository.setTrashed(note.copy(title = "Pick up groceries", reminderAt = reminderAt), trashed = true)
        assertTrue(repository.reminderNotes().first().isEmpty())

        repository.setTrashed(
            note.copy(title = "Pick up groceries", reminderAt = reminderAt, isTrashed = true),
            trashed = false,
        )
        assertEquals(listOf(note.id), repository.reminderNotes().first().map { it.id })
    }

    @Test
    fun attachmentMetadataSurvivesUnrelatedEditsAndKeepsNotesActive() = runBlocking {
        listOf("Image", "Drawing", "Audio").forEach { type ->
            val note = repository.createNote()
            val path = "/data/user/0/dev.arcturus.memo/files/attachments/synthetic.$type"
            repository.saveNote(note.copy(attachmentType = type, attachmentPath = path))
            repository.saveNote(note.copy(title = "Synthetic $type", attachmentType = type, attachmentPath = path))

            val saved = repository.note(note.id).first()
            assertEquals(type, saved?.attachmentType)
            assertEquals(path, saved?.attachmentPath)
            assertTrue(repository.activeNotes().first().any { it.id == note.id })
        }
    }
}
