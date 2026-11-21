package dev.arcturus.memo.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AttachmentPreview(path: String, attachmentType: String = "Image", modifier: Modifier = Modifier) {
    if (attachmentType == "Audio") {
        AudioAttachmentPreview(path, modifier)
    } else {
        val bitmap by produceState<Bitmap?>(null, path) {
            value = withContext(Dispatchers.IO) { decodePreview(path) }
        }
        bitmap?.let {
            Image(
                it.asImageBitmap(),
                contentDescription = if (attachmentType == "Drawing") "Drawing attachment" else "Image attachment",
                modifier = modifier,
                contentScale = if (attachmentType == "Drawing") ContentScale.Fit else ContentScale.Crop,
            )
        }
    }
}

@Composable
private fun AudioAttachmentPreview(path: String, modifier: Modifier = Modifier) {
    var player by remember(path) { mutableStateOf<MediaPlayer?>(null) }
    var playing by remember(path) { mutableStateOf(false) }
    var prepared by remember(path) { mutableStateOf(false) }
    DisposableEffect(path) {
        onDispose {
            player?.release()
            player = null
            playing = false
            prepared = false
        }
    }

    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = {
            val current = player
            when {
                current == null -> {
                    val created = MediaPlayer()
                    try {
                        created.setDataSource(path)
                        created.setOnPreparedListener { mediaPlayer ->
                            prepared = true
                            mediaPlayer.start()
                            playing = true
                        }
                        created.setOnCompletionListener { mediaPlayer ->
                            playing = false
                            prepared = false
                            mediaPlayer.release()
                            player = null
                        }
                        created.setOnErrorListener { mediaPlayer, _, _ ->
                            playing = false
                            prepared = false
                            mediaPlayer.release()
                            player = null
                            true
                        }
                        player = created
                        created.prepareAsync()
                    } catch (_: Exception) {
                        created.release()
                    }
                }
                current.isPlaying -> { current.pause(); playing = false }
                prepared -> { current.start(); playing = true }
            }
        }) {
            Icon(
                if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (playing) "Pause audio" else "Play audio",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text("Audio recording", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun decodePreview(path: String): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    var sampleSize = 1
    while (bounds.outWidth / sampleSize > 1200 || bounds.outHeight / sampleSize > 1200) sampleSize *= 2
    return BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sampleSize })
}
