package dev.arcturus.memo.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.arcturus.memo.ui.theme.KeepDarkColors
import dev.arcturus.memo.ui.theme.KeepLightColors

@Composable
fun KeepCollaboratorsScreen(isDark: Boolean, onClose: () -> Unit) {
    val colors = if (isDark) KeepDarkColors else KeepLightColors
    var address by rememberSaveable { mutableStateOf("") }
    var unavailable by rememberSaveable { mutableStateOf(false) }
    BackHandler(onBack = onClose)

    Column(Modifier.fillMaxSize().background(if (isDark) colors.background else colors.searchSurface)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Filled.Close, contentDescription = "Close collaborators", tint = colors.secondaryForeground)
            }
            Spacer(Modifier.width(8.dp))
            Text("Collaborators", fontSize = 18.sp, color = colors.foreground)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = {
                if (address.isBlank()) onClose() else unavailable = true
            }) {
                Text("Save", color = Color(0xFF1A73E8), fontSize = 14.sp)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().height(88.dp).padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape).background(colors.fabForeground),
                contentAlignment = Alignment.Center,
            ) {
                Text("L", color = colors.searchSurface, fontSize = 24.sp, fontWeight = FontWeight.Medium)
            }
            Text(
                "This device",
                modifier = Modifier.padding(start = 20.dp),
                color = colors.foreground,
                fontSize = 16.sp,
            )
        }

        Divider(color = colors.divider)

        Row(
            modifier = Modifier.fillMaxWidth().height(76.dp).padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.PersonAdd, contentDescription = null, tint = colors.secondaryForeground)
            Spacer(Modifier.width(20.dp))
            Box(Modifier.weight(1f)) {
                if (address.isEmpty()) {
                    Text("Person or email to share with", color = colors.secondaryForeground, fontSize = 16.sp)
                }
                BasicTextField(
                    value = address,
                    onValueChange = { address = it; unavailable = false },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = TextStyle(color = colors.foreground, fontSize = 16.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { if (address.isNotBlank()) unavailable = true }),
                )
            }
        }

        Divider(color = colors.divider)

        if (unavailable) {
            Text(
                "Collaborator invites require a Google account. This local-only copy did not send an invite.",
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
                color = colors.secondaryForeground,
                fontSize = 14.sp,
            )
        } else {
            Spacer(Modifier.height(16.dp))
        }
        Spacer(Modifier.weight(1f))
    }
}
