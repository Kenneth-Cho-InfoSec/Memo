package dev.arcturus.memo.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface KeepDao {
    @Query("SELECT * FROM notes WHERE isArchived = 0 AND isTrashed = 0 AND (length(trim(title)) > 0 OR length(trim(body)) > 0 OR isChecklist = 1 OR attachmentPath IS NOT NULL) ORDER BY isPinned DESC, updatedAt DESC")
    fun observeActiveNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isArchived = 1 AND isTrashed = 0 ORDER BY updatedAt DESC")
    fun observeArchivedNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isTrashed = 1 ORDER BY updatedAt DESC")
    fun observeTrashedNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE reminderAt IS NOT NULL AND isArchived = 0 AND isTrashed = 0 ORDER BY reminderAt")
    fun observeReminderNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE reminderAt IS NOT NULL AND isTrashed = 0 ORDER BY reminderAt")
    suspend fun getReminderNotes(): List<NoteEntity>

    @Query("SELECT n.* FROM notes n INNER JOIN note_labels nl ON n.id = nl.noteId WHERE nl.labelId = :labelId AND n.isArchived = 0 AND n.isTrashed = 0 ORDER BY n.isPinned DESC, n.updatedAt DESC")
    fun observeNotesForLabel(labelId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :noteId LIMIT 1")
    fun observeNote(noteId: String): Flow<NoteEntity?>

    @Query("SELECT * FROM notes WHERE id = :noteId LIMIT 1")
    suspend fun getNote(noteId: String): NoteEntity?

    @Upsert
    suspend fun saveNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNote(noteId: String)

    @Query("SELECT * FROM checklist_items WHERE noteId = :noteId ORDER BY position")
    fun observeChecklistItems(noteId: String): Flow<List<ChecklistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveChecklistItem(item: ChecklistItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveChecklistItems(items: List<ChecklistItemEntity>)

    @Query("DELETE FROM checklist_items WHERE id = :itemId")
    suspend fun deleteChecklistItem(itemId: String)

    @Query("DELETE FROM checklist_items WHERE noteId = :noteId")
    suspend fun deleteChecklistItems(noteId: String)

    @Transaction
    suspend fun replaceChecklistItems(noteId: String, items: List<ChecklistItemEntity>) {
        deleteChecklistItems(noteId)
        saveChecklistItems(items)
    }

    @Query("UPDATE checklist_items SET isChecked = :checked WHERE id = :itemId")
    suspend fun setChecklistItemChecked(itemId: String, checked: Boolean)

    @Query("SELECT * FROM labels ORDER BY name COLLATE NOCASE")
    fun observeLabels(): Flow<List<LabelEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertLabel(label: LabelEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveNoteLabel(crossRef: NoteLabelCrossRef)

    @Query("DELETE FROM note_labels WHERE noteId = :noteId AND labelId = :labelId")
    suspend fun removeNoteLabel(noteId: String, labelId: String)

    @Transaction
    @Query("SELECT * FROM notes WHERE id = :noteId LIMIT 1")
    fun observeNoteWithRelations(noteId: String): Flow<NoteWithRelations?>
}
