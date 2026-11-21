package dev.arcturus.memo.ui

import android.graphics.Bitmap
import android.graphics.Paint as AndroidPaint
import android.graphics.Path as AndroidPath
import android.graphics.Canvas as AndroidCanvas
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.arcturus.memo.ui.theme.KeepDarkColors
import dev.arcturus.memo.ui.theme.KeepLightColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

private data class DrawingStroke(
    val color: Color,
    val points: androidx.compose.runtime.snapshots.SnapshotStateList<Offset>,
)

@Composable
fun KeepDrawingScreen(isDark: Boolean, onCancel: () -> Unit, onSave: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val colors = if (isDark) KeepDarkColors else KeepLightColors
    val strokes = remember { mutableStateListOf<DrawingStroke>() }
    val palette = listOf(Color(0xFF202124), Color(0xFFEA4335), Color(0xFF1A73E8), Color(0xFF34A853))
    var selectedColor by remember { mutableStateOf(palette.first()) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var saving by remember { mutableStateOf(false) }

    BackHandler(onBack = onCancel)

    Column(Modifier.fillMaxSize().background(if (isDark) colors.settingsBackground else Color.White)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onCancel) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Cancel drawing", tint = colors.foreground)
            }
            Text("Drawing", color = colors.foreground, fontSize = 18.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (strokes.isNotEmpty()) strokes.removeAt(strokes.lastIndex) }, enabled = strokes.isNotEmpty()) {
                    Icon(Icons.Filled.Undo, contentDescription = "Undo stroke", tint = colors.foreground)
                }
                IconButton(onClick = { strokes.clear() }, enabled = strokes.isNotEmpty()) {
                    Icon(Icons.Filled.Clear, contentDescription = "Clear drawing", tint = colors.foreground)
                }
                IconButton(
                    onClick = {
                        if (saving || canvasSize.width < 1 || canvasSize.height < 1) return@IconButton
                        saving = true
                        scope.launch {
                            val path = saveDrawing(context.filesDir, canvasSize, strokes.toList())
                            saving = false
                            onSave(path)
                        }
                    },
                    enabled = !saving,
                ) {
                    Icon(Icons.Filled.Done, contentDescription = "Save drawing", tint = colors.foreground)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(Icons.Filled.Brush, contentDescription = "Brush color", tint = colors.secondaryForeground)
            palette.forEach { color ->
                Surface(
                    modifier = Modifier.size(if (selectedColor == color) 34.dp else 28.dp)
                        .clip(CircleShape)
                        .clickable { selectedColor = color },
                    shape = CircleShape,
                    color = color,
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (selectedColor == color) 2.dp else 1.dp,
                        color = if (selectedColor == color) colors.foreground else colors.divider,
                    ),
                ) {}
            }
        }

        Box(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
            Canvas(
                modifier = Modifier.fillMaxSize()
                    .clip(MaterialTheme.shapes.medium)
                    .background(Color.White)
                    .onSizeChanged { canvasSize = it }
                    .pointerInput(selectedColor) {
                        var activeStroke: DrawingStroke? = null
                        detectDragGestures(
                            onDragStart = { point ->
                                activeStroke = DrawingStroke(selectedColor, mutableStateListOf(point))
                                strokes.add(activeStroke!!)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                activeStroke?.points?.add(change.position)
                            },
                            onDragEnd = { activeStroke = null },
                            onDragCancel = { activeStroke = null },
                        )
                    },
            ) {
                strokes.forEach { stroke ->
                    val path = Path()
                    stroke.points.firstOrNull()?.let { first ->
                        path.moveTo(first.x, first.y)
                        stroke.points.drop(1).forEach { point -> path.lineTo(point.x, point.y) }
                        drawPath(
                            path = path,
                            color = stroke.color,
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                        )
                    }
                }
            }
        }
        Text(
            "Draw with your finger",
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp),
            color = colors.secondaryForeground,
            fontSize = 12.sp,
        )
    }
}

private suspend fun saveDrawing(folder: File, size: IntSize, strokes: List<DrawingStroke>): String =
    withContext(Dispatchers.IO) {
        val outputFolder = File(folder, "attachments").apply { mkdirs() }
        val destination = File(outputFolder, "${UUID.randomUUID()}.png")
        val bitmap = Bitmap.createBitmap(size.width.coerceAtLeast(1), size.height.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)
        strokes.forEach { stroke ->
            val points = stroke.points
            if (points.isNotEmpty()) {
                val paint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                    color = stroke.color.toArgb()
                    style = AndroidPaint.Style.STROKE
                    strokeWidth = 8f
                    strokeCap = AndroidPaint.Cap.ROUND
                    strokeJoin = AndroidPaint.Join.ROUND
                }
                val path = AndroidPath().apply {
                    moveTo(points.first().x, points.first().y)
                    points.drop(1).forEach { lineTo(it.x, it.y) }
                }
                if (points.size == 1) canvas.drawCircle(points.first().x, points.first().y, 4f, paint)
                else canvas.drawPath(path, paint)
            }
        }
        destination.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        destination.absolutePath
    }
