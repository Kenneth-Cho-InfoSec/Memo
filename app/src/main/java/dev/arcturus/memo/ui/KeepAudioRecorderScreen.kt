package dev.arcturus.memo.ui

import android.Manifest
import android.content.Context
import android.media.MediaRecorder
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.arcturus.memo.ui.theme.KeepDarkColors
import dev.arcturus.memo.ui.theme.KeepLightColors
import kotlinx.coroutines.delay
import java.io.File
import java.util.UUID

@Composable
fun KeepAudioRecorderScreen(isDark: Boolean, onCancel: () -> Unit, onSave: (String) -> Unit) {
    val context = LocalContext.current
    val colors = if (isDark) KeepDarkColors else KeepLightColors
    val outputFile = remember(context) {
        File(File(context.filesDir, "attachments").apply { mkdirs() }, "${UUID.randomUUID()}.m4a")
    }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var recording by remember { mutableStateOf(false) }
    var hasRecording by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableStateOf(0) }
    var errorText by remember { mutableStateOf<String?>(null) }

    fun stopRecording() {
        val active = recorder ?: return
        try {
            active.stop()
            hasRecording = outputFile.exists() && outputFile.length() > 0
            if (!hasRecording) errorText = "No audio was captured. Try recording again."
        } catch (_: RuntimeException) {
            outputFile.delete()
            hasRecording = false
            errorText = "The recording could not be saved. Try again."
        } finally {
            active.release()
            recorder = null
            recording = false
        }
    }

    fun startRecording() {
        if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            errorText = "Microphone permission is required to record audio"
            return
        }
        var active: MediaRecorder? = null
        try {
            outputFile.delete()
            elapsedSeconds = 0
            hasRecording = false
            errorText = null
            active = createRecorder(context)
            active.setAudioSource(MediaRecorder.AudioSource.MIC)
            active.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            active.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            active.setAudioSamplingRate(44100)
            active.setAudioEncodingBitRate(96000)
            active.setOutputFile(outputFile.absolutePath)
            active.prepare()
            active.start()
            recorder = active
            recording = true
        } catch (error: Exception) {
            active?.release()
            recorder?.release()
            recorder = null
            outputFile.delete()
            recording = false
            errorText = "Unable to start audio recording"
        }
    }

    BackHandler {
        if (recording) stopRecording()
        onCancel()
    }

    DisposableEffect(outputFile) {
        onDispose {
            recorder?.let { active ->
                if (recording) runCatching { active.stop() }
                active.release()
            }
            if (!saved) outputFile.delete()
        }
    }

    LaunchedEffect(recording) {
        while (recording) {
            delay(1_000)
            elapsedSeconds += 1
        }
    }

    Column(Modifier.fillMaxSize().background(if (isDark) colors.settingsBackground else colors.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = {
                if (recording) stopRecording()
                onCancel()
            }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Cancel audio recording", tint = colors.foreground)
            }
            Text("Audio", modifier = Modifier.padding(start = 8.dp), color = colors.foreground, fontSize = 18.sp)
        }

        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(176.dp).background(
                        color = if (recording) Color(0xFFFA8072).copy(alpha = 0.18f) else colors.searchSurface,
                        shape = CircleShape,
                    ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (recording) Icons.Filled.Stop else Icons.Filled.Mic,
                        contentDescription = if (recording) "Stop recording" else "Start recording",
                        tint = if (recording) Color(0xFFCC3B32) else colors.secondaryForeground,
                        modifier = Modifier.size(72.dp),
                    )
                }
                Spacer(Modifier.height(28.dp))
                Text(
                    text = "%02d:%02d".format(elapsedSeconds / 60, elapsedSeconds % 60),
                    color = colors.foreground,
                    fontSize = 32.sp,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = when {
                        recording -> "Recording"
                        hasRecording -> "Recording ready"
                        else -> "Tap to record"
                    },
                    color = colors.secondaryForeground,
                    fontSize = 14.sp,
                )
                errorText?.let {
                    Text(it, modifier = Modifier.padding(top = 16.dp), color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = {
                if (recording) stopRecording()
                outputFile.delete()
                onCancel()
            }) { Text("Cancel") }
            Button(
                onClick = { if (recording) stopRecording() else startRecording() },
                enabled = true,
            ) {
                Text(if (recording) "Stop" else "Record")
            }
            Button(
                onClick = {
                    if (recording) stopRecording()
                    if (hasRecording && outputFile.length() > 0) {
                        saved = true
                        onSave(outputFile.absolutePath)
                    }
                },
                enabled = hasRecording && !recording,
            ) { Text("Save") }
        }
    }
}

@Suppress("DEPRECATION")
private fun createRecorder(context: Context): MediaRecorder =
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) MediaRecorder(context) else MediaRecorder()
