package dev.arcturus.memo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.lifecycleScope
import dev.arcturus.memo.reminders.ReminderScheduler
import dev.arcturus.memo.ui.KeepApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val pendingNoteId = mutableStateOf<String?>(null)
    private val pendingNoteToken = mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        acceptReminderIntent(intent)
        lifecycleScope.launch(Dispatchers.IO) {
            ReminderScheduler.rescheduleAll(applicationContext)
        }
        enableEdgeToEdge()
        setContent {
            KeepApp(
                launchNoteId = pendingNoteId.value,
                launchNoteToken = pendingNoteToken.intValue,
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        acceptReminderIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch(Dispatchers.IO) {
            ReminderScheduler.rescheduleAll(applicationContext)
        }
    }

    private fun acceptReminderIntent(intent: Intent?) {
        val noteId = intent?.getStringExtra(ReminderScheduler.EXTRA_NOTE_ID) ?: return
        pendingNoteId.value = noteId
        pendingNoteToken.intValue += 1
    }
}
