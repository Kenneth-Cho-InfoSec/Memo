package dev.arcturus.memo.ui

import android.Manifest
import android.app.Activity
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import dev.arcturus.memo.data.KeepSettings
import dev.arcturus.memo.data.KeepSettingsStore
import dev.arcturus.memo.data.KeepOnboardingStore
import dev.arcturus.memo.data.KeepDatabase
import dev.arcturus.memo.data.KeepRepository
import dev.arcturus.memo.data.ThemeMode
import dev.arcturus.memo.data.NoteEntity
import dev.arcturus.memo.reminders.ReminderScheduler
import dev.arcturus.memo.ui.editor.KeepNoteEditorScreen
import dev.arcturus.memo.ui.home.KeepHomeScreen
import dev.arcturus.memo.ui.home.KeepSearchScreen
import dev.arcturus.memo.ui.settings.KeepSettingsScreen
import dev.arcturus.memo.ui.theme.KeepDarkColors
import dev.arcturus.memo.ui.theme.KeepLightColors
import dev.arcturus.memo.ui.theme.KeepTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.a64studio.ui.A64Theme
import dev.a64studio.ui.StudioApp
import dev.a64studio.ui.StudioViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import java.io.File
import java.util.UUID

private enum class KeepRoute { Welcome, Home, Settings, Search, Editor, Collaborators, Drawing, Audio, Assembly }

private fun isReturningRoute(initial: String, target: String): Boolean = when (target) {
    KeepRoute.Home.name -> initial in setOf(
        KeepRoute.Settings.name,
        KeepRoute.Search.name,
        KeepRoute.Editor.name,
        KeepRoute.Drawing.name,
        KeepRoute.Audio.name,
    )
    KeepRoute.Editor.name -> initial == KeepRoute.Collaborators.name
    else -> false
}

@Composable
fun KeepApp(launchNoteId: String? = null, launchNoteToken: Int = 0) {
    val context = LocalContext.current
    val store = remember(context) { KeepSettingsStore(context) }
    val onboardingStore = remember(context) { KeepOnboardingStore(context) }
    val database = remember(context) { KeepDatabase.get(context) }
    val repository = remember(database) { KeepRepository(database.keepDao()) }
    val scope = rememberCoroutineScope()
    var settings by remember { mutableStateOf(store.load()) }
    var route by rememberSaveable {
        mutableStateOf(
            if (launchNoteId.isNullOrBlank() && !onboardingStore.hasSeenWelcome()) {
                KeepRoute.Welcome.name
            } else {
                KeepRoute.Home.name
            },
        )
    }
    var collection by rememberSaveable { mutableStateOf("Notes") }
    var editingNoteId by rememberSaveable { mutableStateOf<String?>(null) }
    val audioPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            route = KeepRoute.Audio.name
        } else {
            Toast.makeText(context, "Microphone permission is required to record audio", Toast.LENGTH_SHORT).show()
        }
    }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) scope.launch {
            var imageFile: File? = null
            try {
                val attachmentPath = withContext(Dispatchers.IO) {
                    val folder = File(context.filesDir, "attachments").apply { mkdirs() }
                    val mimeType = context.contentResolver.getType(uri)
                    val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
                        ?.takeIf { it.isNotBlank() } ?: "img"
                    imageFile = File(folder, "${UUID.randomUUID()}.$extension")
                    val input = context.contentResolver.openInputStream(uri)
                        ?: error("Image picker returned an unreadable item")
                    input.use { source -> imageFile!!.outputStream().use { output -> source.copyTo(output) } }
                    imageFile!!.absolutePath
                }
                val note = repository.createNote()
                repository.saveNote(note.copy(attachmentType = "Image", attachmentPath = attachmentPath))
                editingNoteId = note.id
                collection = "Notes"
                route = KeepRoute.Editor.name
            } catch (error: Exception) {
                imageFile?.delete()
                if (error is CancellationException) throw error
                Toast.makeText(context, "Could not attach this image", Toast.LENGTH_SHORT).show()
            }
        }
    }
    val labels by remember(repository) { repository.labels() }.collectAsState(initial = emptyList())
    val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val dark = when (settings.themeMode) {
        ThemeMode.System -> systemDark
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    val colors = if (dark) KeepDarkColors else KeepLightColors
    val surfaceColor = when (route) {
        KeepRoute.Welcome.name -> keepWelcomeBackgroundColor(dark)
        KeepRoute.Home.name, KeepRoute.Search.name -> colors.background
        else -> colors.settingsBackground
    }
    val notesFlow = remember(repository, collection, labels) {
        when (collection) {
            "Archive" -> repository.archivedNotes()
            "Trash" -> repository.trashedNotes()
            "Reminders" -> repository.reminderNotes()
            "Notes" -> repository.activeNotes()
            else -> labels.firstOrNull { it.name == collection }?.let { repository.notesForLabel(it.id) }
                ?: flowOf(emptyList())
        }
    }
    val notes by notesFlow.collectAsState(initial = emptyList())
    val activeNotes by remember(repository) { repository.activeNotes() }.collectAsState(initial = emptyList())

    LaunchedEffect(launchNoteId, launchNoteToken) {
        val noteId = launchNoteId?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect
        if (repository.note(noteId).first() != null) {
            editingNoteId = noteId
            collection = "Notes"
            route = KeepRoute.Editor.name
        }
    }

    LaunchedEffect(route) {
        if (route == KeepRoute.Welcome.name) onboardingStore.markWelcomeSeen()
    }

    SideEffect {
        (context as? Activity)?.window?.let { window ->
            window.statusBarColor = surfaceColor.toArgb()
            window.navigationBarColor = surfaceColor.toArgb()
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.isAppearanceLightStatusBars = !dark
            controller.isAppearanceLightNavigationBars = !dark
        }
    }

    fun updateSettings(update: (KeepSettings) -> KeepSettings) {
        settings = update(settings)
        store.save(settings)
    }

    BackHandler(
        enabled = route == KeepRoute.Welcome.name || route == KeepRoute.Settings.name || route == KeepRoute.Search.name,
    ) {
        route = KeepRoute.Home.name
    }

    KeepTheme(dark = dark) {
        val rootModifier = Modifier.fillMaxSize().background(surfaceColor)
        Box(if (route == KeepRoute.Welcome.name) rootModifier else rootModifier.statusBarsPadding()) {
            AnimatedContent(
                targetState = route,
                transitionSpec = {
                    val returning = isReturningRoute(initialState, targetState)
                    val enter = slideInHorizontally(
                        initialOffsetX = { width -> if (returning) -width else width },
                        animationSpec = tween(KeepMotion.SCREEN_DURATION_MS, easing = KeepMotion.fragmentFastOutExtraSlowIn),
                    )
                    val exit = slideOutHorizontally(
                        targetOffsetX = { width -> if (returning) width else -width },
                        animationSpec = tween(KeepMotion.SCREEN_DURATION_MS, easing = KeepMotion.fragmentFastOutExtraSlowIn),
                    )
                    enter togetherWith exit
                },
                label = "Arcturus Memo horizontal route slide",
            ) { screenRoute ->
            when (screenRoute) {
                KeepRoute.Welcome.name -> KeepWelcomeScreen(
                    isDark = dark,
                    onGetStarted = { route = KeepRoute.Home.name },
                )
                KeepRoute.Settings.name -> KeepSettingsScreen(
                    settings = settings,
                    onSettingsChange = ::updateSettings,
                    onBack = { route = KeepRoute.Home.name },
                )
                KeepRoute.Search.name -> KeepSearchScreen(
                    notes = activeNotes,
                    repository = repository,
                    isDark = dark,
                    onBack = { route = KeepRoute.Home.name },
                    onOpenNote = { id -> editingNoteId = id; route = KeepRoute.Editor.name },
                )
                KeepRoute.Drawing.name -> KeepDrawingScreen(
                    isDark = dark,
                    onCancel = { route = KeepRoute.Home.name },
                    onSave = { path ->
                        scope.launch {
                            val note = repository.createNote()
                            repository.saveNote(note.copy(attachmentType = "Drawing", attachmentPath = path))
                            editingNoteId = note.id
                            route = KeepRoute.Editor.name
                        }
                    },
                )
                KeepRoute.Audio.name -> KeepAudioRecorderScreen(
                    isDark = dark,
                    onCancel = { route = KeepRoute.Home.name },
                    onSave = { path ->
                        scope.launch {
                            val note = repository.createNote()
                            repository.saveNote(note.copy(attachmentType = "Audio", attachmentPath = path))
                            editingNoteId = note.id
                            route = KeepRoute.Editor.name
                        }
                    },
                )
                KeepRoute.Collaborators.name -> KeepCollaboratorsScreen(
                    isDark = dark,
                    onClose = { route = KeepRoute.Editor.name },
                )
                KeepRoute.Assembly.name -> {
                    val a64Vm: StudioViewModel = viewModel()
                    BackHandler { route = KeepRoute.Home.name }
                    A64Theme(a64Vm.colors, a64Vm.themeMode, a64Vm.dynamicColor) {
                        StudioApp(a64Vm)
                    }
                }
                KeepRoute.Editor.name -> editingNoteId?.let { noteId ->
                    KeepNoteEditorScreen(
                        noteId = noteId,
                        repository = repository,
                        labels = labels,
                        addNewItemsToBottom = settings.addNewItemsToBottom,
                        moveCheckedItemsToBottom = settings.moveCheckedItemsToBottom,
                        enableSharing = settings.enableSharing,
                        isDark = dark,
                        onBack = { editingNoteId = null; route = KeepRoute.Home.name },
                        onOpenCollaborators = { route = KeepRoute.Collaborators.name },
                        onArchive = { note ->
                            scope.launch { repository.setArchived(note, !note.isArchived) }
                            ReminderScheduler.reconcile(
                                context,
                                note.copy(isArchived = !note.isArchived, isTrashed = false),
                            )
                            editingNoteId = null
                            route = KeepRoute.Home.name
                        },
                        onTrash = { note ->
                            scope.launch { repository.setTrashed(note, !note.isTrashed) }
                            ReminderScheduler.reconcile(
                                context,
                                note.copy(isTrashed = !note.isTrashed, isArchived = false),
                            )
                            editingNoteId = null
                            route = KeepRoute.Home.name
                        },
                        onDeleteForever = { note ->
                            scope.launch { repository.deleteNote(note.id) }
                            ReminderScheduler.cancel(context, note.id)
                            editingNoteId = null
                            route = KeepRoute.Home.name
                        },
                    )
                } ?: KeepHomeScreen(
                    settings = settings,
                    notes = notes,
                    repository = repository,
                    labels = labels,
                    collection = collection,
                    onSettingsChange = ::updateSettings,
                    onOpenSettings = { route = KeepRoute.Settings.name },
                    onOpenSearch = { route = KeepRoute.Search.name },
                    onOpenCollection = { target -> collection = target },
                    onCreateNote = { checklist ->
                        scope.launch {
                            val note = repository.createNote(checklist)
                            editingNoteId = note.id
                            route = KeepRoute.Editor.name
                        }
                    },
                    onCreateImageNote = { imagePicker.launch("image/*") },
                    onCreateSpecialNote = { type ->
                        when (type) {
                            "Drawing" -> route = KeepRoute.Drawing.name
                            "Audio" -> audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    onCreateAssembly = { route = KeepRoute.Assembly.name },
                    onOpenNote = { id -> editingNoteId = id; route = KeepRoute.Editor.name },
                    onCreateLabel = { name -> scope.launch { repository.createLabel(name) } },
                    onSort = { mode -> updateSettings { it.copy(sortMode = mode) } },
                    isDark = dark,
                )
                else -> KeepHomeScreen(
                    settings = settings,
                    notes = notes,
                    repository = repository,
                    labels = labels,
                    collection = collection,
                    onSettingsChange = ::updateSettings,
                    onOpenSettings = { route = KeepRoute.Settings.name },
                    onOpenSearch = { route = KeepRoute.Search.name },
                    onOpenCollection = { target -> collection = target },
                    onCreateNote = { checklist ->
                        scope.launch {
                            val note = repository.createNote(checklist)
                            editingNoteId = note.id
                            route = KeepRoute.Editor.name
                        }
                    },
                    onCreateImageNote = { imagePicker.launch("image/*") },
                    onCreateSpecialNote = { type ->
                        when (type) {
                            "Drawing" -> route = KeepRoute.Drawing.name
                            "Audio" -> audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    onCreateAssembly = { route = KeepRoute.Assembly.name },
                    onOpenNote = { id -> editingNoteId = id; route = KeepRoute.Editor.name },
                    onCreateLabel = { name -> scope.launch { repository.createLabel(name) } },
                    onSort = { mode -> updateSettings { it.copy(sortMode = mode) } },
                    isDark = dark,
                )
            }
            }
        }
    }
}
