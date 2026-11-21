package dev.arcturus.memo.ui

import android.graphics.Typeface
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import dev.arcturus.memo.R
import kotlinx.coroutines.delay

internal fun keepWelcomeBackgroundColor(isDark: Boolean): Color =
    if (isDark) Color(0xFF000000) else Color(0xFFFAF8FE)

@Composable
fun KeepWelcomeScreen(isDark: Boolean, onGetStarted: () -> Unit) {
    val context = LocalContext.current
    val systemColors = remember(context, isDark) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            null
        }
    }
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val background = when {
        !isDark && screenWidthDp >= 600 -> Color(0xFFFBF9FE)
        isDark -> Color(0xFF000000)
        else -> systemColors?.surface ?: keepWelcomeBackgroundColor(isDark)
    }
    val foreground = systemColors?.onSurface
        ?: if (isDark) Color(0xFFE4E5F0) else Color(0xFF30323A)
    val buttonBackground = systemColors?.primary
        ?: if (isDark) Color(0xFFB8C6EE) else Color(0xFF4C5E8B)
    val buttonForeground = systemColors?.onPrimary
        ?: if (isDark) Color(0xFF323F60) else Color(0xFFF9F8FF)

    LaunchedEffect(Unit) {
        delay(10_000)
        onGetStarted()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .pointerInput(onGetStarted) {
                var horizontalDistance = 0f
                detectHorizontalDragGestures(
                    onDragStart = { horizontalDistance = 0f },
                    onDragEnd = {
                        if (horizontalDistance < -80f) onGetStarted()
                    },
                    onDragCancel = { horizontalDistance = 0f },
                    onHorizontalDrag = { _, delta -> horizontalDistance += delta },
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AndroidView(
            factory = { viewContext ->
                ImageView(viewContext).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    scaleType = ImageView.ScaleType.CENTER_INSIDE
                    contentDescription = null
                    setImageResource(R.drawable.product_logo_keep_2026_color_192)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
        KeepWelcomeText(
            text = "Capture anything",
            color = foreground,
            fontResource = R.font.google_sans_compat,
            fontSizeSp = 22f,
            textScaleX = 1.011f,
            horizontalPadding = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 32.4.dp),
        )
        KeepWelcomeText(
            text = "Make lists, take photos, speak your mind - whatever works for you, works in Arcturus Memo.",
            color = foreground,
            fontResource = R.font.google_sans_text_compat,
            fontSizeSp = 14f,
            textScaleX = 1.025f,
            horizontalPadding = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .heightIn(min = 39.25.dp),
        )
        Button(
            onClick = onGetStarted,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = buttonForeground,
            ),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .padding(top = 24.dp, bottom = 48.dp)
                .offset(x = (-0.36).dp)
                .height(50.5.dp)
                .widthIn(min = 108.dp)
                .widthIn(max = 108.dp),
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.5.dp)
                    .background(buttonBackground, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                KeepWelcomeText(
                    text = "Get started",
                    color = buttonForeground,
                    fontResource = R.font.google_sans_text_medium_compat,
                    fontSizeSp = 14f,
                    textScaleX = 1.03f,
                    horizontalPadding = 0.dp,
                    modifier = Modifier.offset(y = (-0.36).dp),
                )
            }
        }
    }
}

@Composable
private fun KeepWelcomeText(
    text: String,
    color: Color,
    fontResource: Int,
    fontSizeSp: Float,
    textScaleX: Float,
    horizontalPadding: Dp,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val typeface = androidx.compose.runtime.remember(context, fontResource) {
        runCatching { ResourcesCompat.getFont(context, fontResource) }.getOrNull()
    }
    val horizontalPaddingPx = with(density) { horizontalPadding.roundToPx() }

    AndroidView(
        factory = { viewContext ->
            TextView(viewContext).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                gravity = Gravity.CENTER
                includeFontPadding = true
            }
        },
        update = { view ->
            view.text = text
            view.setTextColor(color.toArgb())
            view.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeSp)
            view.textScaleX = textScaleX
            view.letterSpacing = 0f
            view.setPadding(horizontalPaddingPx, 0, horizontalPaddingPx, 0)
            view.includeFontPadding = true
            view.typeface = typeface ?: Typeface.DEFAULT
        },
        modifier = modifier,
    )
}
