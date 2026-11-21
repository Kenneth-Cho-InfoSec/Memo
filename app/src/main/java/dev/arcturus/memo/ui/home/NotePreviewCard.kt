package dev.arcturus.memo.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.arcturus.memo.data.NoteEntity
import dev.arcturus.memo.data.KeepRepository
import dev.arcturus.memo.ui.theme.KeepDarkColors
import dev.arcturus.memo.ui.theme.KeepLightColors
import dev.arcturus.memo.ui.theme.KeepNoteBackgrounds
import dev.arcturus.memo.ui.theme.KeepNoteBackgroundAssets
import dev.arcturus.memo.ui.AttachmentPreview

@Composable
fun NotePreviewCard(note: NoteEntity, repository: KeepRepository, isDark: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val checklistItems by androidx.compose.runtime.remember(repository, note.id) {
        repository.checklistItems(note.id)
    }.collectAsState(initial = emptyList())
    val colors = if (isDark) KeepDarkColors else KeepLightColors
    val artBackground = KeepNoteBackgroundAssets.firstOrNull { it.selectionKey == note.colorName }
    val noteColors = KeepNoteBackgrounds[note.colorName] ?: KeepNoteBackgrounds.getValue("Default")
    val background = if (isDark) noteColors.dark else noteColors.light
    val defaultColor = note.colorName == "Default"
    val foreground = when {
        defaultColor -> colors.foreground
        artBackground != null -> if (isDark) Color(0xFFE8EAED) else Color(0xFF202124)
        isDark -> Color(0xFFE8EAED)
        else -> Color(0xFF202124)
    }
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        border = BorderStroke(1.dp, colors.divider.copy(alpha = if (defaultColor) 0.35f else 0.18f)),
    ) {
        androidx.compose.foundation.layout.Box {
            if (artBackground != null) {
                Image(
                    painter = painterResource(if (isDark) artBackground.night else artBackground.day),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            if (note.isPinned) {
                Icon(
                    Icons.Filled.PushPin,
                    contentDescription = "Pinned",
                    modifier = Modifier.align(Alignment.End),
                    tint = colors.secondaryForeground,
                )
            }
            note.attachmentPath?.let { path ->
                AttachmentPreview(
                    path,
                    note.attachmentType ?: "Image",
                    Modifier.fillMaxWidth().height(if (note.attachmentType == "Audio") 84.dp else 180.dp)
                        .clip(RoundedCornerShape(8.dp)),
                )
            }
            if (note.title.isNotBlank()) {
                Text(note.title, color = foreground, fontSize = 16.sp, fontWeight = FontWeight.Medium, maxLines = 5)
            }
            if (note.body.isNotBlank()) {
                Text(
                    note.body,
                    modifier = Modifier.padding(top = if (note.title.isNotBlank()) 8.dp else 0.dp),
                    color = foreground,
                    fontSize = 14.sp,
                    maxLines = 10,
                )
            }
            if (note.isChecklist) {
                val items = checklistItems.take(3)
                if (items.isEmpty()) {
                    Text(
                        "☐  List item",
                        modifier = Modifier.padding(top = if (note.title.isNotBlank() || note.body.isNotBlank()) 8.dp else 0.dp),
                        color = foreground,
                        fontSize = 14.sp,
                    )
                } else {
                    items.forEachIndexed { index, item ->
                        Text(
                            text = (if (item.isChecked) "☑  " else "☐  ") + item.text,
                            modifier = Modifier.padding(top = if (index == 0 && (note.title.isNotBlank() || note.body.isNotBlank())) 8.dp else 2.dp),
                            color = foreground.copy(alpha = if (item.isChecked) 0.65f else 1f),
                            fontSize = 14.sp,
                            maxLines = 2,
                            textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                        )
                    }
                    if (checklistItems.size > 3) Text("+${checklistItems.size - 3} more", color = foreground.copy(alpha = 0.65f), fontSize = 12.sp)
                }
                } else if (note.title.isBlank() && note.body.isBlank() && note.attachmentPath == null) {
                    Text("Note", color = foreground, fontSize = 14.sp)
                }
            }
        }
    }
}
