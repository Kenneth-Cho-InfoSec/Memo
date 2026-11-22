package dev.arcturus.memo.data

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [NoteEntity::class, ChecklistItemEntity::class, LabelEntity::class, NoteLabelCrossRef::class],
    version = 3,
    exportSchema = true,
)
abstract class KeepDatabase : RoomDatabase() {
    abstract fun keepDao(): KeepDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE notes ADD COLUMN attachmentType TEXT")
                database.execSQL("ALTER TABLE notes ADD COLUMN attachmentPath TEXT")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE checklist_items ADD COLUMN isCheckbox INTEGER NOT NULL DEFAULT 1")
            }
        }

        @Volatile private var instance: KeepDatabase? = null

        fun get(context: Context): KeepDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                KeepDatabase::class.java,
                "keep-local.db",
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build().also { instance = it }
        }
    }
}
