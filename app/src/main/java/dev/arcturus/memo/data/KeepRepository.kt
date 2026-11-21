package dev.arcturus.memo.data

import java.util.UUID

class KeepRepository(private val dao: KeepDao) {
    fun activeNotes() = dao.observeActiveNotes()
    fun archivedNotes() = dao.observeArchivedNotes()
    fun trashedNotes() = dao.observeTrashedNotes()
    fun reminderNotes() = dao.observeReminderNotes()
    fun notesForLabel(labelId: String) = dao.observeNotesForLabel(labelId)
    fun labels() = dao.observeLabels()
    fun note(noteId: String) = dao.observeNote(noteId)
    fun noteWithRelations(noteId: String) = dao.observeNoteWithRelations(noteId)
    fun checklistItems(noteId: String) = dao.observeChecklistItems(noteId)

    suspend fun createNote(checklist: Boolean = false): NoteEntity {
        val now = System.currentTimeMillis()
        return NoteEntity(
            id = UUID.randomUUID().toString(),
            isChecklist = checklist,
            createdAt = now,
            updatedAt = now,
        ).also { dao.saveNote(it) }
    }

    suspend fun saveNote(note: NoteEntity) = dao.saveNote(note.copy(updatedAt = System.currentTimeMillis()))
    suspend fun deleteNote(noteId: String) = dao.deleteNote(noteId)
    suspend fun saveItem(item: ChecklistItemEntity) = dao.saveChecklistItem(item)
    suspend fun saveItems(items: List<ChecklistItemEntity>) = dao.saveChecklistItems(items)
    suspend fun deleteItem(itemId: String) = dao.deleteChecklistItem(itemId)
    suspend fun deleteItems(noteId: String) = dao.deleteChecklistItems(noteId)
    suspend fun replaceItems(noteId: String, items: List<ChecklistItemEntity>) =
        dao.replaceChecklistItems(noteId, items)
    suspend fun checkItem(itemId: String, checked: Boolean) = dao.setChecklistItemChecked(itemId, checked)
    suspend fun createLabel(name: String) = dao.insertLabel(LabelEntity(UUID.randomUUID().toString(), name.trim()))
    suspend fun addLabel(noteId: String, labelId: String) = dao.saveNoteLabel(NoteLabelCrossRef(noteId, labelId))
    suspend fun removeLabel(noteId: String, labelId: String) = dao.removeNoteLabel(noteId, labelId)

    suspend fun setArchived(note: NoteEntity, archived: Boolean) =
        saveNote(note.copy(isArchived = archived, isTrashed = false))

    suspend fun setTrashed(note: NoteEntity, trashed: Boolean) =
        saveNote(note.copy(isTrashed = trashed, isArchived = false))

    suspend fun setPinned(note: NoteEntity, pinned: Boolean) = saveNote(note.copy(isPinned = pinned))
    suspend fun setColor(note: NoteEntity, colorName: String) = saveNote(note.copy(colorName = colorName))
}
