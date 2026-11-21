package dev.arcturus.memo.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.arcturus.memo.data.NoteEntity
import dev.arcturus.memo.data.KeepRepository
import dev.arcturus.memo.ui.theme.KeepDarkColors
import dev.arcturus.memo.ui.theme.KeepLightColors

@Composable
fun KeepSearchScreen(notes: List<NoteEntity>, repository: KeepRepository, isDark: Boolean, onBack: () -> Unit, onOpenNote: (String) -> Unit) {
    val colors = if (isDark) KeepDarkColors else KeepLightColors
    var query by remember { mutableStateOf("") }
    val results = notes.filter {
        query.isBlank() || it.title.contains(query, ignoreCase = true) || it.body.contains(query, ignoreCase = true)
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = colors.secondaryForeground)
            }
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 16.dp),
                singleLine = true,
                textStyle = TextStyle(color = colors.foreground, fontSize = 18.sp),
                decorationBox = { inner ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (query.isEmpty()) Text("Search Arcturus Memo", color = colors.secondaryForeground, fontSize = 18.sp)
                        inner()
                    }
                },
            )
            if (query.isNotEmpty()) IconButton(onClick = { query = "" }) {
                Icon(Icons.Filled.Clear, contentDescription = "Clear search", tint = colors.secondaryForeground)
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(results, key = { it.id }) { note ->
                NotePreviewCard(note, repository, isDark, onClick = { onOpenNote(note.id) })
            }
        }
    }
}
