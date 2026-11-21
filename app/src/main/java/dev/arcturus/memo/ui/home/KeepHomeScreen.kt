package dev.arcturus.memo.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalConfiguration
import dev.arcturus.memo.data.KeepSettings
import dev.arcturus.memo.data.LabelEntity
import dev.arcturus.memo.data.NoteEntity
import dev.arcturus.memo.data.KeepRepository
import dev.arcturus.memo.ui.theme.KeepColors
import dev.arcturus.memo.ui.theme.KeepDarkColors
import dev.arcturus.memo.ui.theme.KeepLightColors
import dev.arcturus.memo.ui.KeepMotion

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun KeepHomeScreen(
    settings: KeepSettings,
    notes: List<NoteEntity>,
    repository: KeepRepository,
    labels: List<LabelEntity>,
    collection: String,
    onSettingsChange: ((KeepSettings) -> KeepSettings) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenCollection: (String) -> Unit,
    onCreateNote: (Boolean) -> Unit,
    onCreateImageNote: () -> Unit,
    onCreateSpecialNote: (String) -> Unit,
    onOpenNote: (String) -> Unit,
    onCreateLabel: (String) -> Unit,
    onSort: (String) -> Unit,
    isDark: Boolean,
) {
    val colors = if (isDark) KeepDarkColors else KeepLightColors
    var drawerOpen by remember { mutableStateOf(false) }
    var createMenuOpen by remember { mutableStateOf(false) }
    var sortMenuOpen by remember { mutableStateOf(false) }
    var createLabelDialog by remember { mutableStateOf(false) }
    var newLabel by remember { mutableStateOf("") }
    val sortedNotes = when (settings.sortMode) {
        "Date created" -> notes.sortedByDescending { it.createdAt }
        "Title" -> notes.sortedBy { it.title.lowercase() }
        else -> notes.sortedByDescending { it.updatedAt }
    }.let { order -> if (settings.sortMode == "Title") order else order.sortedByDescending { it.isPinned } }

    Box(Modifier.fillMaxSize().background(colors.background)) {
        Column(Modifier.fillMaxSize()) {
            HomeToolbar(
                colors = colors,
                gridView = settings.gridView,
                onMenu = { drawerOpen = true },
                onSearch = onOpenSearch,
                onGrid = { onSettingsChange { it.copy(gridView = !it.gridView) } },
                onSort = { sortMenuOpen = true },
            )

            if (sortedNotes.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(
                        modifier = Modifier.offset(y = (-57).dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(
                            painter = painterResource(dev.arcturus.memo.R.drawable.ic_lightbulb_outline_yellow500_120),
                            contentDescription = null,
                            modifier = Modifier.size(120.dp),
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = when (collection) {
                                "Archive" -> "Archived notes appear here"
                                "Trash" -> "Notes you delete appear here"
                                "Reminders" -> "Notes with reminders appear here"
                                else -> "Notes you add appear here"
                            },
                            color = colors.foreground,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                AnimatedContent(
                    targetState = settings.gridView,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    transitionSpec = {
                        (fadeIn(tween(150)) + scaleIn(initialScale = 0.98f, animationSpec = tween(150))) togetherWith
                            (fadeOut(tween(100)) + scaleOut(targetScale = 0.98f, animationSpec = tween(100)))
                    },
                    label = "note grid or list",
                ) {
                    if (it) {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 112.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalItemSpacing = 8.dp,
                        ) {
                            items(sortedNotes, key = { note -> note.id }) { note ->
                                NotePreviewCard(
                                    note,
                                    repository,
                                    isDark,
                                    onClick = { onOpenNote(note.id) },
                                    modifier = Modifier.animateItem(
                                        fadeInSpec = tween(120, easing = KeepMotion.fragmentFastOutExtraSlowIn),
                                        fadeOutSpec = tween(100, easing = KeepMotion.fragmentFastOutExtraSlowIn),
                                        placementSpec = tween(250, easing = KeepMotion.fragmentFastOutExtraSlowIn),
                                    ),
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 112.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(sortedNotes, key = { note -> note.id }) { note ->
                                NotePreviewCard(
                                    note,
                                    repository,
                                    isDark,
                                    onClick = { onOpenNote(note.id) },
                                    modifier = Modifier.animateItem(
                                        fadeInSpec = tween(120, easing = KeepMotion.fragmentFastOutExtraSlowIn),
                                        fadeOutSpec = tween(100, easing = KeepMotion.fragmentFastOutExtraSlowIn),
                                        placementSpec = tween(250, easing = KeepMotion.fragmentFastOutExtraSlowIn),
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = !createMenuOpen,
            modifier = Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(end = 16.dp, bottom = 16.dp),
            enter = fadeIn(tween(100)) + scaleIn(initialScale = 0.85f, animationSpec = tween(180, easing = KeepMotion.fragmentFastOutExtraSlowIn)),
            exit = fadeOut(tween(80)) + scaleOut(targetScale = 0.9f, animationSpec = tween(140, easing = KeepMotion.fragmentFastOutExtraSlowIn)),
        ) {
            FloatingActionButton(
                onClick = {
                    if (settings.createTextNotesByDefault) onCreateNote(false) else createMenuOpen = true
                },
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(26.dp),
                containerColor = colors.fabSurface,
                contentColor = colors.fabForeground,
                elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(4.dp),
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add note", modifier = Modifier.size(36.dp))
            }
        }

        NavigationDrawer(
            visible = drawerOpen,
            colors = colors,
            currentCollection = collection,
            labels = labels,
            onDismiss = { drawerOpen = false },
            onSettings = { drawerOpen = false; onOpenSettings() },
            onCollection = { target -> drawerOpen = false; onOpenCollection(target) },
            onCreateLabel = { drawerOpen = false; createLabelDialog = true },
        )

        Box(Modifier.align(Alignment.TopEnd).padding(end = 44.dp, top = 76.dp)) {
            DropdownMenu(expanded = sortMenuOpen, onDismissRequest = { sortMenuOpen = false }) {
                listOf("Date created", "Last edited", "Title").forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = { sortMenuOpen = false; onSort(option) })
                }
            }
        }

        AnimatedVisibility(
            visible = createMenuOpen,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(tween(100)),
            exit = fadeOut(tween(100)),
        ) {
            Box(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.32f)).clickable { createMenuOpen = false })
                Column(
                    modifier = Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(end = 16.dp, bottom = 128.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    listOf("Image", "Drawing", "Audio", "List", "Text").forEachIndexed { index, type ->
                        AnimatedVisibility(
                            visible = createMenuOpen,
                            enter = slideInVertically(
                                initialOffsetY = { it / 5 },
                                animationSpec = tween(180, delayMillis = index * 24, easing = KeepMotion.fragmentFastOutExtraSlowIn),
                            ) + fadeIn(tween(100, delayMillis = index * 24)),
                            exit = slideOutVertically(
                                targetOffsetY = { it / 8 },
                                animationSpec = tween(100, easing = KeepMotion.fragmentFastOutExtraSlowIn),
                            ) + fadeOut(tween(80)),
                        ) {
                            CreateTypeRow(type, colors) {
                                createMenuOpen = false
                                when (type) {
                                    "Image" -> onCreateImageNote()
                                    "Drawing", "Audio" -> onCreateSpecialNote(type)
                                    else -> onCreateNote(type == "List")
                                }
                            }
                        }
                    }
                }
                FloatingActionButton(
                    onClick = { createMenuOpen = false },
                    modifier = Modifier.align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(end = 16.dp, bottom = 64.dp)
                        .size(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    containerColor = colors.fabSurface,
                    contentColor = colors.fabForeground,
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Close create menu", modifier = Modifier.size(24.dp))
                }
            }
        }

        if (createLabelDialog) {
            AlertDialog(
                onDismissRequest = { createLabelDialog = false; newLabel = "" },
                title = { Text("Create new label") },
                text = {
                    OutlinedTextField(value = newLabel, onValueChange = { newLabel = it }, singleLine = true, label = { Text("Label name") })
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (newLabel.isNotBlank()) onCreateLabel(newLabel)
                        createLabelDialog = false
                        newLabel = ""
                    }) { Text("Save") }
                },
                dismissButton = { TextButton(onClick = { createLabelDialog = false; newLabel = "" }) { Text("Cancel") } },
            )
        }
    }
}

@Composable
private fun CreateTypeRow(type: String, colors: KeepColors, onClick: () -> Unit) {
    val icon = when (type) {
        "List" -> Icons.Filled.CheckBox
        "Image" -> Icons.Filled.Image
        "Drawing" -> Icons.Filled.Brush
        "Audio" -> Icons.Filled.Mic
        else -> Icons.Filled.StickyNote2
    }
    Surface(
        modifier = Modifier.height(56.dp).wrapContentWidth().clickable(onClick = onClick),
        color = colors.fabSurface,
        contentColor = colors.fabForeground,
        shape = CircleShape,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Text(type, modifier = Modifier.padding(start = 12.dp), fontSize = 16.sp)
        }
    }
}

@Composable
private fun HomeToolbar(
    colors: KeepColors,
    gridView: Boolean,
    onMenu: () -> Unit,
    onSearch: () -> Unit,
    onGrid: () -> Unit,
    onSort: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(72.dp).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onMenu, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Filled.Menu, contentDescription = "Open navigation drawer", tint = colors.secondaryForeground)
        }
        Surface(
            modifier = Modifier.weight(1f).height(56.dp).clickable(onClick = onSearch),
            color = colors.searchSurface,
            shape = CircleShape,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Search Arcturus Memo", modifier = Modifier.weight(1f).padding(start = 24.dp), fontSize = 16.sp, color = colors.secondaryForeground)
                IconButton(onClick = onGrid, modifier = Modifier.size(48.dp)) {
                    Icon(if (gridView) Icons.Filled.GridView else Icons.Filled.ViewAgenda, contentDescription = if (gridView) "Grid view" else "List view", tint = colors.secondaryForeground)
                }
                IconButton(onClick = onSort, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Filled.Sort, contentDescription = "Sort notes", tint = colors.secondaryForeground)
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        Surface(modifier = Modifier.size(32.dp), shape = CircleShape, color = Color(0xFF008577)) {
            Box(contentAlignment = Alignment.Center) { Text("K", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Medium) }
        }
    }
}

@Composable
private fun NavigationDrawer(
    visible: Boolean,
    colors: KeepColors,
    currentCollection: String,
    labels: List<LabelEntity>,
    onDismiss: () -> Unit,
    onSettings: () -> Unit,
    onCollection: (String) -> Unit,
    onCreateLabel: () -> Unit,
) {
    val drawerWidth = if (LocalConfiguration.current.smallestScreenWidthDp >= 400) 320.dp else 304.dp
    val scrimAlpha by animateFloatAsState(
        targetValue = if (visible) colors.drawerScrimAlpha else 0f,
        animationSpec = tween(if (visible) 150 else 250, easing = LinearEasing),
        label = "drawer scrim",
    )
    if (visible || scrimAlpha > 0f) {
        Box(Modifier.fillMaxSize()) {
            Box(
                Modifier.fillMaxSize()
                    .background(Color.Black.copy(alpha = scrimAlpha))
                    .clickable(enabled = visible, onClick = onDismiss),
            )
            AnimatedVisibility(
                visible = visible,
                enter = slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(300, easing = KeepMotion.fragmentFastOutExtraSlowIn),
                ) + fadeIn(tween(100)),
                exit = slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(250, easing = KeepMotion.fragmentFastOutExtraSlowIn),
                ) + fadeOut(tween(100)),
            ) {
                Surface(
                    modifier = Modifier.fillMaxHeight().width(drawerWidth),
                    color = colors.drawerBackground,
                    shape = RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp),
                ) {
                    Column(Modifier.padding(top = 2.dp, bottom = 20.dp)) {
                        Text("Arcturus Memo", modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 18.5.dp), fontSize = 24.sp, color = colors.foreground)
                        DrawerItem("Notes", Icons.Filled.StickyNote2, currentCollection == "Notes", colors) { onCollection("Notes") }
                        DrawerItem("Reminders", Icons.Filled.NotificationsNone, currentCollection == "Reminders", colors) { onCollection("Reminders") }
                        DrawerItem("Create new label", Icons.Filled.Add, false, colors, onCreateLabel)
                        labels.forEach { label -> DrawerItem(label.name, Icons.Filled.Label, currentCollection == label.name, colors) { onCollection(label.name) } }
                        DrawerItem("Archive", Icons.Filled.Archive, currentCollection == "Archive", colors) { onCollection("Archive") }
                        DrawerItem("Trash", Icons.Filled.Delete, currentCollection == "Trash", colors) { onCollection("Trash") }
                        DrawerItem("Settings", Icons.Filled.Settings, false, colors, onSettings)
                        DrawerItem("Help & feedback", Icons.Filled.HelpOutline, false, colors) { onDismiss() }
                        DrawerItem("Keyboard shortcuts", Icons.Filled.Keyboard, false, colors) { onDismiss() }
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, colors: KeepColors, onClick: () -> Unit) {
    val tint = if (selected) colors.fabForeground else colors.secondaryForeground
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
            .height(55.dp).background(if (selected) colors.fabSurface else Color.Transparent, RoundedCornerShape(28.dp))
            .clickable(onClick = onClick).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        Text(label, modifier = Modifier.padding(start = 16.dp), color = if (selected) colors.fabForeground else colors.foreground, fontSize = 15.sp)
    }
}
