package dev.arcturus.memo.ui.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import dev.arcturus.memo.R
import dev.arcturus.memo.data.KeepSettings
import dev.arcturus.memo.data.ThemeMode
import dev.arcturus.memo.ui.theme.KeepDarkColors
import dev.arcturus.memo.ui.theme.KeepLightColors

private val ThemeProductSans = FontFamily(Font(R.font.product_sans_regular))

@Composable
fun KeepSettingsScreen(
    settings: KeepSettings,
    onSettingsChange: ((KeepSettings) -> KeepSettings) -> Unit,
    onBack: () -> Unit,
) {
    val dark = MaterialTheme.colorScheme.background == KeepDarkColors.background
    val colors = if (dark) KeepDarkColors else KeepLightColors
    val context = LocalContext.current
    var themeDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = colors.secondaryForeground)
            }
            Spacer(Modifier.width(8.dp))
            Text("Settings", fontSize = 18.sp, color = colors.foreground)
        }

        SectionTitle("Display options", colors.foreground, top = 2.5.dp)
        SettingSwitchRow("Add new items to bottom", settings.addNewItemsToBottom, colors) {
            onSettingsChange { it.copy(addNewItemsToBottom = !it.addNewItemsToBottom) }
        }
        SettingSwitchRow("Move checked items to bottom", settings.moveCheckedItemsToBottom, colors) {
            onSettingsChange { it.copy(moveCheckedItemsToBottom = !it.moveCheckedItemsToBottom) }
        }
        SettingSwitchRow("Display rich link previews", settings.displayRichLinkPreviews, colors) {
            onSettingsChange { it.copy(displayRichLinkPreviews = !it.displayRichLinkPreviews) }
        }
        Row(
            modifier = Modifier.fillMaxWidth().height(48.dp).clickable { themeDialog = true }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Theme", fontSize = 16.sp, color = colors.foreground)
            Text(settings.themeMode.label(), fontSize = 14.sp, color = colors.foreground)
        }

        SectionTitle("Note creation", colors.foreground, top = 26.dp)
        SettingSwitchRow("Create text notes by default", settings.createTextNotesByDefault, colors) {
            onSettingsChange { it.copy(createTextNotesByDefault = !it.createTextNotesByDefault) }
        }
        Text(
            "You can always long press to create other note types.",
            modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 0.dp, bottom = 30.dp),
            color = colors.secondaryForeground,
            fontSize = 12.sp,
            fontStyle = FontStyle.Italic,
        )

        SectionTitle("Sharing", colors.foreground, top = 0.dp)
        SettingSwitchRow("Enable sharing", settings.enableSharing, colors) {
            onSettingsChange { it.copy(enableSharing = !it.enableSharing) }
        }
    }

    if (themeDialog) {
        val dialogWidth = if (LocalConfiguration.current.screenWidthDp < 600) 325.dp else 560.dp
        Dialog(
            onDismissRequest = { themeDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        )
        {
            val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
            SideEffect { dialogWindow?.setDimAmount(0.32f) }
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.width(dialogWidth).height(371.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = if (dark) colors.settingsBackground else Color(0xFFE9EEF6),
                    shadowElevation = 6.dp,
                ) {
                    Box(Modifier.fillMaxSize()) {
                        Text(
                            "Choose theme",
                            modifier = Modifier.offset(x = 24.dp, y = 26.dp),
                            color = colors.foreground,
                            fontSize = 24.sp,
                            letterSpacing = 0.5.sp,
                            fontFamily = ThemeProductSans,
                        )
                        Column(Modifier.fillMaxWidth().offset(y = 75.dp)) {
                            listOf(ThemeMode.Light, ThemeMode.Dark, ThemeMode.System).forEach { mode ->
                                ThemeChoiceRow(
                                    label = mode.label(),
                                    selected = settings.themeMode == mode,
                                    colors = colors,
                                    onClick = {
                                        onSettingsChange { it.copy(themeMode = mode) }
                                        themeDialog = false
                                    },
                                )
                            }
                        }
                        val helpText = buildAnnotatedString {
                            append("To change the theme of your Arcturus Memo widget and notifications, manage your Android ")
                            pushStringAnnotation(tag = "display-settings", annotation = "open")
                            pushStyle(SpanStyle(color = Color(0xFF0B57D0), textDecoration = TextDecoration.Underline))
                            append("screen and display settings")
                            pop()
                            pop()
                            append(".")
                        }
                        ClickableText(
                            text = helpText,
                            modifier = Modifier.offset(x = 24.dp, y = 228.dp).width(dialogWidth - 48.dp),
                            style = TextStyle(
                                color = colors.secondaryForeground,
                                fontSize = 14.75.sp,
                                lineHeight = 21.75.sp,
                                fontFamily = ThemeProductSans,
                            ),
                            onClick = { offset ->
                                if (helpText.getStringAnnotations("display-settings", offset, offset).isNotEmpty()) {
                                    context.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS))
                                }
                            },
                        )
                        Box(
                            modifier = Modifier.align(Alignment.BottomEnd)
                                .padding(end = 24.dp, bottom = 14.dp)
                                .size(width = 78.dp, height = 50.dp)
                                .clickable(role = androidx.compose.ui.semantics.Role.Button) { themeDialog = false },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "Cancel",
                                color = Color(0xFF0B57D0),
                                fontSize = 14.5.sp,
                                fontFamily = ThemeProductSans,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String, color: Color, top: androidx.compose.ui.unit.Dp) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = top, bottom = 4.dp),
        color = color,
        fontSize = 24.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
    )
}

@Composable
private fun SettingSwitchRow(label: String, checked: Boolean, colors: dev.arcturus.memo.ui.theme.KeepColors, onChange: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(48.dp).clickable(onClick = onChange)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = colors.foreground, fontSize = 14.sp)
        Switch(
            checked = checked,
            onCheckedChange = { onChange() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.switchThumbOn,
                checkedTrackColor = colors.switchTrackOn,
                uncheckedThumbColor = colors.switchThumbOff,
                uncheckedTrackColor = colors.switchTrackOff,
                uncheckedBorderColor = colors.secondaryForeground,
            ),
        )
    }
}

@Composable
private fun ThemeChoiceRow(
    label: String,
    selected: Boolean,
    colors: dev.arcturus.memo.ui.theme.KeepColors,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(48.dp)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics { this.selected = selected },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.width(26.dp))
        Box(
            modifier = Modifier.size(20.dp)
                .border(2.dp, if (selected) MaterialTheme.colorScheme.primary else colors.secondaryForeground, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Box(Modifier.size(10.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
            }
        }
        Spacer(Modifier.width(25.5.dp))
        Text(
            label,
            color = colors.secondaryForeground,
            fontSize = 16.sp,
            fontFamily = ThemeProductSans,
        )
    }
}

private fun ThemeMode.label(): String = when (this) {
    ThemeMode.System -> "System default"
    ThemeMode.Light -> "Light"
    ThemeMode.Dark -> "Dark"
}
