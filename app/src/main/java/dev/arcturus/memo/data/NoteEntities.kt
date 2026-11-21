package dev.arcturus.memo.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

@Entity(
    tableName = "notes",
    indices = [Index("isArchived"), Index("isTrashed"), Index("isPinned"), Index("updatedAt")],
)
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String = "",
    val body: String = "",
    val isChecklist: Boolean = false,
    val colorName: String = "Default",
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isTrashed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = createdAt,
    val reminderAt: Long? = null,
    val attachmentType: String? = null,
    val attachmentPath: String? = null,
)

@Entity(
    tableName = "checklist_items",
    foreignKeys = [ForeignKey(
        entity = NoteEntity::class,
        parentColumns = ["id"],
        childColumns = ["noteId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("noteId"), Index(value = ["noteId", "position"], unique = true)],
)
data class ChecklistItemEntity(
    @PrimaryKey val id: String,
    val noteId: String,
    val text: String,
    val isChecked: Boolean = false,
    val position: Int,
)

@Entity(tableName = "labels", indices = [Index(value = ["name"], unique = true)])
data class LabelEntity(
    @PrimaryKey val id: String,
    val name: String,
    val colorName: String = "Default",
)

@Entity(
    tableName = "note_labels",
    primaryKeys = ["noteId", "labelId"],
    foreignKeys = [
        ForeignKey(NoteEntity::class, ["id"], ["noteId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(LabelEntity::class, ["id"], ["labelId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("labelId")],
)
data class NoteLabelCrossRef(val noteId: String, val labelId: String)

data class NoteWithRelations(
    @Embedded val note: NoteEntity,
    @Relation(parentColumn = "id", entityColumn = "noteId")
    val checklistItems: List<ChecklistItemEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = NoteLabelCrossRef::class,
            parentColumn = "noteId",
            entityColumn = "labelId",
        ),
    )
    val labels: List<LabelEntity>,
)
