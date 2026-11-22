package dev.arcturus.memo.ui.editor

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.io.OutputStreamWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FormatColorReset
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.HideImage
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.arcturus.memo.data.ChecklistItemEntity
import dev.arcturus.memo.data.KeepRepository
import dev.arcturus.memo.data.LabelEntity
import dev.arcturus.memo.data.NoteEntity
import dev.arcturus.memo.reminders.ReminderScheduler
import dev.arcturus.memo.ui.AttachmentPreview
import dev.arcturus.memo.ui.theme.KeepDarkColors
import dev.arcturus.memo.ui.theme.KeepLightColors
import dev.arcturus.memo.ui.theme.KeepNoteBackgroundAssets
import dev.arcturus.memo.ui.theme.KeepNoteBackgrounds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeepNoteEditorScreen(
    noteId: String,
    repository: KeepRepository,
    labels: List<LabelEntity>,
    addNewItemsToBottom: Boolean,
    moveCheckedItemsToBottom: Boolean,
    enableSharing: Boolean,
    isDark: Boolean,
    onBack: () -> Unit,
    onOpenCollaborators: () -> Unit,
    onArchive: (NoteEntity) -> Unit,
    onTrash: (NoteEntity) -> Unit,
    onDeleteForever: (NoteEntity) -> Unit,
) {
    val note by repository.note(noteId).collectAsState(initial = null)
    val relations by repository.noteWithRelations(noteId).collectAsState(initial = null)
    val itemsState by repository.checklistItems(noteId).collectAsState(initial = null)
    val items = itemsState.orEmpty()
    val itemsLoaded = itemsState != null
    val dark = isDark
    val colors = if (dark) KeepDarkColors else KeepLightColors
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    if (note == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Loading note") }
        return
    }
    val current = note!!
    var title by remember(current.id) { mutableStateOf(current.title) }
    var body by remember(current.id) { mutableStateOf(current.body) }
    var colorName by remember(current.id) { mutableStateOf(current.colorName) }
    var pinned by remember(current.id) { mutableStateOf(current.isPinned) }
    var checklistMode by remember(current.id) { mutableStateOf(current.isChecklist) }
    var reminderAt by remember(current.id) { mutableStateOf(current.reminderAt) }
    var paletteSheetOpen by remember { mutableStateOf(false) }
    var overflowSheetOpen by remember { mutableStateOf(false) }
    var formattingSheetOpen by remember { mutableStateOf(false) }
    var reminderSheetOpen by remember { mutableStateOf(false) }
    var findDialogOpen by remember { mutableStateOf(false) }
    var findQuery by remember { mutableStateOf("") }
    var bodyBold by remember { mutableStateOf(false) }
    var bodyItalic by remember { mutableStateOf(false) }
    var bodyUnderline by remember { mutableStateOf(false) }
    var bodyStrikeThrough by remember { mutableStateOf(false) }
    var labelsSheetOpen by remember { mutableStateOf(false) }
    var dateTimePickerPending by remember { mutableStateOf(false) }
    var notificationPermissionGranted by remember {
        mutableStateOf(ReminderScheduler.canPostNotifications(context))
    }
    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        notificationPermissionGranted = granted
        if (!granted) {
            Toast.makeText(context, "Reminder saved, but notifications are disabled", Toast.LENGTH_LONG).show()
        }
    }
    val focusRequester = remember(current.id) { FocusRequester() }
    var itemOptionsOpen by remember { mutableStateOf(false) }
    var pendingFocusItemId by remember { mutableStateOf<String?>(null) }
    val noteBg = KeepNoteBackgrounds[colorName] ?: KeepNoteBackgrounds.getValue("Default")
    val noteSurface = if (dark) noteBg.dark else noteBg.light
    val noteBackgroundArt = KeepNoteBackgroundAssets.firstOrNull { it.selectionKey == colorName }
    val editorCursor = if (colorName == "Default" && !dark) Color.Black else Color.White
    val editorText = when {
        colorName == "Default" -> colors.foreground
        dark -> Color(0xFFE8EAED)
        else -> Color(0xFF202124)
    }
    val attachedLabelIds = relations?.labels?.mapTo(mutableSetOf()) { it.id }.orEmpty()
    val sortedItems = if (moveCheckedItemsToBottom) {
        items.sortedWith(compareBy<ChecklistItemEntity> { it.isChecked }.thenBy { it.position })
    } else items.sortedBy { it.position }

    // If a checklist loses all items, fall back to text mode so the note body
    // remains editable. Guard with a settled check: only when items have loaded
    // AND the note is not in the middle of being converted (body was cleared by
    // showCheckboxes and replacement items may not have arrived yet).
    LaunchedEffect(itemsLoaded, checklistMode, sortedItems.isEmpty(), body) {
        if (itemsLoaded && checklistMode && sortedItems.isEmpty() && body.isNotBlank()) {
            // Body has content but no items - inconsistent; keep text mode.
            checklistMode = false
        }
    }

    LaunchedEffect(itemsLoaded, sortedItems.isNotEmpty()) {
        if (itemsLoaded && sortedItems.isNotEmpty() && !checklistMode) {
            checklistMode = true
            if (body.isBlank()) body = ""
        }
    }

    LaunchedEffect(itemsLoaded, checklistMode, sortedItems) {
        if (itemsLoaded && checklistMode && sortedItems.isNotEmpty() && sortedItems.none { it.isCheckbox }) {
            body = ChecklistConversion.itemsToBodyText(sortedItems.map { it.text })
            checklistMode = false
            scope.launch { repository.deleteItems(current.id) }
        }
    }

    fun setReminder(timestamp: Long?) {
        reminderAt = timestamp
        notificationPermissionGranted = ReminderScheduler.canPostNotifications(context)
        if (
            timestamp != null && Build.VERSION.SDK_INT >= 33 && !notificationPermissionGranted
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun quickReminderAt(hour: Int, minute: Int, daysFromToday: Int): Long {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, daysFromToday)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (calendar.timeInMillis <= System.currentTimeMillis()) calendar.add(Calendar.DAY_OF_YEAR, 1)
        return calendar.timeInMillis
    }

    fun showDateTimePicker() {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = reminderAt ?: System.currentTimeMillis()
        }
        DatePickerDialog(
            context,
            { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        setReminder(calendar.timeInMillis)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    android.text.format.DateFormat.is24HourFormat(context),
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
        ).show()
    }

    LaunchedEffect(dateTimePickerPending) {
        if (dateTimePickerPending) {
            delay(250)
            showDateTimePicker()
            dateTimePickerPending = false
        }
    }

    fun effectiveBody() = if (checklistMode) sortedItems.joinToString("\n") { it.text } else body
    fun effectiveIsChecklist() = if (checklistMode) sortedItems.any { it.isCheckbox } else false

    val itemsSyncKey = sortedItems.joinToString("\u0001") { "${it.id}\u0002${it.text}\u0002${it.isChecked}\u0002${it.isCheckbox}" }
    LaunchedEffect(title, body, colorName, pinned, reminderAt, checklistMode, notificationPermissionGranted, itemsSyncKey) {
        delay(250)
        val updated = current.copy(
            title = title,
            body = effectiveBody(),
            colorName = colorName,
            isPinned = pinned,
            isChecklist = effectiveIsChecklist(),
            reminderAt = reminderAt,
        )
        repository.saveNote(updated)
        ReminderScheduler.reconcile(context, updated)
    }

    fun draftNote() = current.copy(
        title = title,
        body = effectiveBody(),
        colorName = colorName,
        isPinned = pinned,
        isChecklist = effectiveIsChecklist(),
        reminderAt = reminderAt,
    )

    fun closeEditor() {
        scope.launch {
            val bodyValue = effectiveBody()
            val isList = effectiveIsChecklist()
            if (title.isBlank() && bodyValue.isBlank() && items.isEmpty() && !isList && current.attachmentPath == null) {
                repository.deleteNote(current.id)
                ReminderScheduler.cancel(context, current.id)
            } else {
                val updated = current.copy(
                    title = title,
                    body = bodyValue,
                    colorName = colorName,
                    isPinned = pinned,
                    isChecklist = isList,
                    reminderAt = reminderAt,
                )
                repository.saveNote(updated)
                ReminderScheduler.reconcile(context, updated)
            }
            onBack()
        }
    }
    BackHandler(onBack = ::closeEditor)

    fun showCheckboxes() {
        val existingItems = items.sortedBy { it.position }
        val newItems = mutableListOf<ChecklistItemEntity>()
        if (existingItems.isEmpty()) {
            if (body.isNotBlank()) {
                newItems += ChecklistItemEntity(
                    id = UUID.randomUUID().toString(),
                    noteId = current.id,
                    text = body,
                    isCheckbox = false,
                    position = 0,
                )
            }
            newItems += ChecklistItemEntity(
                id = UUID.randomUUID().toString(),
                noteId = current.id,
                text = "",
                isCheckbox = true,
                position = newItems.size,
            )
        } else {
            newItems += existingItems.mapIndexed { index, item -> item.copy(position = index) }
            newItems += ChecklistItemEntity(
                id = UUID.randomUUID().toString(),
                noteId = current.id,
                text = "",
                isCheckbox = true,
                position = newItems.size,
            )
        }
        val focusId = newItems.lastOrNull { it.isCheckbox }?.id
        pendingFocusItemId = focusId
        body = ""
        checklistMode = true
        scope.launch { repository.replaceItems(current.id, newItems) }
    }

    fun hideCheckboxes() {
        val currentItems = items.sortedBy { it.position }
        body = ChecklistConversion.itemsToBodyText(currentItems.map { it.text })
        checklistMode = false
        scope.launch { repository.deleteItems(current.id) }
    }

    fun removeChecklistItem(itemId: String) {
        val remaining = sortedItems.filterNot { it.id == itemId }
        if (remaining.isEmpty()) {
            body = ""
            checklistMode = false
            scope.launch { repository.deleteItems(current.id) }
        } else {
            val index = sortedItems.indexOfFirst { it.id == itemId }
            val adjacent = sortedItems.getOrNull(index - 1) ?: sortedItems.getOrNull(index + 1)
            pendingFocusItemId = adjacent?.id
            scope.launch { repository.deleteItem(itemId) }
        }
    }

    var exportPickerOpen by remember { mutableStateOf(false) }

    fun exportBaseName(): String =
        title.trim().ifBlank { "note" }.replace(Regex("[\\\\/:*?\"<>|\\n\\r\\t]"), "_").take(80).ifBlank { "note" }

    fun noteAsPlainText(): String {
        val header = title.trim()
        val content = if (checklistMode) {
            sortedItems.joinToString("\n") { item ->
                (if (item.isChecked) "[x] " else "[ ] ") + item.text
            }
        } else {
            body
        }
        return listOf(header, content.trimEnd()).filter { it.isNotBlank() }.joinToString("\n\n")
    }

    fun noteAsHtml(): String {
        fun esc(s: String) = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
        val header = title.trim()
        val bodyHtml = if (checklistMode) {
            "<ul>" + sortedItems.joinToString("") { item ->
                val mark = if (item.isChecked) "&#9745; " else "&#9744; "
                "<li>${mark}${esc(item.text)}</li>"
            } + "</ul>"
        } else {
            "<p>" + esc(body).replace("\n", "<br/>") + "</p>"
        }
        val titleHtml = if (header.isNotBlank()) "<h1>${esc(header)}</h1>" else ""
        return "<html><head><meta charset=\"utf-8\"><title>${esc(header.ifBlank { "Note" })}</title></head><body>$titleHtml$bodyHtml</body></html>"
    }

    fun writeTextToUri(uri: android.net.Uri, text: String) {
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        OutputStreamWriter(out, Charsets.UTF_8).use { it.write(text) }
                    } ?: error("Could not open export destination")
                }
            }.onSuccess { Toast.makeText(context, "Exported", Toast.LENGTH_SHORT).show() }
                .onFailure { Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show() }
        }
    }

    fun writePdfToUri(uri: android.net.Uri) {
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val text = noteAsPlainText().ifBlank { "(Empty note)" }
                    val pageWidth = 595
                    val pageHeight = 842
                    val margin = 40f
                    val contentWidth = (pageWidth - margin * 2).toInt()
                    val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = android.graphics.Color.BLACK
                        textSize = 12f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    }
                    val layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, contentWidth)
                        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                        .setLineSpacing(0f, 1.2f)
                        .setIncludePad(false)
                        .build()
                    val doc = PdfDocument()
                    var pageNumber = 1
                    var yOffset = 0
                    val usableHeight = (pageHeight - margin * 2).toInt()
                    while (yOffset < layout.height) {
                        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                        val page = doc.startPage(pageInfo)
                        val canvas = page.canvas
                        canvas.save()
                        canvas.translate(margin, margin - yOffset)
                        layout.draw(canvas)
                        canvas.restore()
                        doc.finishPage(page)
                        yOffset += usableHeight
                        pageNumber++
                    }
                    if (layout.height == 0) {
                        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
                        val page = doc.startPage(pageInfo)
                        doc.finishPage(page)
                    }
                    context.contentResolver.openOutputStream(uri)?.use { out -> doc.writeTo(out) }
                        ?: error("Could not open export destination")
                    doc.close()
                }
            }.onSuccess { Toast.makeText(context, "Exported", Toast.LENGTH_SHORT).show() }
                .onFailure { Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show() }
        }
    }

    val exportTxtLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        if (uri != null) writeTextToUri(uri, noteAsPlainText())
    }
    val exportPdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null) writePdfToUri(uri)
    }
    val exportHtmlLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/html")) { uri ->
        if (uri != null) writeTextToUri(uri, noteAsHtml())
    }
    val exportDocLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/msword")) { uri ->
        if (uri != null) writeTextToUri(uri, noteAsHtml())
    }

    Box(Modifier.fillMaxSize().background(noteSurface)) {
        if (noteBackgroundArt != null) {
            Image(
                painter = painterResource(if (dark) noteBackgroundArt.night else noteBackgroundArt.day),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = ::closeEditor) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = colors.secondaryForeground)
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { pinned = !pinned }) {
                Icon(
                    if (pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    contentDescription = if (pinned) "Unpin note" else "Pin note",
                    tint = colors.secondaryForeground,
                )
            }
            if (enableSharing) {
                IconButton(onClick = onOpenCollaborators) {
                    Icon(Icons.Filled.PersonAdd, contentDescription = "Collaborator", tint = colors.secondaryForeground)
                }
            }
        }

        reminderAt?.let { timestamp ->
            Row(
                modifier = Modifier.fillMaxWidth().height(44.dp).padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.NotificationsNone, contentDescription = null, tint = colors.secondaryForeground)
                Text(
                    "Reminder · ${DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(timestamp))}",
                    modifier = Modifier.weight(1f).padding(start = 12.dp),
                    color = colors.secondaryForeground,
                    fontSize = 14.sp,
                )
                IconButton(onClick = { setReminder(null) }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Remove reminder", tint = colors.secondaryForeground)
                }
            }
        }

        current.attachmentPath?.let { path ->
            AttachmentPreview(
                path,
                current.attachmentType ?: "Image",
                Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    .height(if (current.attachmentType == "Audio") 88.dp else 220.dp)
                    .clip(RoundedCornerShape(12.dp)),
            )
        }

        Column(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.weight(1f).focusRequester(focusRequester),
                    textStyle = TextStyle(color = editorText, fontSize = 22.sp, fontWeight = FontWeight.Medium),
                    cursorBrush = SolidColor(editorCursor),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusRequester.freeFocus() }),
                    decorationBox = { inner ->
                        Box {
                            if (title.isEmpty()) Text("Title", color = editorText.copy(alpha = 0.55f), fontSize = 22.sp)
                            inner()
                        }
                    },
                )
                if (checklistMode) {
                    Box {
                        IconButton(onClick = { itemOptionsOpen = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "List options", tint = colors.secondaryForeground)
                        }
                        DropdownMenu(expanded = itemOptionsOpen, onDismissRequest = { itemOptionsOpen = false }) {
                            DropdownMenuItem(
                                text = { Text("Hide checkboxes") },
                                onClick = {
                                    itemOptionsOpen = false
                                    hideCheckboxes()
                                },
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            AnimatedContent(
                targetState = checklistMode,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                transitionSpec = {
                    (fadeIn(tween(150)) + scaleIn(initialScale = 0.98f, animationSpec = tween(150))) togetherWith
                        (fadeOut(tween(100)) + scaleOut(targetScale = 0.98f, animationSpec = tween(100)))
                },
                label = "note or checklist editor",
            ) { listMode ->
            if (listMode) {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(sortedItems, key = { it.id }) { item ->
                        var itemValue by remember(item.id) { mutableStateOf(TextFieldValue(item.text)) }
                        LaunchedEffect(item.text) {
                            if (item.text != itemValue.text) {
                                val start = itemValue.selection.start.coerceAtMost(item.text.length)
                                val end = itemValue.selection.end.coerceAtMost(item.text.length)
                                itemValue = TextFieldValue(item.text, TextRange(start, end))
                            }
                        }
                        val itemFocus = remember(item.id) { FocusRequester() }
                        var itemFocused by remember(item.id) { mutableStateOf(false) }
                        LaunchedEffect(pendingFocusItemId) {
                            if (pendingFocusItemId == item.id) {
                                itemFocus.requestFocus()
                                pendingFocusItemId = null
                            }
                        }

                        fun splitItemAtCursor() {
                            val (before, after) = ChecklistConversion.splitItem(itemValue.text, itemValue.selection.start)
                            val newId = UUID.randomUUID().toString()
                            val updated = sortedItems.map { existing ->
                                if (existing.id == item.id) existing.copy(text = before) else existing
                            }.toMutableList()
                            val insertAt = (updated.indexOfFirst { it.id == item.id } + 1).coerceIn(0, updated.size)
                            updated.add(
                                insertAt,
                                ChecklistItemEntity(
                                    id = newId,
                                    noteId = current.id,
                                    text = after,
                                    position = insertAt,
                                    isCheckbox = item.isCheckbox,
                                ),
                            )
                            scope.launch {
                                repository.replaceItems(current.id, updated.mapIndexed { index, entry -> entry.copy(position = index) })
                                pendingFocusItemId = newId
                            }
                        }

                        fun deleteEmptyItem(): Boolean {
                            if (itemValue.text.isNotEmpty()) return false
                            removeChecklistItem(item.id)
                            return true
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.animateItem().fillMaxWidth()) {
                            if (item.isCheckbox) {
                                Checkbox(
                                    checked = item.isChecked,
                                    onCheckedChange = { checked -> scope.launch { repository.checkItem(item.id, checked) } },
                                )
                            }
                            val fieldInteraction = remember { MutableInteractionSource() }
                            Box(
                                modifier = Modifier.weight(1f).then(
                                    if (!itemFocused) Modifier.clickable(
                                        interactionSource = fieldInteraction,
                                        indication = null,
                                    ) { itemFocus.requestFocus() } else Modifier
                                ),
                            ) {
                                BasicTextField(
                                    value = itemValue,
                                    onValueChange = { value ->
                                        itemValue = value
                                        scope.launch { repository.saveItem(item.copy(text = value.text)) }
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = if (item.isCheckbox) 12.dp else 8.dp)
                                        .focusRequester(itemFocus)
                                        .onFocusChanged { itemFocused = it.isFocused }
                                        .onPreviewKeyEvent { event ->
                                            if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                                            when (event.key) {
                                                Key.Enter, Key.NumPadEnter -> {
                                                    splitItemAtCursor()
                                                    true
                                                }
                                                Key.Backspace -> deleteEmptyItem()
                                                else -> false
                                            }
                                        },
                                    textStyle = TextStyle(
                                        color = editorText,
                                        fontSize = 16.sp,
                                        textDecoration = if (item.isCheckbox && item.isChecked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                                    ),
                                    cursorBrush = SolidColor(editorCursor),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(onNext = { splitItemAtCursor() }),
                                    decorationBox = { inner ->
                                        Box {
                                            if (itemValue.text.isEmpty()) {
                                                Text(
                                                    if (item.isCheckbox) "List item" else "Note",
                                                    color = editorText.copy(alpha = 0.5f),
                                                    fontSize = 16.sp,
                                                )
                                            }
                                            inner()
                                        }
                                    },
                                )
                            }
                            IconButton(onClick = { removeChecklistItem(item.id) }) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = if (item.isCheckbox) "Delete list item" else "Delete text block",
                                    tint = colors.secondaryForeground,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                    item {
                        Column(modifier = Modifier.fillParentMaxHeight()) {
                            if (sortedItems.isEmpty() || sortedItems.last().isCheckbox) {
                                Text(
                                    "+ List item",
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        scope.launch {
                                            val newId = UUID.randomUUID().toString()
                                            repository.saveItem(
                                                ChecklistItemEntity(
                                                    id = newId,
                                                    noteId = current.id,
                                                    text = "",
                                                    isCheckbox = true,
                                                    position = (items.maxOfOrNull { it.position } ?: -1) + 1,
                                                ),
                                            )
                                            pendingFocusItemId = newId
                                        }
                                    }.padding(start = 60.dp, top = 14.dp, bottom = 14.dp),
                                    color = colors.secondaryForeground,
                                    fontSize = 16.sp,
                                )
                            }
                            if (sortedItems.lastOrNull()?.isCheckbox == true) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().weight(1f).clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                    ) {
                                        scope.launch {
                                            val newId = UUID.randomUUID().toString()
                                            repository.saveItem(
                                                ChecklistItemEntity(
                                                    id = newId,
                                                    noteId = current.id,
                                                    text = "",
                                                    isCheckbox = false,
                                                    position = (items.maxOfOrNull { it.position } ?: -1) + 1,
                                                ),
                                            )
                                            pendingFocusItemId = newId
                                        }
                                    },
                                )
                            } else {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            } else {
                BasicTextField(
                    value = body,
                    onValueChange = { body = it },
                    modifier = Modifier.fillMaxSize().padding(top = 4.dp),
                    textStyle = TextStyle(
                        color = editorText,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        fontWeight = if (bodyBold) FontWeight.Bold else FontWeight.Normal,
                        fontStyle = if (bodyItalic) FontStyle.Italic else FontStyle.Normal,
                        textDecoration = when {
                            bodyUnderline && bodyStrikeThrough -> TextDecoration.combine(
                                listOf(TextDecoration.Underline, TextDecoration.LineThrough),
                            )
                            bodyUnderline -> TextDecoration.Underline
                            bodyStrikeThrough -> TextDecoration.LineThrough
                            else -> TextDecoration.None
                        },
                    ),
                    cursorBrush = SolidColor(editorCursor),
                    decorationBox = { inner ->
                        Box {
                            if (body.isEmpty()) Text("Note", color = editorText.copy(alpha = 0.5f), fontSize = 16.sp)
                            inner()
                        }
                    },
                )
            }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = {
                if (checklistMode) {
                    scope.launch {
                        repository.saveItem(
                            ChecklistItemEntity(
                                id = UUID.randomUUID().toString(),
                                noteId = current.id,
                                text = "",
                                position = (items.maxOfOrNull { it.position } ?: -1) + 1,
                            ),
                        )
                    }
                } else {
                    showCheckboxes()
                }
            }) {
                Icon(Icons.Outlined.AddBox, contentDescription = if (checklistMode) "Add list item" else "Add checklist", tint = colors.secondaryForeground)
            }
            IconButton(onClick = { paletteSheetOpen = true }) {
                Icon(Icons.Outlined.Palette, contentDescription = "Change colour or background", tint = colors.secondaryForeground)
            }
            IconButton(onClick = { formattingSheetOpen = true }) {
                Text(
                    "A",
                    color = colors.secondaryForeground,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    textDecoration = TextDecoration.Underline,
                )
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { overflowSheetOpen = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "More options", tint = colors.secondaryForeground)
            }
        }
    }

    val sheetColor = if (dark) Color(0xFF101010) else Color(0xFFFAF9FF)
    if (paletteSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { paletteSheetOpen = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = sheetColor,
            contentColor = colors.foreground,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            dragHandle = null,
        ) {
            Column(Modifier.fillMaxWidth().padding(top = 22.dp)) {
                Text(
                    "Colour",
                    modifier = Modifier.padding(horizontal = 24.dp),
                    color = colors.foreground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState())
                        .padding(start = 24.dp, end = 24.dp, top = 18.dp, bottom = 26.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(50.dp)) {
                        Surface(
                            modifier = Modifier.fillMaxSize().clickable { colorName = "Default" },
                            shape = CircleShape,
                            color = sheetColor,
                            border = BorderStroke(if (colorName == "Default") 2.dp else 1.dp, colors.secondaryForeground),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.FormatColorReset, contentDescription = "No colour", tint = colors.secondaryForeground, modifier = Modifier.size(30.dp))
                            }
                        }
                        if (colorName == "Default") SelectionCheck()
                    }
                    KeepNoteBackgrounds.filterKeys { it != "Default" }.forEach { (name, pair) ->
                        val swatch = if (dark) pair.dark else pair.light
                        Box(Modifier.size(50.dp)) {
                            Surface(
                                modifier = Modifier.fillMaxSize().clickable { colorName = name },
                                shape = CircleShape,
                                color = swatch,
                                border = BorderStroke(if (name == colorName) 2.dp else 1.dp, colors.secondaryForeground.copy(alpha = 0.65f)),
                            ) { }
                            if (name == colorName) SelectionCheck()
                        }
                    }
                }
                Text(
                    "Background",
                    modifier = Modifier.padding(horizontal = 24.dp),
                    color = colors.foreground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState())
                        .padding(start = 24.dp, end = 24.dp, top = 18.dp, bottom = 0.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(76.dp)) {
                        Surface(
                            modifier = Modifier.fillMaxSize().clickable { colorName = "Default" },
                            shape = CircleShape,
                            color = sheetColor,
                            border = BorderStroke(if (colorName == "Default") 2.dp else 1.dp, colors.secondaryForeground),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.HideImage, contentDescription = "No background", tint = colors.secondaryForeground, modifier = Modifier.size(34.dp))
                            }
                        }
                        if (colorName == "Default") SelectionCheck()
                    }
                    KeepNoteBackgroundAssets.forEach { art ->
                        Box(Modifier.size(76.dp)) {
                            Image(
                                painter = painterResource(if (dark) art.thumbnailNight else art.thumbnailDay),
                                contentDescription = "${art.name} background",
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                                    .border(
                                        if (colorName == art.selectionKey) 2.dp else 1.dp,
                                        if (colorName == art.selectionKey) Color.White else colors.divider,
                                        CircleShape,
                                    ).clickable { colorName = art.selectionKey },
                                contentScale = ContentScale.Crop,
                            )
                            if (colorName == art.selectionKey) SelectionCheck()
                        }
                    }
                }
            }
        }
    }

    if (overflowSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { overflowSheetOpen = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = sheetColor,
            contentColor = colors.foreground,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            dragHandle = null,
        ) {
            Column(Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 16.dp)) {
                Text(
                    editedLabel(current.updatedAt),
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 10.dp),
                    color = colors.foreground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                EditorSheetAction(
                    if (current.isArchived) "Unarchive" else "Archive",
                    if (current.isArchived) Icons.Outlined.Unarchive else Icons.Outlined.Archive,
                    colors,
                ) {
                    overflowSheetOpen = false
                    onArchive(draftNote())
                }
                EditorSheetAction("Share", Icons.Outlined.Share, colors) {
                    overflowSheetOpen = false
                    val text = noteAsPlainText()
                    val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
                    context.startActivity(Intent.createChooser(send, "Share note"))
                }
                EditorSheetAction("Export", Icons.Outlined.FileDownload, colors) {
                    overflowSheetOpen = false
                    exportPickerOpen = true
                }
                EditorSheetAction("Reminder", Icons.Outlined.Alarm, colors) {
                    overflowSheetOpen = false
                    reminderSheetOpen = true
                }
                EditorSheetAction("Find in note", Icons.Outlined.Search, colors) {
                    overflowSheetOpen = false
                    findDialogOpen = true
                }
                EditorSheetAction("Make a copy", Icons.Outlined.ContentCopy, colors) {
                    overflowSheetOpen = false
                    scope.launch {
                        val copied = repository.createNote(checklist = checklistMode)
                        repository.saveNote(copied.copy(
                            title = title,
                            body = body,
                            colorName = colorName,
                            isPinned = pinned,
                            reminderAt = reminderAt,
                            attachmentType = current.attachmentType,
                            attachmentPath = current.attachmentPath,
                        ))
                        items.forEachIndexed { index, item ->
                            repository.saveItem(item.copy(id = UUID.randomUUID().toString(), noteId = copied.id, position = index))
                        }
                        relations?.labels?.forEach { repository.addLabel(copied.id, it.id) }
                        Toast.makeText(context, "Copy created", Toast.LENGTH_SHORT).show()
                    }
                }
                EditorSheetAction("Labels", Icons.Outlined.Label, colors) {
                    overflowSheetOpen = false
                    labelsSheetOpen = true
                }
                EditorSheetAction("Delete", Icons.Outlined.Delete, colors) {
                    overflowSheetOpen = false
                    onTrash(draftNote())
                }
                EditorSheetAction("Help & feedback", Icons.Outlined.HelpOutline, colors) {
                    overflowSheetOpen = false
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://support.google.com/keep/")))
                    } catch (_: Exception) {
                        Toast.makeText(context, "Help is unavailable", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    if (reminderSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { reminderSheetOpen = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = sheetColor,
            contentColor = colors.foreground,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            dragHandle = null,
        ) {
            Column(Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 16.dp)) {
                Text("Reminder", Modifier.padding(start = 24.dp, end = 24.dp, bottom = 10.dp), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                EditorSheetAction("Later today", Icons.Outlined.Alarm, colors) {
                    reminderSheetOpen = false
                    setReminder(quickReminderAt(18, 0, 0))
                }
                EditorSheetAction("Tomorrow morning", Icons.Outlined.Alarm, colors) {
                    reminderSheetOpen = false
                    setReminder(quickReminderAt(9, 0, 1))
                }
                EditorSheetAction("Pick date and time", Icons.Outlined.Alarm, colors) {
                    reminderSheetOpen = false
                    dateTimePickerPending = true
                }
                if (reminderAt != null) EditorSheetAction("Remove reminder", Icons.Outlined.Delete, colors) {
                    reminderSheetOpen = false
                    setReminder(null)
                }
            }
        }
    }

    if (formattingSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { formattingSheetOpen = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = sheetColor,
            contentColor = colors.foreground,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            dragHandle = null,
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 22.dp)) {
                Text("Text formatting", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Row(Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 14.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    FormatChip("B", bodyBold, colors) { bodyBold = !bodyBold }
                    FormatChip("I", bodyItalic, colors, italic = true) { bodyItalic = !bodyItalic }
                    FormatChip("U", bodyUnderline, colors, underline = true) { bodyUnderline = !bodyUnderline }
                    FormatChip("S", bodyStrikeThrough, colors, strike = true) { bodyStrikeThrough = !bodyStrikeThrough }
                }
            }
        }
    }

    if (labelsSheetOpen) {
        AlertDialog(
            onDismissRequest = { labelsSheetOpen = false },
            title = { Text("Labels") },
            text = {
                if (labels.isEmpty()) Text("No labels yet") else Column {
                    labels.forEach { label ->
                        val attached = label.id in attachedLabelIds
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                scope.launch {
                                    if (attached) repository.removeLabel(current.id, label.id)
                                    else repository.addLabel(current.id, label.id)
                                }
                            }.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(checked = attached, onCheckedChange = {
                                scope.launch {
                                    if (it) repository.addLabel(current.id, label.id)
                                    else repository.removeLabel(current.id, label.id)
                                }
                            })
                            Text(label.name)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { labelsSheetOpen = false }) { Text("Done") } },
        )
    }

    if (findDialogOpen) {
        val matchCount = remember(findQuery, title, body) {
            if (findQuery.isBlank()) 0 else (title + "\n" + body).windowed(findQuery.length, 1, partialWindows = false)
                .count { it.equals(findQuery, ignoreCase = true) }
        }
        AlertDialog(
            onDismissRequest = { findDialogOpen = false },
            title = { Text("Find in note") },
            text = {
                Column {
                    OutlinedTextField(value = findQuery, onValueChange = { findQuery = it }, label = { Text("Find") }, singleLine = true)
                    if (findQuery.isNotBlank()) Text("$matchCount ${if (matchCount == 1) "match" else "matches"}", Modifier.padding(top = 10.dp))
                }
            },
            confirmButton = { TextButton(onClick = { findDialogOpen = false }) { Text("Done") } },
        )
    }

    if (exportPickerOpen) {
        AlertDialog(
            onDismissRequest = { exportPickerOpen = false },
            title = { Text("Export note") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            exportPickerOpen = false
                            exportTxtLauncher.launch(exportBaseName() + ".txt")
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Plain text (.txt)") }
                    TextButton(
                        onClick = {
                            exportPickerOpen = false
                            exportPdfLauncher.launch(exportBaseName() + ".pdf")
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("PDF (.pdf)") }
                    TextButton(
                        onClick = {
                            exportPickerOpen = false
                            exportDocLauncher.launch(exportBaseName() + ".doc")
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Word document (.doc)") }
                    TextButton(
                        onClick = {
                            exportPickerOpen = false
                            exportHtmlLauncher.launch(exportBaseName() + ".html")
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("HTML (.html)") }
                }
            },
            confirmButton = { TextButton(onClick = { exportPickerOpen = false }) { Text("Cancel") } },
        )
    }
    }
}

@Composable
private fun BoxScope.SelectionCheck() {
    Box(
        modifier = Modifier.align(Alignment.TopEnd).size(20.dp).background(Color.White, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Filled.Check, contentDescription = "Selected", tint = Color(0xFF202124), modifier = Modifier.size(14.dp))
    }
}

@Composable
private fun EditorSheetAction(label: String, icon: ImageVector, colors: dev.arcturus.memo.ui.theme.KeepColors, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(52.dp).clickable(onClick = onClick).padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = colors.secondaryForeground, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(24.dp))
        Text(label, color = colors.foreground, fontSize = 16.sp)
    }
}

@Composable
private fun FormatChip(
    label: String,
    selected: Boolean,
    colors: dev.arcturus.memo.ui.theme.KeepColors,
    italic: Boolean = false,
    underline: Boolean = false,
    strike: Boolean = false,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.size(52.dp).clickable(onClick = onClick),
        shape = CircleShape,
        color = if (selected) colors.fabSurface else Color.Transparent,
        border = BorderStroke(1.dp, colors.divider),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                label,
                color = colors.foreground,
                fontWeight = if (label == "B") FontWeight.Bold else FontWeight.Normal,
                fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
                textDecoration = when {
                    underline && strike -> TextDecoration.combine(listOf(TextDecoration.Underline, TextDecoration.LineThrough))
                    underline -> TextDecoration.Underline
                    strike -> TextDecoration.LineThrough
                    else -> TextDecoration.None
                },
            )
        }
    }
}

private fun editedLabel(timestamp: Long): String {
    val edited = Calendar.getInstance().apply { timeInMillis = timestamp }
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val dayLabel = when {
        edited.get(Calendar.YEAR) == today.get(Calendar.YEAR) && edited.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "today"
        edited.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) && edited.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> "yesterday"
        else -> DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(timestamp))
    }
    return "Edited $dayLabel ${DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(timestamp))}"
}
