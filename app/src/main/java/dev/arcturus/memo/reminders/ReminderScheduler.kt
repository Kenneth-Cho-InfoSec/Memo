package dev.arcturus.memo.reminders

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dev.arcturus.memo.MainActivity
import dev.arcturus.memo.R
import dev.arcturus.memo.data.KeepDatabase
import dev.arcturus.memo.data.NoteEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object ReminderScheduler {
    const val CHANNEL_ID = "note-reminders"

    fun reconcile(context: Context, note: NoteEntity) {
        val reminderAt = note.reminderAt
        if (reminderAt == null || note.isTrashed || !canPostNotifications(context)) cancel(context, note.id)
        else schedule(context, note.id, reminderAt)
    }

    fun schedule(context: Context, noteId: String, reminderAt: Long) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            reminderAt,
            alarmIntent(context, noteId),
        )
    }

    fun cancel(context: Context, noteId: String) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val pending = alarmIntent(context, noteId)
        alarmManager.cancel(pending)
        pending.cancel()
    }

    suspend fun rescheduleAll(context: Context) {
        val canPost = canPostNotifications(context)
        KeepDatabase.get(context).keepDao().getReminderNotes().forEach { note ->
            if (canPost) schedule(context, note.id, note.reminderAt ?: return@forEach)
            else cancel(context, note.id)
        }
    }

    fun canPostNotifications(context: Context): Boolean =
        Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Note reminders",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply { description = "Alerts for reminders saved in Arcturus Memo" },
        )
    }

    private fun alarmIntent(context: Context, noteId: String): PendingIntent {
        val intent = Intent(context, NoteReminderReceiver::class.java)
            .setAction(ACTION_FIRE_REMINDER)
            .putExtra(EXTRA_NOTE_ID, noteId)
        return PendingIntent.getBroadcast(
            context,
            noteId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    const val ACTION_FIRE_REMINDER = "dev.arcturus.memo.action.FIRE_REMINDER"
    const val EXTRA_NOTE_ID = "dev.arcturus.memo.extra.NOTE_ID"
}

class NoteReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val noteId = intent.getStringExtra(ReminderScheduler.EXTRA_NOTE_ID) ?: return
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val note = KeepDatabase.get(context).keepDao().getNote(noteId) ?: return@launch
                if (note.isTrashed) return@launch
                val reminderAt = note.reminderAt ?: return@launch
                if (reminderAt > System.currentTimeMillis()) {
                    ReminderScheduler.schedule(context, note.id, reminderAt)
                    return@launch
                }

                if (
                    Build.VERSION.SDK_INT >= 33 &&
                    ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.POST_NOTIFICATIONS,
                    ) != PackageManager.PERMISSION_GRANTED
                ) return@launch

                ReminderScheduler.ensureChannel(context)
                val openNoteIntent = Intent(context, MainActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    .putExtra(ReminderScheduler.EXTRA_NOTE_ID, note.id)
                val openNote = PendingIntent.getActivity(
                    context,
                    note.id.hashCode(),
                    openNoteIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                val body = note.body.lineSequence().firstOrNull { it.isNotBlank() }?.trim().orEmpty()
                val notification = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_lightbulb_outline_yellow500_120)
                    .setContentTitle(note.title.ifBlank { "Arcturus Memo reminder" })
                    .setContentText(body.ifBlank { "Tap to open your note" })
                    .setStyle(NotificationCompat.BigTextStyle().bigText(body.ifBlank { "Tap to open your note" }))
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(openNote)
                    .build()
                try {
                    NotificationManagerCompat.from(context).notify(note.id.hashCode(), notification)
                } catch (_: SecurityException) {
                    // Notification permission can be revoked after an alarm was scheduled.
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}

class ReminderBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                ReminderScheduler.rescheduleAll(context)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
